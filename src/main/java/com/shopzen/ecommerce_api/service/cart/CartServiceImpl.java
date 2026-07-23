package com.shopzen.ecommerce_api.service.cart;

import com.shopzen.ecommerce_api.dto.product.CartItemDTO;
import com.shopzen.ecommerce_api.dto.product.CartSummaryDTO;
import com.shopzen.ecommerce_api.entity.Cart;
import com.shopzen.ecommerce_api.entity.CartItem;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.CartItemRepository;
import com.shopzen.ecommerce_api.repository.CartRepository;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Override
    public List<CartItemDTO> getCartItems(String userId) {
        log.debug("Fetching cart items for user: {}", userId);
        
        Cart cart = getOrCreateCart(userId);
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        return cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    item.setProduct(product);
                    return entityMapper.toCartItemDTO(item);
                })
                .collect(Collectors.toList());
    }

    @Override
    public CartItemDTO addToCart(String userId, String productId, Integer quantity, String color, String size) {
        log.info("Adding to cart - userId: {}, productId: {}, quantity: {}", userId, productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        Cart cart = getOrCreateCart(userId);
        
        List<CartItem> existingItems = cartItemRepository.findByCartId(cart.getId());
        
        CartItem existingItem = existingItems.stream()
                .filter(item -> item.getProductId().equals(productId) &&
                        (color == null || color.equals(item.getSelectedColor())) &&
                        (size == null || size.equals(item.getSelectedSize())))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            CartItem saved = cartItemRepository.save(existingItem);
            saved.setProduct(product);
            return entityMapper.toCartItemDTO(saved);
        }

        CartItem cartItem = CartItem.builder()
                .cart(cart)
                .productId(productId)
                .quantity(quantity)
                .selectedColor(color)
                .selectedSize(size)
                .build();

        cartItem = cartItemRepository.save(cartItem);
        cartItem.setProduct(product);
        return entityMapper.toCartItemDTO(cartItem);
    }

    @Override
    public CartItemDTO updateQuantity(String userId, String itemId, Integer quantity) {
        log.info("Updating quantity - userId: {}, itemId: {}, quantity: {}", userId, itemId, quantity);
        
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("Quantity must be a positive number");
        }

        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            log.warn("Unauthorized access attempt - userId: {}, itemOwnerId: {}", userId, cartItem.getCart().getUserId());
            throw new RuntimeException("You don't have permission to modify this cart item");
        }

        if (quantity == 0) {
            cartItemRepository.delete(cartItem);
            log.info("Cart item deleted because quantity is 0: {}", itemId);
            return null;
        }

        cartItem.setQuantity(quantity);
        CartItem saved = cartItemRepository.save(cartItem);
        log.info("Cart item updated: {}", saved.getId());
        
        Product product = productRepository.findById(saved.getProductId()).orElse(null);
        saved.setProduct(product);
        
        return entityMapper.toCartItemDTO(saved);
    }

    @Override
    public void removeFromCart(String userId, String itemId) {
        log.info("Removing from cart - userId: {}, itemId: {}", userId, itemId);
        
        CartItem cartItem = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found with id: " + itemId));

        if (!cartItem.getCart().getUserId().equals(userId)) {
            throw new RuntimeException("You don't have permission to remove this cart item");
        }

        cartItemRepository.delete(cartItem);
        log.info("Cart item removed: {}", itemId);
    }

    @Override
    public void clearCart(String userId) {
        log.info("Clearing cart for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getId());
            log.info("Cart cleared for user: {}", userId);
        }
    }

    @Override
    public CartSummaryDTO getCartSummary(String userId) {
        log.debug("Getting cart summary for user: {}", userId);
        
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return CartSummaryDTO.builder()
                    .itemCount(0)
                    .subtotal(BigDecimal.ZERO)
                    .items(List.of())
                    .build();
        }
        
        List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
        
        int itemCount = cartItems.stream().mapToInt(CartItem::getQuantity).sum();
        
        BigDecimal subtotal = cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    if (product == null) {
                        log.warn("Product not found for cart item: {}", item.getId());
                        return BigDecimal.ZERO;
                    }
                    return product.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                })
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<CartItemDTO> itemDTOs = cartItems.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    item.setProduct(product);
                    return entityMapper.toCartItemDTO(item);
                })
                .collect(Collectors.toList());

        return CartSummaryDTO.builder()
                .itemCount(itemCount)
                .subtotal(subtotal)
                .items(itemDTOs)
                .build();
    }

    @Override
    public boolean isInCart(String userId, String productId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) return false;
        
        return cartItemRepository.findByCartId(cart.getId()).stream()
                .anyMatch(item -> item.getProductId().equals(productId));
    }
    
    private Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart newCart = Cart.builder()
                            .userId(userId)
                            .build();
                    return cartRepository.save(newCart);
                });
    }
}
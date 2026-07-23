package com.shopzen.ecommerce_api.service.wishlist;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.wishlist.WishlistItemDTO;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.entity.WishlistItem;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.repository.WishlistItemRepository;
import com.shopzen.ecommerce_api.util.EntityMapper;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistServiceImpl implements WishlistService {

    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Override
    public List<WishlistItemDTO> getWishlistItems(String userId) {  // Changed from UUID to String
        List<WishlistItem> items = wishlistItemRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return items.stream()
                .map(item -> {
                    Product product = productRepository.findById(item.getProductId()).orElse(null);
                    item.setProduct(product);
                    return toDTO(item);
                })
                .collect(Collectors.toList());
    }

    @Override
    public WishlistItemDTO addToWishlist(String userId, String productId) {  // Changed from UUID to String
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (wishlistItemRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("Product already in wishlist");
        }

        WishlistItem item = WishlistItem.builder()
                .userId(userId)
                .productId(productId)
                .build();

        item = wishlistItemRepository.save(item);
        item.setProduct(product);
        return toDTO(item);
    }

    @Override
    public void removeFromWishlist(String userId, String productId) {  // Changed from UUID to String
        WishlistItem item = wishlistItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in wishlist"));
        wishlistItemRepository.delete(item);
    }

    @Override
    public boolean isInWishlist(String userId, String productId) {  // Changed from UUID to String
        return wishlistItemRepository.existsByUserIdAndProductId(userId, productId);
    }

    @Override
    public void clearWishlist(String userId) {  // Changed from UUID to String
        wishlistItemRepository.deleteByUserId(userId);
    }

    private WishlistItemDTO toDTO(WishlistItem item) {
        return WishlistItemDTO.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .userId(item.getUserId())
                .createdAt(item.getCreatedAt() != null ? item.getCreatedAt().toString() : null)
                .product(item.getProduct() != null ? entityMapper.toProductDTO(item.getProduct()) : null)
                .build();
    }
}
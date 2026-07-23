// com.shopzen.ecommerce_api.service.cart.CartService.java
package com.shopzen.ecommerce_api.service.cart;

import java.util.List;
import java.util.UUID;

import com.shopzen.ecommerce_api.dto.product.CartItemDTO;
import com.shopzen.ecommerce_api.dto.product.CartSummaryDTO;

public interface CartService {
    List<CartItemDTO> getCartItems(String userId);
    CartItemDTO addToCart(String userId, String productId, Integer quantity, String color, String size);
    CartItemDTO updateQuantity(String userId, String itemId, Integer quantity);
    void removeFromCart(String userId, String itemId);
    void clearCart(String userId);
    CartSummaryDTO getCartSummary(String userId);
    boolean isInCart(String userId, String productId);
}
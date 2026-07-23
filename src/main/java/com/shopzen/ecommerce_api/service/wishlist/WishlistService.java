package com.shopzen.ecommerce_api.service.wishlist;

import java.util.List;

import com.shopzen.ecommerce_api.dto.wishlist.WishlistItemDTO;

public interface WishlistService {
    List<WishlistItemDTO> getWishlistItems(String userId);  // Changed from UUID to String
    WishlistItemDTO addToWishlist(String userId, String productId);  // Changed from UUID to String
    void removeFromWishlist(String userId, String productId);  // Changed from UUID to String
    boolean isInWishlist(String userId, String productId);  // Changed from UUID to String
    void clearWishlist(String userId);  // Changed from UUID to String
}
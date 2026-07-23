package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.dto.wishlist.WishlistItemDTO;
import com.shopzen.ecommerce_api.service.wishlist.WishlistService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist API", description = "Wishlist management endpoints")
public class WishlistController {

    private final WishlistService wishlistService;

    @GetMapping
    @Operation(summary = "Get current user's wishlist")
    public ResponseEntity<ApiResponseDTO<List<WishlistItemDTO>>> getWishlist() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        List<WishlistItemDTO> items = wishlistService.getWishlistItems(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(items));
    }

    @PostMapping
    @Operation(summary = "Add product to wishlist")
    public ResponseEntity<ApiResponseDTO<WishlistItemDTO>> addToWishlist(
            @RequestBody WishlistAddRequest request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        
        if (wishlistService.isInWishlist(userId, request.getProductId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponseDTO.error("Product already in wishlist"));
        }
        
        WishlistItemDTO item = wishlistService.addToWishlist(userId, request.getProductId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(item, "Added to wishlist"));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Remove product from wishlist")
    public ResponseEntity<ApiResponseDTO<Void>> removeFromWishlist(@PathVariable String productId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Removed from wishlist"));
    }

    @GetMapping("/check/{productId}")
    @Operation(summary = "Check if product is in wishlist")
    public ResponseEntity<ApiResponseDTO<WishlistCheckResponse>> isInWishlist(@PathVariable String productId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        boolean isInWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(ApiResponseDTO.success(
                WishlistCheckResponse.builder()
                        .productId(productId)
                        .isInWishlist(isInWishlist)
                        .build()
        ));
    }

    @DeleteMapping
    @Operation(summary = "Clear wishlist")
    public ResponseEntity<ApiResponseDTO<Void>> clearWishlist() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        wishlistService.clearWishlist(userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Wishlist cleared"));
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class WishlistAddRequest {
    private String productId;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class WishlistCheckResponse {
    private String productId;
    private boolean isInWishlist;
}
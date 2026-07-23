package com.shopzen.ecommerce_api.dto.review;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewDTO {
    private String id;
    private String userId;
    private String productId;
    private Integer rating;
    private String title;
    private String content;
    private List<String> images;
    private Boolean verifiedPurchase;
    private Integer helpfulCount;
    private Boolean isApproved;
    private String createdAt;
    private String updatedAt;
    
    private UserInfo user;
    
    private ProductInfo product;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private String id;
        private String fullName;
        private String firstName;
        private String lastName;
        private String avatarUrl;
        private String email;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductInfo {
        private String id;
        private String name;
        private String slug;
        private Double sellingPrice;
    }
}
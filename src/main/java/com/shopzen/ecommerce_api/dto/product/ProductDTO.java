package com.shopzen.ecommerce_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String categoryId;
    private String brandId;
    private Double originalPrice;
    private Double sellingPrice;
    private Integer stockQuantity;
    private List<String> images;
    private List<String> tags;
    private List<String> colors;
    private List<String> sizes;
    private Double rating;
    private Integer reviewCount;
    private Boolean isFeatured;
    private Boolean isTrending;
    private Boolean isBestseller;
    private Boolean isNewArrival;
    private Boolean isActive;
    private Boolean freeDelivery;
    private String createdAt;
    private String updatedAt;
    
    private String createdBy;     
    private String creatorName;   
    
    private CategorySummary category;
    private BrandSummary brand;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategorySummary {
        private String id;
        private String name;
        private String slug;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BrandSummary {
        private String id;
        private String name;
        private String slug;
    }
}
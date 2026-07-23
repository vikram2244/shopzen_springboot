package com.shopzen.ecommerce_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductFiltersDTO {
    private String search;
    private String categoryId;
    private String brandId;
    private Double minPrice;
    private Double maxPrice;
    private Double minRating;
    private Boolean isFeatured;
    private Boolean isTrending;
    private Boolean isNewArrival;
    private Boolean isBestseller;
    private Boolean inStock;
    private String sortBy;
}
package com.shopzen.ecommerce_api.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDTO {
 @NotBlank(message = "Product name is required")
 private String name;
 
 @NotBlank(message = "Product slug is required")
 private String slug;
 
 private String description;
 private String categoryId;
 private String brandId;
 
 @NotNull(message = "Original price is required")
 @Min(value = 0, message = "Original price must be greater than or equal to 0")
 private Double originalPrice;
 
 @NotNull(message = "Selling price is required")
 @Min(value = 0, message = "Selling price must be greater than or equal to 0")
 private Double sellingPrice;
 
 @Min(value = 0, message = "Stock quantity must be greater than or equal to 0")
 private Integer stockQuantity = 0;
 
 private String sku;
 private List<String> images;
 private List<String> tags;
 private List<String> colors;
 private List<String> sizes;
 private Boolean isFeatured = false;
 private Boolean isTrending = false;
 private Boolean isBestseller = false;
 private Boolean isNewArrival = false;
 private Boolean isActive = true;
 private Boolean freeDelivery = false;
 private Double weight;
 private String dimensions;
}

package com.shopzen.ecommerce_api.dto.product;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryRequestDTO {
    @NotBlank(message = "Category name is required")
    private String name;
    
    @NotBlank(message = "Category slug is required")
    private String slug;
    
    private String description;
    private String imageUrl;
    private String parentId;
    private Boolean isActive;
    private Integer sortOrder;
}

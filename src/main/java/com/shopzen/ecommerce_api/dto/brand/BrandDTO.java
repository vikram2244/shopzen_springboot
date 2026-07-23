package com.shopzen.ecommerce_api.dto.brand;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrandDTO {
    private String id;
    private String name;
    private String slug;
    private String description;
    private String logoUrl;
    private Boolean isActive;
    private String createdAt;
    private String updatedAt;
    private String createdBy;  
    private String creatorName; 
}
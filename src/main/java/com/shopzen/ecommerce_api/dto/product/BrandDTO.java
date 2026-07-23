package com.shopzen.ecommerce_api.dto.product;

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
 private String logoUrl;
 private String description;
 private Boolean isActive;
 private String createdAt;
 private String updatedAt;
}
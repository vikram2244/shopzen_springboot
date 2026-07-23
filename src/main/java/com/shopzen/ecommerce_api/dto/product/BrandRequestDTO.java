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
public class BrandRequestDTO {
 @NotBlank(message = "Brand name is required")
 private String name;
 
 @NotBlank(message = "Brand slug is required")
 private String slug;
 
 private String logoUrl;
 private String description;
 private Boolean isActive = true;
}

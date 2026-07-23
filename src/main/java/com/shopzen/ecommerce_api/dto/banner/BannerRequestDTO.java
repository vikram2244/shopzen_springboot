package com.shopzen.ecommerce_api.dto.banner;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerRequestDTO {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String subtitle;
    
    @NotBlank(message = "Image URL is required")
    private String imageUrl;
    
    private String link;
    
    private String badge;
    
    private Boolean isActive;
    
    private Integer sortOrder;
}
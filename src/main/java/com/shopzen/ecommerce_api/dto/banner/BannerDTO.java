package com.shopzen.ecommerce_api.dto.banner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BannerDTO {
    private String id;
    private String title;
    private String subtitle;
    private String imageUrl;
    private String link;
    private String badge;
    private Boolean isActive;
    private Integer sortOrder;
    private String createdAt;
    private String updatedAt;
    private String createdBy; 
    private String creatorName; 
}
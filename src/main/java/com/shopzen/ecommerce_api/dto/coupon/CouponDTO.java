package com.shopzen.ecommerce_api.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponDTO {
    private String id;
    private String code;
    private String description;
    private String discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private Double maxDiscount;
    private Integer usageLimit;
    private Integer usedCount;
    private Boolean isActive;
    private LocalDateTime expiresAt;
    private String createdAt;
    private String updatedAt;
    private String createdBy;
    
    private List<String> productIds;
    private List<ProductSummary> products;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductSummary {
        private String id;
        private String name;
        private String slug;
        private Double sellingPrice;
    }
}
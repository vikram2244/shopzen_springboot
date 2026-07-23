package com.shopzen.ecommerce_api.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequestDTO {
    
    private String code;
    
    private String description;
    
    private String discountType;
    
    private Double discountValue;
    
    private Double minOrderAmount;
    
    private Double maxDiscount;
    
    private Integer usageLimit;
    
    private Boolean isActive;
    
    private String expiresAt;
    
    private List<String> productIds;
}
package com.shopzen.ecommerce_api.dto.coupon;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CouponValidationResultDTO {
    private Boolean valid;
    private String message;
    private Double discountAmount;
    private CouponDTO coupon;
}
package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.dto.coupon.ApplyCouponRequestDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponValidationResultDTO;
import com.shopzen.ecommerce_api.service.coupon.CouponService;

import java.util.List;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon API", description = "Coupon management endpoints")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "Get all active coupons")
    public ResponseEntity<ApiResponseDTO<List<CouponDTO>>> getCoupons() {
        List<CouponDTO> coupons = couponService.getPublicActiveCoupons();
        return ResponseEntity.ok(ApiResponseDTO.success(coupons));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> getCouponByCode(@PathVariable String code) {
        CouponDTO coupon = couponService.getPublicCouponByCode(code);
        return ResponseEntity.ok(ApiResponseDTO.success(coupon));
    }

    @PostMapping("/validate")
    @Operation(summary = "Validate coupon")
    public ResponseEntity<ApiResponseDTO<CouponValidationResultDTO>> validateCoupon(
            @Valid @RequestBody ApplyCouponRequestDTO request) {
        CouponValidationResultDTO result = couponService.validatePublicCoupon(request);
        return ResponseEntity.ok(ApiResponseDTO.success(result));
    }
}
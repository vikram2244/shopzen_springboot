package com.shopzen.ecommerce_api.service.coupon;

import com.shopzen.ecommerce_api.dto.coupon.ApplyCouponRequestDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponRequestDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponValidationResultDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CouponService {
    List<CouponDTO> getPublicActiveCoupons();
    List<CouponDTO> getPublicActiveCouponsForProducts(List<String> productIds);
    CouponDTO getPublicCouponByCode(String code);
    CouponValidationResultDTO validatePublicCoupon(ApplyCouponRequestDTO request);
    List<CouponDTO> getActiveCoupons(String adminUserId);
    CouponDTO getCouponByCode(String adminUserId, String code);
    CouponValidationResultDTO validateCoupon(String adminUserId, ApplyCouponRequestDTO request);
    CouponDTO createCoupon(String adminUserId, CouponRequestDTO request);
    CouponDTO updateCoupon(String adminUserId, String id, CouponRequestDTO request);
    void deleteCoupon(String adminUserId, String id);
    CouponDTO toggleCouponStatus(String adminUserId, String id);
    Page<CouponDTO> getCouponsByAdmin(String adminUserId, Pageable pageable);
    Page<CouponDTO> getCouponsByAdminAndStatus(String adminUserId, Boolean isActive, Pageable pageable);
    Page<CouponDTO> searchCouponsByAdmin(String adminUserId, String search, Pageable pageable);
    boolean canEditCoupon(String userId, String couponId);
    CouponDTO getCouponById(String adminUserId, String couponId);
}
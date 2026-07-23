package com.shopzen.ecommerce_api.controller.admin;

import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponRequestDTO;
import com.shopzen.ecommerce_api.service.coupon.CouponService;
import com.shopzen.ecommerce_api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Coupon API", description = "Admin coupon management")
@Slf4j
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    @Operation(summary = "Get all coupons created by the current admin")
    public ResponseEntity<Page<CouponDTO>> getMyCoupons(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) Boolean isActive) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Fetching coupons for admin: {}", adminUserId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        if (isActive != null) {
            return ResponseEntity.ok(couponService.getCouponsByAdminAndStatus(adminUserId, isActive, pageable));
        }
        return ResponseEntity.ok(couponService.getCouponsByAdmin(adminUserId, pageable));
    }

    @GetMapping("/active")
    @Operation(summary = "Get all active coupons created by the current admin")
    public ResponseEntity<ApiResponseDTO<List<CouponDTO>>> getActiveCoupons() {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Fetching active coupons for admin: {}", adminUserId);
        List<CouponDTO> coupons = couponService.getActiveCoupons(adminUserId);
        return ResponseEntity.ok(ApiResponseDTO.success(coupons));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get coupon by ID")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> getCouponById(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} fetching coupon: {}", adminUserId, id);
        
        if (!couponService.canEditCoupon(adminUserId, id)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponseDTO.error("You are not authorized to view this coupon"));
        }
        
        Pageable pageable = PageRequest.of(0, 100);
        Page<CouponDTO> coupons = couponService.getCouponsByAdmin(adminUserId, pageable);
        CouponDTO found = coupons.stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        
        if (found == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponseDTO.error("Coupon not found"));
        }
        
        return ResponseEntity.ok(ApiResponseDTO.success(found));
    }

    @GetMapping("/code/{code}")
    @Operation(summary = "Get coupon by code")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> getCouponByCode(@PathVariable String code) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} fetching coupon by code: {}", adminUserId, code);
        CouponDTO coupon = couponService.getCouponByCode(adminUserId, code);
        return ResponseEntity.ok(ApiResponseDTO.success(coupon));
    }

    @PostMapping
    @Operation(summary = "Create coupon")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> createCoupon(@Valid @RequestBody CouponRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} creating coupon", adminUserId);
        CouponDTO coupon = couponService.createCoupon(adminUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(coupon, "Coupon created successfully"));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update coupon")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> updateCoupon(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody CouponRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} updating coupon: {}", adminUserId, id);
        CouponDTO coupon = couponService.updateCoupon(adminUserId, id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(coupon, "Coupon updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete coupon")
    public ResponseEntity<ApiResponseDTO<Void>> deleteCoupon(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} deleting coupon: {}", adminUserId, id);
        couponService.deleteCoupon(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Coupon deleted successfully"));
    }

    @PatchMapping("/{id}/toggle")
    @Operation(summary = "Toggle coupon active status")
    public ResponseEntity<ApiResponseDTO<CouponDTO>> toggleCouponStatus(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} toggling coupon status: {}", adminUserId, id);
        CouponDTO coupon = couponService.toggleCouponStatus(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(coupon, "Coupon status updated"));
    }

    @GetMapping("/search")
    @Operation(summary = "Search coupons by code or description")
    public ResponseEntity<Page<CouponDTO>> searchCoupons(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} searching coupons: {}", adminUserId, query);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CouponDTO> coupons = couponService.searchCouponsByAdmin(adminUserId, query, pageable);
        return ResponseEntity.ok(coupons);
    }
}
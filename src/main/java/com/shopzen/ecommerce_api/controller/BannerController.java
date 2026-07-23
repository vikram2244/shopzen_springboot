package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.banner.BannerDTO;
import com.shopzen.ecommerce_api.dto.banner.BannerRequestDTO;
import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.service.banner.BannerService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Banner API", description = "Banner management endpoints")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping("/banners")
    @Operation(summary = "Get all active banners")
    public ResponseEntity<ApiResponseDTO<List<BannerDTO>>> getActiveBanners() {
        List<BannerDTO> banners = bannerService.getActiveBanners();
        return ResponseEntity.ok(ApiResponseDTO.success(banners));
    }

    @GetMapping("/banners/{id}")
    @Operation(summary = "Get banner by ID")
    public ResponseEntity<ApiResponseDTO<BannerDTO>> getBannerById(@PathVariable String id) {  // Changed from UUID to String
        BannerDTO banner = bannerService.getBannerById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(banner));
    }
    
    @GetMapping("/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all banners (Admin only)")
    public ResponseEntity<ApiResponseDTO<List<BannerDTO>>> getAllBanners() {
        List<BannerDTO> banners = bannerService.getAllBanners();
        return ResponseEntity.ok(ApiResponseDTO.success(banners));
    }

    @PostMapping("/admin/banners")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create banner (Admin only)")
    public ResponseEntity<ApiResponseDTO<BannerDTO>> createBanner(@Valid @RequestBody BannerRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        BannerDTO banner = bannerService.createBanner(adminUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(banner, "Banner created successfully"));
    }

    @PutMapping("/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update banner (Admin only)")
    public ResponseEntity<ApiResponseDTO<BannerDTO>> updateBanner(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody BannerRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        BannerDTO banner = bannerService.updateBanner(adminUserId, id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(banner, "Banner updated successfully"));
    }

    @DeleteMapping("/admin/banners/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete banner (Admin only)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBanner(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        bannerService.deleteBanner(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Banner deleted successfully"));
    }

    @PatchMapping("/admin/banners/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle banner status (Admin only)")
    public ResponseEntity<ApiResponseDTO<BannerDTO>> toggleBannerStatus(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        BannerDTO banner = bannerService.toggleBannerStatus(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(banner, "Banner status updated"));
    }
}
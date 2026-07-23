package com.shopzen.ecommerce_api.controller;

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

import com.shopzen.ecommerce_api.dto.brand.BrandDTO;
import com.shopzen.ecommerce_api.dto.brand.BrandRequestDTO;
import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.service.brand.BrandService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brand API", description = "Brand management endpoints")
public class BrandController {

    private final BrandService brandService;

    @GetMapping("/brands")
    @Operation(summary = "Get all active brands")
    public ResponseEntity<ApiResponseDTO<List<BrandDTO>>> getActiveBrands() {
        List<BrandDTO> brands = brandService.getActiveBrands();
        return ResponseEntity.ok(ApiResponseDTO.success(brands));
    }

    @GetMapping("/brands/{id}")
    @Operation(summary = "Get brand by ID")
    public ResponseEntity<ApiResponseDTO<BrandDTO>> getBrandById(@PathVariable String id) {  // Changed from UUID to String
        BrandDTO brand = brandService.getBrandById(id);
        return ResponseEntity.ok(ApiResponseDTO.success(brand));
    }

    @GetMapping("/brands/slug/{slug}")
    @Operation(summary = "Get brand by slug")
    public ResponseEntity<ApiResponseDTO<BrandDTO>> getBrandBySlug(@PathVariable String slug) {
        BrandDTO brand = brandService.getBrandBySlug(slug);
        return ResponseEntity.ok(ApiResponseDTO.success(brand));
    }

    @GetMapping("/brands/search")
    @Operation(summary = "Search active brands")
    public ResponseEntity<ApiResponseDTO<Page<BrandDTO>>> searchActiveBrands(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name"));
        Page<BrandDTO> brands = brandService.searchActiveBrands(query, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(brands));
    }

    @GetMapping("/admin/brands")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all brands (Admin only)")
    public ResponseEntity<ApiResponseDTO<Page<BrandDTO>>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<BrandDTO> brands;
        if (search != null && !search.isEmpty()) {
            brands = brandService.searchAllBrands(search, pageable);
        } else {
            brands = brandService.getAllBrands(pageable);
        }
        return ResponseEntity.ok(ApiResponseDTO.success(brands));
    }

    @GetMapping("/admin/brands/my-brands")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get brands created by current admin")
    public ResponseEntity<ApiResponseDTO<Page<BrandDTO>>> getMyBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BrandDTO> brands = brandService.getBrandsByAdmin(adminUserId, pageable);
        return ResponseEntity.ok(ApiResponseDTO.success(brands));
    }

    @PostMapping("/admin/brands")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create brand (Admin only)")
    public ResponseEntity<ApiResponseDTO<BrandDTO>> createBrand(@Valid @RequestBody BrandRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} creating brand", adminUserId);
        BrandDTO brand = brandService.createBrand(adminUserId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(brand, "Brand created successfully"));
    }

    @PutMapping("/admin/brands/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update brand (Admin only)")
    public ResponseEntity<ApiResponseDTO<BrandDTO>> updateBrand(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody BrandRequestDTO request) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} updating brand: {}", adminUserId, id);
        BrandDTO brand = brandService.updateBrand(adminUserId, id, request);
        return ResponseEntity.ok(ApiResponseDTO.success(brand, "Brand updated successfully"));
    }

    @DeleteMapping("/admin/brands/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete brand (Admin only)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteBrand(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} deleting brand: {}", adminUserId, id);
        brandService.deleteBrand(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Brand deleted successfully"));
    }

    @PatchMapping("/admin/brands/{id}/toggle")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle brand active status (Admin only)")
    public ResponseEntity<ApiResponseDTO<BrandDTO>> toggleBrandStatus(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} toggling brand status: {}", adminUserId, id);
        BrandDTO brand = brandService.toggleBrandStatus(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(brand, "Brand status updated"));
    }

    @GetMapping("/admin/brands/{id}/can-edit")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Check if admin can edit brand")
    public ResponseEntity<ApiResponseDTO<Boolean>> canEditBrand(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        boolean canEdit = brandService.canEditBrand(adminUserId, id);
        return ResponseEntity.ok(ApiResponseDTO.success(canEdit));
    }
}
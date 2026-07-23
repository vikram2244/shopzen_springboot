package com.shopzen.ecommerce_api.service.brand;

import com.shopzen.ecommerce_api.dto.brand.BrandDTO;
import com.shopzen.ecommerce_api.dto.brand.BrandRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface BrandService {

    List<BrandDTO> getActiveBrands();
    
    BrandDTO getBrandBySlug(String slug);
    
    BrandDTO getBrandById(String id);
    
    Page<BrandDTO> getActiveBrands(Pageable pageable);
    
    Page<BrandDTO> searchActiveBrands(String search, Pageable pageable);
    
    BrandDTO createBrand(String adminUserId, BrandRequestDTO request);
    
    BrandDTO updateBrand(String adminUserId, String id, BrandRequestDTO request);
    
    void deleteBrand(String adminUserId, String id);
    
    BrandDTO toggleBrandStatus(String adminUserId, String id);
    
    Page<BrandDTO> getAllBrands(Pageable pageable);
    
    Page<BrandDTO> searchAllBrands(String search, Pageable pageable);
    
    Page<BrandDTO> getBrandsByAdmin(String adminUserId, Pageable pageable);
    
    boolean canEditBrand(String userId, String brandId);
}
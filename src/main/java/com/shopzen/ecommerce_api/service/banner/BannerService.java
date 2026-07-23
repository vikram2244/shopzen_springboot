package com.shopzen.ecommerce_api.service.banner;

import com.shopzen.ecommerce_api.dto.banner.BannerDTO;
import com.shopzen.ecommerce_api.dto.banner.BannerRequestDTO;

import java.util.List;
import java.util.UUID;

public interface BannerService {
    
    List<BannerDTO> getActiveBanners();
    
    BannerDTO getBannerById(String id);
    
    List<BannerDTO> getAllBanners();
    
    BannerDTO createBanner(String adminUserId, BannerRequestDTO request);
    
    BannerDTO updateBanner(String adminUserId, String id, BannerRequestDTO request);
    
    void deleteBanner(String adminUserId, String id);
    
    BannerDTO toggleBannerStatus(String adminUserId, String id);
}
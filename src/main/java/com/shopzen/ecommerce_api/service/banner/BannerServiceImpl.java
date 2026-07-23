package com.shopzen.ecommerce_api.service.banner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.banner.BannerDTO;
import com.shopzen.ecommerce_api.dto.banner.BannerRequestDTO;
import com.shopzen.ecommerce_api.entity.Banner;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.BannerRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BannerServiceImpl implements BannerService {

    private final BannerRepository bannerRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public List<BannerDTO> getActiveBanners() {
        log.debug("Fetching active banners");
        return bannerRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BannerDTO getBannerById(String id) {
        log.debug("Fetching banner by id: {}", id);
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
        return toDTO(banner);
    }

    @Override
    public List<BannerDTO> getAllBanners() {
        log.debug("Fetching all banners (Admin)");
        return bannerRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BannerDTO createBanner(String adminUserId, BannerRequestDTO request) {
        log.info("Creating new banner for admin: {}", adminUserId);
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminUserId));
        
        if (!admin.getIsAdmin()) {
            throw new UnauthorizedException("User is not an admin");
        }
        
        Banner banner = Banner.builder()
                .title(request.getTitle())
                .subtitle(request.getSubtitle())
                .imageUrl(request.getImageUrl())
                .link(request.getLink())
                .badge(request.getBadge())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .createdBy(adminUserId)
                .build();
        
        banner = bannerRepository.save(banner);
        log.info("Banner created with ID: {} by admin: {}", banner.getId(), adminUserId);
        return toDTO(banner);
    }

    @Override
    public BannerDTO updateBanner(String adminUserId, String id, BannerRequestDTO request) {
        log.info("Admin {} updating banner: {}", adminUserId, id);
        
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
        
        // 🔥 Optional: Only creator can update
        // if (banner.getCreatedBy() != null && !banner.getCreatedBy().equals(adminUserId)) {
        //     throw new UnauthorizedException("You are not authorized to update this banner");
        // }
        
        banner.setTitle(request.getTitle());
        banner.setSubtitle(request.getSubtitle());
        banner.setImageUrl(request.getImageUrl());
        banner.setLink(request.getLink());
        banner.setBadge(request.getBadge());
        if (request.getIsActive() != null) {
            banner.setIsActive(request.getIsActive());
        }
        if (request.getSortOrder() != null) {
            banner.setSortOrder(request.getSortOrder());
        }
        
        banner = bannerRepository.save(banner);
        log.info("Banner updated with ID: {}", banner.getId());
        return toDTO(banner);
    }

    @Override
    public void deleteBanner(String adminUserId, String id) {
        log.info("Admin {} deleting banner: {}", adminUserId, id);
        
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
        
//         🔥 Optional: Only creator can delete
         if (banner.getCreatedBy() != null && !banner.getCreatedBy().equals(adminUserId)) {
             throw new UnauthorizedException("You are not authorized to delete this banner");
         }
        
        bannerRepository.deleteById(id);
        log.info("Banner deleted with ID: {}", id);
    }

    @Override
    public BannerDTO toggleBannerStatus(String adminUserId, String id) {
        log.info("Admin {} toggling banner status: {}", adminUserId, id);
        
        Banner banner = bannerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Banner not found with id: " + id));
        
        // 🔥 Optional: Only creator can toggle
        // if (banner.getCreatedBy() != null && !banner.getCreatedBy().equals(adminUserId)) {
        //     throw new UnauthorizedException("You are not authorized to toggle this banner");
        // }
        
        banner.setIsActive(!banner.getIsActive());
        banner = bannerRepository.save(banner);
        log.info("Banner status toggled to: {}", banner.getIsActive());
        return toDTO(banner);
    }

    private BannerDTO toDTO(Banner banner) {
        String creatorName = null;
        if (banner.getCreatedBy() != null) {
            try {
                User creator = userRepository.findById(banner.getCreatedBy()).orElse(null);
                if (creator != null) {
                    creatorName = creator.getFullName();
                    if (creatorName == null || creatorName.isEmpty()) {
                        creatorName = creator.getFirstName() + " " + creator.getLastName();
                    }
                }
            } catch (Exception e) {
                log.warn("Could not fetch creator name: {}", e.getMessage());
            }
        }
        
        return BannerDTO.builder()
                .id(banner.getId().toString())
                .title(banner.getTitle())
                .subtitle(banner.getSubtitle())
                .imageUrl(banner.getImageUrl())
                .link(banner.getLink())
                .badge(banner.getBadge())
                .isActive(banner.getIsActive())
                .sortOrder(banner.getSortOrder())
                .createdAt(banner.getCreatedAt() != null ? banner.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(banner.getUpdatedAt() != null ? banner.getUpdatedAt().format(FORMATTER) : null)
                .createdBy(banner.getCreatedBy() != null ? banner.getCreatedBy().toString() : null)
                .creatorName(creatorName)
                .build();
    }
}
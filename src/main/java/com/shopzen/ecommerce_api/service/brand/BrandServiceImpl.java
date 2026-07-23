package com.shopzen.ecommerce_api.service.brand;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.brand.BrandDTO;
import com.shopzen.ecommerce_api.dto.brand.BrandRequestDTO;
import com.shopzen.ecommerce_api.entity.Brand;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.BrandRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BrandServiceImpl implements BrandService {

    private final BrandRepository brandRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public List<BrandDTO> getActiveBrands() {
        log.debug("Fetching all active brands");
        return brandRepository.findByIsActiveTrueOrderByNameAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public BrandDTO getBrandBySlug(String slug) {
        log.debug("Fetching brand by slug: {}", slug);
        Brand brand = brandRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with slug: " + slug));
        return toDTO(brand);
    }

    @Override
    public BrandDTO getBrandById(String id) {
        log.debug("Fetching brand by id: {}", id);
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
        return toDTO(brand);
    }

    @Override
    public Page<BrandDTO> getActiveBrands(Pageable pageable) {
        log.debug("Fetching active brands with pagination");
        return brandRepository.findByIsActiveTrue(pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<BrandDTO> searchActiveBrands(String search, Pageable pageable) {
        log.debug("Searching active brands: {}", search);
        return brandRepository.searchActiveBrands(search, pageable)
                .map(this::toDTO);
    }

    @Override
    public BrandDTO createBrand(String adminUserId, BrandRequestDTO request) {
        log.info("Creating new brand for admin: {}", adminUserId);
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminUserId));
        
        if (!admin.getIsAdmin()) {
            throw new UnauthorizedException("User is not an admin");
        }
        if (brandRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Brand slug already exists: " + request.getSlug());
        }
        
        if (brandRepository.existsByName(request.getName())) {
            throw new RuntimeException("Brand name already exists: " + request.getName());
        }

        String slug = generateSlug(request.getSlug());
        
        Brand brand = Brand.builder()
                .name(request.getName().trim())
                .slug(slug)
                .description(request.getDescription())
                .logoUrl(request.getLogoUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .createdBy(adminUserId)
                .build();

        brand = brandRepository.save(brand);
        log.info("Brand created with ID: {} by admin: {}", brand.getId(), adminUserId);
        return toDTO(brand);
    }

    @Override
    public BrandDTO updateBrand(String adminUserId, String id, BrandRequestDTO request) {
        log.info("Admin {} updating brand: {}", adminUserId, id);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        
        if (brand.getCreatedBy() == null || !brand.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to update brand {} created by {}", adminUserId, id, brand.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to update this brand. Only the creator can update it.");
        }

        String newSlug = generateSlug(request.getSlug());
        if (!brand.getSlug().equals(newSlug)) {
            if (brandRepository.existsBySlug(newSlug)) {
                throw new RuntimeException("Brand slug already exists: " + newSlug);
            }
        }
        
        if (!brand.getName().equals(request.getName())) {
            if (brandRepository.existsByName(request.getName())) {
                throw new RuntimeException("Brand name already exists: " + request.getName());
            }
        }

        brand.setName(request.getName().trim());
        brand.setSlug(newSlug);
        brand.setDescription(request.getDescription());
        brand.setLogoUrl(request.getLogoUrl());
        if (request.getIsActive() != null) {
            brand.setIsActive(request.getIsActive());
        }

        brand = brandRepository.save(brand);
        log.info("Brand updated with ID: {}", brand.getId());
        return toDTO(brand);
    }

    @Override
    public void deleteBrand(String adminUserId, String id) {
        log.info("Admin {} deleting brand: {}", adminUserId, id);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        
        if (brand.getCreatedBy() == null || !brand.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to delete brand {} created by {}", adminUserId, id, brand.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to delete this brand. Only the creator can delete it.");
        }
        
        brandRepository.deleteById(id);
        log.info("Brand deleted with ID: {}", id);
    }

    @Override
    public BrandDTO toggleBrandStatus(String adminUserId, String id) {
        log.info("Admin {} toggling brand status: {}", adminUserId, id);
        
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand not found with ID: " + id));
        
        if (brand.getCreatedBy() == null || !brand.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to toggle brand {} created by {}", adminUserId, id, brand.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to toggle this brand. Only the creator can toggle it.");
        }
        
        brand.setIsActive(!brand.getIsActive());
        brand = brandRepository.save(brand);
        log.info("Brand status toggled to: {}", brand.getIsActive());
        return toDTO(brand);
    }

    @Override
    public Page<BrandDTO> getAllBrands(Pageable pageable) {
        log.debug("Fetching all brands with pagination");
        return brandRepository.findAll(pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<BrandDTO> searchAllBrands(String search, Pageable pageable) {
        log.debug("Searching all brands: {}", search);
        return brandRepository.searchAllBrands(search, pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<BrandDTO> getBrandsByAdmin(String adminUserId, Pageable pageable) {
        log.debug("Fetching brands created by admin: {}", adminUserId);
        return brandRepository.findByCreatedBy(adminUserId, pageable)
                .map(this::toDTO);
    }

    @Override
    public boolean canEditBrand(String userId, String brandId) {
        if (userId == null || brandId == null) {
            return false;
        }
        try {
            Brand brand = brandRepository.findById(brandId).orElse(null);
            if (brand == null) return false;
            if (brand.getCreatedBy() == null) return false;
            return brand.getCreatedBy().equals(userId);
        } catch (Exception e) {
            log.error("Error checking edit permission: {}", e.getMessage());
            return false;
        }
    }


    /**
     * Generate a URL-friendly slug from the input string
     */
    private String generateSlug(String input) {
        if (input == null) return null;
        
        String slug = input.trim().toLowerCase();
        
        slug = slug.replaceAll("\\s+", "-");
        
        slug = slug.replaceAll("[^a-z0-9-]", "");
        
        slug = slug.replaceAll("-+", "-");
        
        slug = slug.replaceAll("^-|-$", "");
        
        return slug;
    }

    private BrandDTO toDTO(Brand brand) {
        if (brand == null) return null;
        
        String creatorName = null;
        if (brand.getCreatedBy() != null) {
            try {
                User creator = userRepository.findById(brand.getCreatedBy()).orElse(null);
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
        
        return BrandDTO.builder()
                .id(brand.getId().toString())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .logoUrl(brand.getLogoUrl())
                .isActive(brand.getIsActive())
                .createdAt(brand.getCreatedAt() != null ? brand.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(brand.getUpdatedAt() != null ? brand.getUpdatedAt().format(FORMATTER) : null)
                .createdBy(brand.getCreatedBy() != null ? brand.getCreatedBy().toString() : null)
                .creatorName(creatorName)
                .build();
    }
}
package com.shopzen.ecommerce_api.service.coupon;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.coupon.ApplyCouponRequestDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponRequestDTO;
import com.shopzen.ecommerce_api.dto.coupon.CouponValidationResultDTO;
import com.shopzen.ecommerce_api.entity.Coupon;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.CouponRepository;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public List<CouponDTO> getPublicActiveCoupons() {
        log.info("Fetching all public active coupons");
        try {
            return couponRepository.findByIsActiveTrue()
                    .stream()
                    .filter(Coupon::isValid)
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching public active coupons: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<CouponDTO> getPublicActiveCouponsForProducts(List<String> productIds) {
        log.info("Fetching public active coupons for products: {}", productIds);
        try {
            if (productIds == null || productIds.isEmpty()) {
                return List.of();
            }
            return couponRepository.findActiveCouponsForProducts(productIds, LocalDateTime.now())
                    .stream()
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching public active coupons for products: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public CouponDTO getPublicCouponByCode(String code) {
        log.info("Fetching public coupon by code: {}", code);
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        return toDTO(coupon);
    }

    @Override
    public CouponValidationResultDTO validatePublicCoupon(ApplyCouponRequestDTO request) {
        log.info("Validating public coupon: {} with subtotal: {}", request.getCode(), request.getSubtotal());
        
        String code = request.getCode().toUpperCase();
        BigDecimal subtotal = BigDecimal.valueOf(request.getSubtotal());

        try {
            Coupon coupon = couponRepository.findByCodeAndIsActiveTrue(code)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            if (!coupon.isValid()) {
                return CouponValidationResultDTO.builder()
                        .valid(false)
                        .message("Coupon is expired or no longer valid")
                        .discountAmount(0.0)
                        .build();
            }

            if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                return CouponValidationResultDTO.builder()
                        .valid(false)
                        .message("Minimum order amount of " + coupon.getMinOrderAmount() + " required")
                        .discountAmount(0.0)
                        .build();
            }

            BigDecimal discount = coupon.calculateDiscount(subtotal);

            return CouponValidationResultDTO.builder()
                    .valid(true)
                    .message("Coupon applied successfully")
                    .discountAmount(discount.doubleValue())
                    .coupon(toDTO(coupon))
                    .build();

        } catch (Exception e) {
            log.error("Error validating public coupon: {}", e.getMessage());
            return CouponValidationResultDTO.builder()
                    .valid(false)
                    .message("Invalid coupon code")
                    .discountAmount(0.0)
                    .build();
        }
    }

    @Override
    public List<CouponDTO> getActiveCoupons(String adminUserId) {
        log.info("Fetching all active coupons for admin: {}", adminUserId);
        try {
            return couponRepository.findByIsActiveTrueAndCreatedBy(adminUserId)
                    .stream()
                    .filter(Coupon::isValid)
                    .map(this::toDTO)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching active coupons: {}", e.getMessage());
            return List.of();
        }
    }

    @Override
    public CouponDTO getCouponByCode(String adminUserId, String code) {
        log.info("Fetching coupon by code: {} for admin: {}", code, adminUserId);
        Coupon coupon = couponRepository.findByCodeAndIsActiveTrueAndCreatedBy(code.toUpperCase(), adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + code));
        return toDTO(coupon);
    }

    @Override
    public CouponValidationResultDTO validateCoupon(String adminUserId, ApplyCouponRequestDTO request) {
        log.info("Validating coupon: {} for admin: {} with subtotal: {}", request.getCode(), adminUserId, request.getSubtotal());
        
        String code = request.getCode().toUpperCase();
        BigDecimal subtotal = BigDecimal.valueOf(request.getSubtotal());

        try {
            Coupon coupon = couponRepository.findByCodeAndIsActiveTrueAndCreatedBy(code, adminUserId)
                    .orElseThrow(() -> new RuntimeException("Coupon not found"));

            if (!coupon.isValid()) {
                return CouponValidationResultDTO.builder()
                        .valid(false)
                        .message("Coupon is expired or no longer valid")
                        .discountAmount(0.0)
                        .build();
            }

            if (subtotal.compareTo(coupon.getMinOrderAmount()) < 0) {
                return CouponValidationResultDTO.builder()
                        .valid(false)
                        .message("Minimum order amount of " + coupon.getMinOrderAmount() + " required")
                        .discountAmount(0.0)
                        .build();
            }

            BigDecimal discount = coupon.calculateDiscount(subtotal);

            return CouponValidationResultDTO.builder()
                    .valid(true)
                    .message("Coupon applied successfully")
                    .discountAmount(discount.doubleValue())
                    .coupon(toDTO(coupon))
                    .build();

        } catch (Exception e) {
            log.error("Error validating coupon: {}", e.getMessage());
            return CouponValidationResultDTO.builder()
                    .valid(false)
                    .message("Invalid coupon code")
                    .discountAmount(0.0)
                    .build();
        }
    }

    private LocalDateTime parseExpiresAt(String expiresAtStr) {
        if (expiresAtStr == null || expiresAtStr.isEmpty()) {
            return null;
        }
        
        try {
            return LocalDateTime.parse(expiresAtStr);
        } catch (DateTimeParseException e1) {
            try {
                LocalDate date = LocalDate.parse(expiresAtStr);
                return date.atTime(LocalTime.MAX);
            } catch (DateTimeParseException e2) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                    LocalDate date = LocalDate.parse(expiresAtStr, formatter);
                    return date.atTime(LocalTime.MAX);
                } catch (DateTimeParseException e3) {
                    log.warn("Could not parse expiresAt: {}, using null", expiresAtStr);
                    return null;
                }
            }
        }
    }

    @Override
    public CouponDTO createCoupon(String adminUserId, CouponRequestDTO request) {
        log.info("Creating new coupon for admin: {}", adminUserId);
        
        if (request == null) {
            throw new IllegalArgumentException("Coupon request cannot be null");
        }
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code is required");
        }
        
        if (request.getDiscountValue() == null || request.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0");
        }
        
        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with ID: " + adminUserId));
        
        if (!admin.getIsAdmin()) {
            throw new UnauthorizedException("User is not an admin");
        }
        
        if (couponRepository.findByCodeAndCreatedBy(request.getCode().toUpperCase(), adminUserId).isPresent()) {
            throw new RuntimeException("Coupon code already exists: " + request.getCode());
        }

        LocalDateTime expiresAt = parseExpiresAt(request.getExpiresAt());

        List<Product> products = new ArrayList<>();
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            for (String productIdStr : request.getProductIds()) {
                try {
                	String productId = productIdStr;
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
                    
                    if (product.getCreatedBy() == null || !product.getCreatedBy().equals(adminUserId)) {
                        log.warn("Product {} does not belong to admin {}", productId, adminUserId);
                        throw new UnauthorizedException("Product " + product.getName() + " does not belong to you");
                    }
                    products.add(product);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid product ID format: {}", productIdStr);
                }
            }
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription() != null ? request.getDescription() : null)
                .discountType(request.getDiscountType() != null ? request.getDiscountType() : "percentage")
                .discountValue(BigDecimal.valueOf(request.getDiscountValue()))
                .minOrderAmount(request.getMinOrderAmount() != null ? BigDecimal.valueOf(request.getMinOrderAmount()) : BigDecimal.ZERO)
                .maxDiscount(request.getMaxDiscount() != null ? BigDecimal.valueOf(request.getMaxDiscount()) : null)
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .expiresAt(expiresAt)
                .createdBy(adminUserId)
                .products(products)
                .build();

        coupon = couponRepository.save(coupon);
        log.info("Coupon created with ID: {} by admin: {} for {} products", 
            coupon.getId(), adminUserId, products.size());
        return toDTO(coupon);
    }

    @Override
    public CouponDTO updateCoupon(String adminUserId, String id, CouponRequestDTO request) {
        log.info("Admin {} updating coupon with ID: {}", adminUserId, id);
        
        if (request == null) {
            throw new IllegalArgumentException("Coupon request cannot be null");
        }
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Coupon code is required");
        }
        
        if (request.getDiscountValue() == null || request.getDiscountValue() <= 0) {
            throw new IllegalArgumentException("Discount value must be greater than 0");
        }
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));
        
        if (coupon.getCreatedBy() == null || !coupon.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to update coupon {} created by {}", adminUserId, id, coupon.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to update this coupon. Only the creator can update it.");
        }

        if (!coupon.getCode().equals(request.getCode().toUpperCase())) {
            if (couponRepository.findByCodeAndCreatedBy(request.getCode().toUpperCase(), adminUserId).isPresent()) {
                throw new RuntimeException("Coupon code already exists: " + request.getCode());
            }
        }

        LocalDateTime expiresAt = parseExpiresAt(request.getExpiresAt());

        List<Product> products = new ArrayList<>();
        if (request.getProductIds() != null && !request.getProductIds().isEmpty()) {
            for (String productIdStr : request.getProductIds()) {
                try {
                	String productId =productIdStr;
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
                    
                    if (product.getCreatedBy() == null || !product.getCreatedBy().equals(adminUserId)) {
                        log.warn("Product {} does not belong to admin {}", productId, adminUserId);
                        throw new UnauthorizedException("Product " + product.getName() + " does not belong to you");
                    }
                    products.add(product);
                } catch (IllegalArgumentException e) {
                    log.warn("Invalid product ID format: {}", productIdStr);
                }
            }
        }

        coupon.setCode(request.getCode().toUpperCase());
        coupon.setDescription(request.getDescription() != null ? request.getDescription() : null);
        coupon.setDiscountType(request.getDiscountType() != null ? request.getDiscountType() : "percentage");
        coupon.setDiscountValue(BigDecimal.valueOf(request.getDiscountValue()));
        coupon.setMinOrderAmount(request.getMinOrderAmount() != null ? BigDecimal.valueOf(request.getMinOrderAmount()) : BigDecimal.ZERO);
        coupon.setMaxDiscount(request.getMaxDiscount() != null ? BigDecimal.valueOf(request.getMaxDiscount()) : null);
        coupon.setUsageLimit(request.getUsageLimit());
        if (request.getIsActive() != null) {
            coupon.setIsActive(request.getIsActive());
        }
        coupon.setExpiresAt(expiresAt);
        coupon.setProducts(products);

        coupon = couponRepository.save(coupon);
        log.info("Coupon updated with ID: {}", coupon.getId());
        return toDTO(coupon);
    }

    @Override
    public void deleteCoupon(String adminUserId, String id) {
        log.info("Admin {} deleting coupon with ID: {}", adminUserId, id);
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));
        
        if (coupon.getCreatedBy() == null || !coupon.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to delete coupon {} created by {}", adminUserId, id, coupon.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to delete this coupon. Only the creator can delete it.");
        }
        
        couponRepository.deleteById(id);
        log.info("Coupon deleted with ID: {}", id);
    }

    @Override
    public CouponDTO toggleCouponStatus(String adminUserId, String id) {
        log.info("Admin {} toggling coupon status for ID: {}", adminUserId, id);
        
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + id));
        
        if (coupon.getCreatedBy() == null || !coupon.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to toggle coupon {} created by {}", adminUserId, id, coupon.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to toggle this coupon. Only the creator can toggle it.");
        }
        
        coupon.setIsActive(!coupon.getIsActive());
        coupon = couponRepository.save(coupon);
        log.info("Coupon status toggled to: {}", coupon.getIsActive());
        return toDTO(coupon);
    }

    @Override
    public Page<CouponDTO> getCouponsByAdmin(String adminUserId, Pageable pageable) {
        log.info("Fetching coupons created by admin: {}", adminUserId);
        return couponRepository.findByCreatedBy(adminUserId, pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<CouponDTO> getCouponsByAdminAndStatus(String adminUserId, Boolean isActive, Pageable pageable) {
        log.info("Fetching coupons for admin: {} with status: {}", adminUserId, isActive);
        return couponRepository.findByCreatedByAndIsActive(adminUserId, isActive, pageable)
                .map(this::toDTO);
    }

    @Override
    public Page<CouponDTO> searchCouponsByAdmin(String adminUserId, String search, Pageable pageable) {
        log.info("Searching coupons for admin: {} with search: {}", adminUserId, search);
        return couponRepository.searchByCreatedBy(adminUserId, search, pageable)
                .map(this::toDTO);
    }

    @Override
    public boolean canEditCoupon(String userId, String couponId) {
        if (userId == null || couponId == null) {
            return false;
        }
        try {
            Coupon coupon = couponRepository.findById(couponId).orElse(null);
            if (coupon == null) return false;
            if (coupon.getCreatedBy() == null) return false;
            return coupon.getCreatedBy().equals(userId);
        } catch (Exception e) {
            log.error("Error checking edit permission: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public CouponDTO getCouponById(String adminUserId, String couponId) {
        log.info("Admin {} fetching coupon by ID: {}", adminUserId, couponId);
        
        Coupon coupon = couponRepository.findById(couponId)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found with ID: " + couponId));
        
        if (coupon.getCreatedBy() == null || !coupon.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to view coupon {} created by {}", adminUserId, couponId, coupon.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to view this coupon. Only the creator can view it.");
        }
        
        return toDTO(coupon);
    }

    private CouponDTO toDTO(Coupon coupon) {
        if (coupon == null) return null;
        
        List<String> productIds = coupon.getProducts() != null 
            ? coupon.getProducts().stream()
                .map(p -> p.getId().toString())
                .collect(Collectors.toList())
            : List.of();
        
        List<CouponDTO.ProductSummary> productSummaries = coupon.getProducts() != null
            ? coupon.getProducts().stream()
                .map(p -> CouponDTO.ProductSummary.builder()
                    .id(p.getId().toString())
                    .name(p.getName())
                    .slug(p.getSlug())
                    .sellingPrice(p.getSellingPrice() != null ? p.getSellingPrice().doubleValue() : 0.0)
                    .build())
                .collect(Collectors.toList())
            : List.of();
        
        return CouponDTO.builder()
                .id(coupon.getId() != null ? coupon.getId().toString() : null)
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue() != null ? coupon.getDiscountValue().doubleValue() : 0.0)
                .minOrderAmount(coupon.getMinOrderAmount() != null ? coupon.getMinOrderAmount().doubleValue() : 0.0)
                .maxDiscount(coupon.getMaxDiscount() != null ? coupon.getMaxDiscount().doubleValue() : null)
                .usageLimit(coupon.getUsageLimit())
                .usedCount(coupon.getUsedCount() != null ? coupon.getUsedCount() : 0)
                .isActive(coupon.getIsActive() != null ? coupon.getIsActive() : true)
                .expiresAt(coupon.getExpiresAt())
                .createdAt(coupon.getCreatedAt() != null ? coupon.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(coupon.getUpdatedAt() != null ? coupon.getUpdatedAt().format(FORMATTER) : null)
                .createdBy(coupon.getCreatedBy() != null ? coupon.getCreatedBy().toString() : null)
                .productIds(productIds)
                .products(productSummaries)
                .build();
    }
}
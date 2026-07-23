package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Coupon;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, String> {
    
    Optional<Coupon> findByCodeAndIsActiveTrue(String code);
    Optional<Coupon> findByCode(String code);
    List<Coupon> findByIsActiveTrue();
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<Coupon> findActiveAndNotExpired(@Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND " +
           "(c.expiresAt IS NULL OR c.expiresAt > :now) AND " +
           "(c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAvailableCoupons(@Param("now") LocalDateTime now);
    
    boolean existsByCodeAndIsActiveTrue(String code);
    List<Coupon> findByExpiresAtBeforeAndIsActiveTrue(LocalDateTime date);
    long countByIsActiveTrue();
    List<Coupon> findByDiscountType(String discountType);
    
    @Query("SELECT c FROM Coupon c WHERE c.usageLimit IS NULL OR c.usedCount < c.usageLimit")
    List<Coupon> findCouponsWithAvailableUsage();
    
    @Query("SELECT c FROM Coupon c WHERE c.expiresAt BETWEEN :now AND :sevenDaysLater AND c.isActive = true")
    List<Coupon> findCouponsExpiringSoon(@Param("now") LocalDateTime now, 
                                         @Param("sevenDaysLater") LocalDateTime sevenDaysLater);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.isActive = true AND c.createdBy = :adminUserId")
    Optional<Coupon> findByCodeAndIsActiveTrueAndCreatedBy(@Param("code") String code, @Param("adminUserId") String adminUserId);
    
    @Query("SELECT c FROM Coupon c WHERE c.code = :code AND c.createdBy = :adminUserId")
    Optional<Coupon> findByCodeAndCreatedBy(@Param("code") String code, @Param("adminUserId") String adminUserId);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.createdBy = :adminUserId")
    List<Coupon> findByIsActiveTrueAndCreatedBy(@Param("adminUserId") String adminUserId);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.createdBy = :adminUserId AND (c.expiresAt IS NULL OR c.expiresAt > :now)")
    List<Coupon> findActiveAndNotExpiredByAdmin(@Param("adminUserId") String adminUserId, @Param("now") LocalDateTime now);
    
    @Query("SELECT c FROM Coupon c WHERE c.isActive = true AND c.createdBy = :adminUserId AND " +
           "(c.expiresAt IS NULL OR c.expiresAt > :now) AND " +
           "(c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findAvailableCouponsByAdmin(@Param("adminUserId") String adminUserId, @Param("now") LocalDateTime now);
    
    @Query("SELECT COUNT(c) > 0 FROM Coupon c WHERE c.code = :code AND c.isActive = true AND c.createdBy = :adminUserId")
    boolean existsByCodeAndIsActiveTrueAndCreatedBy(@Param("code") String code, @Param("adminUserId") String adminUserId);
    
    @Query("SELECT c FROM Coupon c WHERE c.createdBy = :adminUserId ORDER BY c.createdAt DESC")
    Page<Coupon> findByCreatedBy(@Param("adminUserId") String adminUserId, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.createdBy = :adminUserId AND c.isActive = :isActive ORDER BY c.createdAt DESC")
    Page<Coupon> findByCreatedByAndIsActive(@Param("adminUserId") String adminUserId, @Param("isActive") Boolean isActive, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.createdBy = :adminUserId AND (LOWER(c.code) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Coupon> searchByCreatedBy(@Param("adminUserId") String adminUserId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT c FROM Coupon c WHERE c.expiresAt IS NOT NULL AND c.expiresAt < :date AND c.isActive = true AND c.createdBy = :adminUserId")
    List<Coupon> findExpiredByAdmin(@Param("adminUserId") String adminUserId, @Param("date") LocalDateTime date);
    
    @Query("SELECT COUNT(c) FROM Coupon c WHERE c.isActive = true AND c.createdBy = :adminUserId")
    long countByIsActiveTrueAndCreatedBy(@Param("adminUserId") String adminUserId);
    
    List<Coupon> findByDiscountTypeAndCreatedBy(String discountType, String createdBy);
    boolean existsByIdAndCreatedBy(String id, String createdBy);
    
    @Query("SELECT DISTINCT c FROM Coupon c JOIN c.products p WHERE p.id IN :productIds AND c.isActive = true AND (c.expiresAt IS NULL OR c.expiresAt > :now) AND (c.usageLimit IS NULL OR c.usedCount < c.usageLimit)")
    List<Coupon> findActiveCouponsForProducts(@Param("productIds") List<String> productIds, @Param("now") LocalDateTime now);
}
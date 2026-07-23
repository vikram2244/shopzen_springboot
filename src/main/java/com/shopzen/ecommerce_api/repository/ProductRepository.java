package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    
    Optional<Product> findBySlug(String slug);
    Optional<Product> findByIdAndIsActiveTrue(String id);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    Page<Product> findAllActive(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.categoryId = :categoryId")
    Page<Product> findByCategory(@Param("categoryId") String categoryId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.brandId = :brandId")
    Page<Product> findByBrand(@Param("brandId") String brandId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.categoryId = :categoryId AND p.brandId = :brandId")
    Page<Product> findByCategoryAndBrand(@Param("categoryId") String categoryId, @Param("brandId") String brandId, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.categoryId = :categoryId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> findByCategoryAndSearch(@Param("categoryId") String categoryId, @Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(p.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Product> searchProducts(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.categoryId = :categoryId")
    List<Product> findByCategoryIdAndIsActiveTrue(@Param("categoryId") String categoryId);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isFeatured = true ORDER BY p.createdAt DESC")
    List<Product> findTop8ByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isTrending = true ORDER BY p.rating DESC NULLS LAST")
    List<Product> findTop8ByIsTrendingTrueAndIsActiveTrueOrderByRatingDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isNewArrival = true ORDER BY p.createdAt DESC")
    List<Product> findTop8ByIsNewArrivalTrueAndIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.isBestseller = true ORDER BY p.reviewCount DESC NULLS LAST")
    List<Product> findTop8ByIsBestsellerTrueAndIsActiveTrueOrderByReviewCountDesc(Pageable pageable);
    
    @Query("SELECT p FROM Product p WHERE p.isActive = true AND p.categoryId = :categoryId AND p.id != :productId")
    List<Product> findRelatedProducts(@Param("categoryId") String categoryId, @Param("productId") String productId, Pageable pageable);
    
    Page<Product> findByCreatedBy(String createdBy, Pageable pageable);
    
    boolean existsByIdAndCreatedBy(String id, String createdBy);
}
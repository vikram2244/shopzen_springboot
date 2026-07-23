package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Brand;
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
public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);
    
    List<Brand> findByIsActiveTrueOrderByNameAsc();
    
    Page<Brand> findByIsActiveTrue(Pageable pageable);
    
    boolean existsBySlug(String slug);
    
    boolean existsByName(String name);
    
    Page<Brand> findAll(Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND (LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.slug) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Brand> searchActiveBrands(@Param("search") String search, Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(b.slug) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Brand> searchAllBrands(@Param("search") String search, Pageable pageable);

    @Query("SELECT b FROM Brand b WHERE b.createdBy = :adminUserId")
    Page<Brand> findByCreatedBy(@Param("adminUserId") String adminUserId, Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE b.isActive = true AND b.createdBy = :adminUserId")
    List<Brand> findByIsActiveTrueAndCreatedBy(@Param("adminUserId") String adminUserId);
    
    Optional<Brand> findByIdAndCreatedBy(String id, String createdBy);
    
    boolean existsByIdAndCreatedBy(String id, String createdBy);
}
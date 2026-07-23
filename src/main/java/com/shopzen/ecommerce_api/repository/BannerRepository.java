package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Banner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BannerRepository extends JpaRepository<Banner, String> {
    
    List<Banner> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Banner> findAllByOrderBySortOrderAsc();
    
    @Query("SELECT b FROM Banner b WHERE b.createdBy = :adminUserId ORDER BY b.sortOrder ASC")
    List<Banner> findByCreatedBy(@Param("adminUserId") String adminUserId);
    
    @Query("SELECT b FROM Banner b WHERE b.isActive = true AND b.createdBy = :adminUserId ORDER BY b.sortOrder ASC")
    List<Banner> findActiveByCreatedBy(@Param("adminUserId") String adminUserId);
}
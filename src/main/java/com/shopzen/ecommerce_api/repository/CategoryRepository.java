// com.shopzen.ecommerce_api.repository.CategoryRepository.java
package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, String> {
    
    Optional<Category> findBySlug(String slug);
    boolean existsBySlug(String slug);
    boolean existsByParentId(String parentId);
    
    List<Category> findByIsActiveTrueOrderBySortOrderAsc();
    
    List<Category> findAllByOrderBySortOrderAsc();
    
    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrderAsc(String parentId);
    List<Category> findByParentIdOrderBySortOrderAsc(String parentId);
    
    @Query("SELECT MAX(c.sortOrder) FROM Category c")
    Optional<Integer> findMaxSortOrder();
    
    List<Category> findByParentIdIsNullAndIsActiveTrueOrderBySortOrderAsc();
    
    List<Category> findByNameContainingIgnoreCaseAndIsActiveTrue(String name);
}
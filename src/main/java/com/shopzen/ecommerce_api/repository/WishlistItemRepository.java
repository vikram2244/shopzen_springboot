package com.shopzen.ecommerce_api.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.shopzen.ecommerce_api.entity.WishlistItem;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, String> {  // Changed from UUID to String
    
    List<WishlistItem> findByUserIdOrderByCreatedAtDesc(String userId);  // Changed from UUID to String
    
    Optional<WishlistItem> findByUserIdAndProductId(String userId, String productId);  // Changed from UUID to String
    
    boolean existsByUserIdAndProductId(String userId, String productId);  // Changed from UUID to String
    
    void deleteByUserId(String userId);  // Changed from UUID to String
}
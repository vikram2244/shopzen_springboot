package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, String> {  // Changed from UUID to String
    
    List<Review> findByProductIdOrderByCreatedAtDesc(String productId);  // Changed from UUID to String
    
    List<Review> findByProductId(String productId);  // Changed from UUID to String
    
    Page<Review> findByProductId(String productId, Pageable pageable);  // Changed from UUID to String
    
    List<Review> findByUserIdOrderByCreatedAtDesc(String userId);  // Changed from UUID to String
    
    Page<Review> findByUserId(String userId, Pageable pageable);  // Changed from UUID to String
    
    Optional<Review> findByIdAndUserId(String id, String userId);  // Changed from UUID to String
    
    Optional<Review> findByProductIdAndUserId(String productId, String userId);  // Changed from UUID to String
    
    boolean existsByUserIdAndProductId(String userId, String productId);  // Changed from UUID to String
    
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingByProductId(@Param("productId") String productId);  // Changed from UUID to String
    
    @Query("SELECT COALESCE(AVG(r.rating), 0.0) FROM Review r WHERE r.productId = :productId")
    Double getAverageRatingOrDefault(@Param("productId") String productId);  // Changed from UUID to String
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId")
    Long countByProductId(@Param("productId") String productId);  // Changed from UUID to String
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.rating = :rating")
    Long countByProductIdAndRating(@Param("productId") String productId, @Param("rating") Integer rating);  // Changed from UUID to String
    
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.productId = :productId GROUP BY r.rating ORDER BY r.rating DESC")
    List<Object[]> getRatingDistribution(@Param("productId") String productId);  // Changed from UUID to String
    
    @Query("SELECT COUNT(r) FROM Review r")
    Long countTotalReviews();
    
    @Modifying
    @Transactional
    @Query("UPDATE Review r SET r.helpfulCount = r.helpfulCount + 1 WHERE r.id = :reviewId")
    void incrementHelpfulCount(@Param("reviewId") String reviewId);  // Changed from UUID to String
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Review r WHERE r.productId = :productId")
    void deleteAllByProductId(@Param("productId") String productId);  // Changed from UUID to String
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Review r WHERE r.userId = :userId")
    void deleteAllByUserId(@Param("userId") String userId);  // Changed from UUID to String
    
    @Query("SELECT COUNT(r) FROM Review r WHERE r.productId = :productId AND r.verifiedPurchase = true")
    Long countVerifiedReviewsByProductId(@Param("productId") String productId);  // Changed from UUID to String
    
    List<Review> findByProductIdAndVerifiedPurchaseTrueOrderByCreatedAtDesc(String productId);  // Changed from UUID to String
    
    List<Review> findAllByOrderByCreatedAtDesc();
    
    Page<Review> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
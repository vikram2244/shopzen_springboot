package com.shopzen.ecommerce_api.service.review;

import com.shopzen.ecommerce_api.dto.review.ReviewDTO;
import com.shopzen.ecommerce_api.dto.review.ReviewRequestDTO;

import java.util.List;

public interface ReviewService {
    
    List<ReviewDTO> getReviewsByProductId(String productId);  // Changed from UUID to String
    
    ReviewDTO createReview(String userId, ReviewRequestDTO request);  // Changed from UUID to String
    
    ReviewDTO updateReview(String id, String userId, ReviewRequestDTO request);  // Changed from UUID to String
    
    void deleteReview(String id, String userId);  // Changed from UUID to String
    
    List<ReviewDTO> getAllReviews();
    
    void deleteReviewAdmin(String id);  // Changed from UUID to String
    
    ReviewDTO getReviewById(String id);  // Changed from UUID to String
    
    List<ReviewDTO> getReviewsByUser(String userId);  // Changed from UUID to String
    
    List<ReviewDTO> getReviewsByProductIdWithPagination(String productId, int page, int size);  // Changed from UUID to String
}
package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.common.ApiResponseDTO;
import com.shopzen.ecommerce_api.dto.review.ReviewDTO;
import com.shopzen.ecommerce_api.dto.review.ReviewRequestDTO;
import com.shopzen.ecommerce_api.service.review.ReviewService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Review API", description = "Review management endpoints")
public class ReviewController {

    private final ReviewService reviewService;
    
    @GetMapping("/reviews/product/{productId}")
    @Operation(summary = "Get reviews for a product")
    public ResponseEntity<ApiResponseDTO<List<ReviewDTO>>> getProductReviews(@PathVariable String productId) {  // Changed from UUID to String
        List<ReviewDTO> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(ApiResponseDTO.success(reviews));
    }

    @PostMapping("/reviews")
    @Operation(summary = "Create a review")
    public ResponseEntity<ApiResponseDTO<ReviewDTO>> createReview(@Valid @RequestBody ReviewRequestDTO request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        ReviewDTO review = reviewService.createReview(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponseDTO.success(review, "Review created successfully"));
    }

    @PutMapping("/reviews/{id}")
    @Operation(summary = "Update a review")
    public ResponseEntity<ApiResponseDTO<ReviewDTO>> updateReview(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody ReviewRequestDTO request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        ReviewDTO review = reviewService.updateReview(id, userId, request);
        return ResponseEntity.ok(ApiResponseDTO.success(review, "Review updated successfully"));
    }

    @DeleteMapping("/reviews/{id}")
    @Operation(summary = "Delete a review")
    public ResponseEntity<ApiResponseDTO<Void>> deleteReview(@PathVariable String id) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        reviewService.deleteReview(id, userId);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Review deleted successfully"));
    }
    
    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all reviews (Admin only)")
    public ResponseEntity<ApiResponseDTO<List<ReviewDTO>>> getAllReviews() {
        List<ReviewDTO> reviews = reviewService.getAllReviews();
        return ResponseEntity.ok(ApiResponseDTO.success(reviews));
    }

    @DeleteMapping("/admin/reviews/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete review (Admin only)")
    public ResponseEntity<ApiResponseDTO<Void>> deleteReviewAdmin(@PathVariable String id) {  // Changed from UUID to String
        reviewService.deleteReviewAdmin(id);
        return ResponseEntity.ok(ApiResponseDTO.success(null, "Review deleted successfully"));
    }
}
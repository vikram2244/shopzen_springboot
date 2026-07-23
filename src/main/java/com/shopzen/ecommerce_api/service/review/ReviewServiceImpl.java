package com.shopzen.ecommerce_api.service.review;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.review.ReviewDTO;
import com.shopzen.ecommerce_api.dto.review.ReviewRequestDTO;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.entity.Review;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.repository.ReviewRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public List<ReviewDTO> getReviewsByProductId(String productId) {  // Changed from UUID to String
        log.debug("Fetching reviews for product: {}", productId);
        
        if (!productRepository.existsById(productId)) {
            log.warn("Product not found: {}", productId);
            return List.of();
        }
        
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ReviewDTO createReview(String userId, ReviewRequestDTO request) {  // Changed from UUID to String
        log.info("Creating review for user: {} on product: {}", userId, request.getProductId());
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        
        String productId = request.getProductId();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + productId));

        if (reviewRepository.existsByUserIdAndProductId(userId, productId)) {
            throw new RuntimeException("You have already reviewed this product");
        }

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        Review review = Review.builder()
                .userId(userId)
                .productId(productId)
                .rating(request.getRating())
                .title(request.getTitle() != null ? request.getTitle() : "")
                .content(request.getContent())
                .images(request.getImages() != null ? request.getImages() : List.of())
                .verifiedPurchase(false)
                .helpfulCount(0)
                .isApproved(true)
                .build();

        review = reviewRepository.save(review);
        log.info("Review created with id: {}", review.getId());

        updateProductRatingAndCount(product);

        return toDTO(review);
    }

    @Override
    public ReviewDTO updateReview(String id, String userId, ReviewRequestDTO request) {  // Changed from UUID to String
        log.info("Updating review: {} for user: {}", id, userId);
        
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or you don't have permission to update it"));

        if (request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("Rating must be between 1 and 5");
        }

        review.setRating(request.getRating());
        review.setTitle(request.getTitle() != null ? request.getTitle() : "");
        review.setContent(request.getContent());
        if (request.getImages() != null) {
            review.setImages(request.getImages());
        }

        review = reviewRepository.save(review);
        log.info("Review updated: {}", id);

        Product product = productRepository.findById(review.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductRatingAndCount(product);

        return toDTO(review);
    }

    @Override
    public void deleteReview(String id, String userId) {  // Changed from UUID to String
        log.info("Deleting review: {} for user: {}", id, userId);
        
        Review review = reviewRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found or you don't have permission to delete it"));
        
        String productId = review.getProductId();
        reviewRepository.delete(review);
        log.info("Review deleted: {}", id);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductRatingAndCount(product);
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        log.debug("Fetching all reviews (Admin)");
        return reviewRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteReviewAdmin(String id) {  // Changed from UUID to String
        log.info("Admin deleting review: {}", id);
        
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        
        String productId = review.getProductId();
        reviewRepository.delete(review);
        log.info("Review deleted by admin: {}", id);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        updateProductRatingAndCount(product);
    }

    @Override
    public ReviewDTO getReviewById(String id) {  // Changed from UUID to String
        log.debug("Fetching review by id: {}", id);
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Review not found with id: " + id));
        return toDTO(review);
    }

    @Override
    public List<ReviewDTO> getReviewsByUser(String userId) {  // Changed from UUID to String
        log.debug("Fetching reviews for user: {}", userId);
        return reviewRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewDTO> getReviewsByProductIdWithPagination(String productId, int page, int size) {  // Changed from UUID to String
        log.debug("Fetching paginated reviews for product: {}", productId);
        return reviewRepository.findByProductIdOrderByCreatedAtDesc(productId)
                .stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private void updateProductRatingAndCount(Product product) {
        List<Review> reviews = reviewRepository.findByProductId(product.getId());
        
        if (reviews.isEmpty()) {
            product.setRating(BigDecimal.ZERO);
            product.setReviewCount(0);
            log.debug("Product {} has no reviews, rating set to 0", product.getId());
        } else {
            double avgRating = reviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            
            product.setRating(BigDecimal.valueOf(avgRating).setScale(2, RoundingMode.HALF_UP));
            product.setReviewCount(reviews.size());
            
            log.debug("Product {} updated: rating={}, reviewCount={}", 
                product.getId(), product.getRating(), product.getReviewCount());
        }
        
        productRepository.save(product);
    }

    private ReviewDTO toDTO(Review review) {
        if (review == null) return null;
        
        ReviewDTO dto = ReviewDTO.builder()
                .id(review.getId())
                .userId(review.getUserId())
                .productId(review.getProductId())
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .images(review.getImages())
                .verifiedPurchase(review.getVerifiedPurchase())
                .helpfulCount(review.getHelpfulCount())
                .isApproved(review.getIsApproved())
                .createdAt(review.getCreatedAt() != null ? review.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(review.getUpdatedAt() != null ? review.getUpdatedAt().format(FORMATTER) : null)
                .build();

        userRepository.findById(review.getUserId()).ifPresent(user -> {
            String fullName = user.getFullName();
            if (fullName == null || fullName.isEmpty()) {
                fullName = (user.getFirstName() + " " + user.getLastName()).trim();
            }
            
            ReviewDTO.UserInfo userInfo = ReviewDTO.UserInfo.builder()
                    .id(user.getId())
                    .fullName(fullName)
                    .avatarUrl(user.getProfileImage())
                    .build();
            dto.setUser(userInfo);
        });

        productRepository.findById(review.getProductId()).ifPresent(product -> {
            ReviewDTO.ProductInfo productInfo = ReviewDTO.ProductInfo.builder()
                    .id(product.getId())
                    .name(product.getName())
                    .slug(product.getSlug())
                    .build();
            dto.setProduct(productInfo);
        });

        return dto;
    }
}
package com.shopzen.ecommerce_api.service.product;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.dto.product.ProductFiltersDTO;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.entity.Category;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.repository.ReviewRepository;
import com.shopzen.ecommerce_api.repository.CategoryRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;
import com.shopzen.ecommerce_api.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ReviewRepository reviewRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final EntityMapper entityMapper;

    @Override
    public ProductDTO getProductBySlug(String slug) {
        log.info("Getting product by slug: {}", slug);
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        
        ProductDTO dto = entityMapper.toProductDTO(product);
        dto = enrichWithCategory(dto, product.getCategoryId());
        dto = enrichWithReviewData(dto, product.getId());
        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
        return dto;
    }

    @Override
    public ProductDTO getProductById(String id) {  // Changed from UUID to String
        log.info("Getting product by ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        
        ProductDTO dto = entityMapper.toProductDTO(product);
        dto = enrichWithCategory(dto, product.getCategoryId());
        dto = enrichWithReviewData(dto, product.getId());
        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
        return dto;
    }

    @Override
    public Page<ProductDTO> getProducts(ProductFiltersDTO filters, Pageable pageable) {
        String categoryId = filters.getCategoryId();  // Already String
        String brandId = filters.getBrandId();  // Already String
        
        String search = filters.getSearch() != null ? filters.getSearch() : null;
        
        Page<Product> products;
        
        if (categoryId != null && brandId != null) {
            log.info("Filtering by category: {} and brand: {}", categoryId, brandId);
            products = productRepository.findByCategoryAndBrand(categoryId, brandId, pageable);
        } else if (categoryId != null && search != null && !search.isEmpty()) {
            log.info("Filtering by category: {} and search: {}", categoryId, search);
            products = productRepository.findByCategoryAndSearch(categoryId, search, pageable);
        } else if (categoryId != null) {
            log.info("Filtering by category: {}", categoryId);
            products = productRepository.findByCategory(categoryId, pageable);
        } else if (brandId != null) {
            log.info("Filtering by brand: {}", brandId);
            products = productRepository.findByBrand(brandId, pageable);
        } else if (search != null && !search.isEmpty()) {
            log.info("Searching for: {}", search);
            products = productRepository.searchProducts(search, pageable);
        } else {
            log.info("Fetching all active products");
            products = productRepository.findAllActive(pageable);
        }
        
        return products.map(product -> {
            ProductDTO dto = entityMapper.toProductDTO(product);
            dto = enrichWithCategory(dto, product.getCategoryId());
            dto = enrichWithReviewData(dto, product.getId());
            dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
            return dto;
        });
    }

    @Override
    public List<ProductDTO> getProductsByCategory(String categoryId) {  // Changed from UUID to String
        try {
            List<Product> products = productRepository.findByCategoryIdAndIsActiveTrue(categoryId);
            log.info("Found {} products for category {}", products.size(), categoryId);
            
            if (products.isEmpty()) {
                return Collections.emptyList();
            }
            
            return products.stream()
                    .map(product -> {
                        try {
                            ProductDTO dto = entityMapper.toProductDTO(product);
                            dto = enrichWithCategory(dto, product.getCategoryId());
                            dto = enrichWithReviewData(dto, product.getId());
                            dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
                            return dto;
                        } catch (Exception e) {
                            log.error("Error mapping product {}: {}", product.getId(), e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching products for category {}: {}", categoryId, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getFeaturedProducts() {
        try {
            Pageable limit = PageRequest.of(0, 8);
            return productRepository.findTop8ByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc(limit)
                    .stream()
                    .map(product -> {
                        ProductDTO dto = entityMapper.toProductDTO(product);
                        dto = enrichWithCategory(dto, product.getCategoryId());
                        dto = enrichWithReviewData(dto, product.getId());
                        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching featured products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getTrendingProducts() {
        try {
            Pageable limit = PageRequest.of(0, 8);
            return productRepository.findTop8ByIsTrendingTrueAndIsActiveTrueOrderByRatingDesc(limit)
                    .stream()
                    .map(product -> {
                        ProductDTO dto = entityMapper.toProductDTO(product);
                        dto = enrichWithCategory(dto, product.getCategoryId());
                        dto = enrichWithReviewData(dto, product.getId());
                        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching trending products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getNewArrivals() {
        try {
            Pageable limit = PageRequest.of(0, 8);
            return productRepository.findTop8ByIsNewArrivalTrueAndIsActiveTrueOrderByCreatedAtDesc(limit)
                    .stream()
                    .map(product -> {
                        ProductDTO dto = entityMapper.toProductDTO(product);
                        dto = enrichWithCategory(dto, product.getCategoryId());
                        dto = enrichWithReviewData(dto, product.getId());
                        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching new arrivals: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getBestSellers() {
        try {
            Pageable limit = PageRequest.of(0, 8);
            return productRepository.findTop8ByIsBestsellerTrueAndIsActiveTrueOrderByReviewCountDesc(limit)
                    .stream()
                    .map(product -> {
                        ProductDTO dto = entityMapper.toProductDTO(product);
                        dto = enrichWithCategory(dto, product.getCategoryId());
                        dto = enrichWithReviewData(dto, product.getId());
                        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching best sellers: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public List<ProductDTO> getRelatedProducts(String productId) {  // Changed from UUID to String
        try {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            
            Pageable limit = PageRequest.of(0, 4);
            return productRepository.findRelatedProducts(product.getCategoryId(), productId, limit)
                    .stream()
                    .map(p -> {
                        ProductDTO dto = entityMapper.toProductDTO(p);
                        dto = enrichWithCategory(dto, p.getCategoryId());
                        dto = enrichWithReviewData(dto, p.getId());
                        dto = enrichWithCreatorInfo(dto, p.getCreatedBy());
                        return dto;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error fetching related products: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    @Deprecated
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.warn("Deprecated createProduct called without admin user ID");
        return createProduct(null, productDTO);
    }

    @Override
    @Deprecated
    public ProductDTO updateProduct(String id, ProductDTO productDTO) {  // Changed from UUID to String
        log.warn("Deprecated updateProduct called without admin user ID");
        return updateProduct(null, id, productDTO);
    }

    @Override
    @Deprecated
    public void deleteProduct(String id) {  // Changed from UUID to String
        log.warn("Deprecated deleteProduct called without admin user ID");
        deleteProduct(null, id);
    }

    @Override
    public ProductDTO createProduct(String adminUserId, ProductDTO productDTO) {  // Changed from UUID to String
        log.info("Creating product by admin: {}", adminUserId);
        
        Product product = entityMapper.toProduct(productDTO);
        
        // Set default values
        if (product.getIsActive() == null) product.setIsActive(true);
        if (product.getIsFeatured() == null) product.setIsFeatured(false);
        if (product.getIsTrending() == null) product.setIsTrending(false);
        if (product.getIsBestseller() == null) product.setIsBestseller(false);
        if (product.getIsNewArrival() == null) product.setIsNewArrival(false);
        if (product.getFreeDelivery() == null) product.setFreeDelivery(false);
        if (product.getStockQuantity() == null) product.setStockQuantity(0);
        if (product.getRating() == null) product.setRating(BigDecimal.ZERO);
        if (product.getReviewCount() == null) product.setReviewCount(0);
        if (adminUserId != null) {
            product.setCreatedBy(adminUserId);
        }
        
        product = productRepository.save(product);
        log.info("Product created with ID: {} by admin: {}", product.getId(), adminUserId);
        
        ProductDTO dto = entityMapper.toProductDTO(product);
        dto = enrichWithCategory(dto, product.getCategoryId());
        dto = enrichWithReviewData(dto, product.getId());
        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
        return dto;
    }

    @Override
    public ProductDTO updateProduct(String adminUserId, String productId, ProductDTO productDTO) {  // Changed from UUID to String
        log.info("Admin {} updating product: {}", adminUserId, productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (adminUserId != null && product.getCreatedBy() != null && !product.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to edit product {} created by {}", adminUserId, productId, product.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to edit this product. Only the creator can edit it.");
        }
        
        product.setName(productDTO.getName());
        product.setSlug(productDTO.getSlug());
        product.setDescription(productDTO.getDescription());
        product.setOriginalPrice(BigDecimal.valueOf(productDTO.getOriginalPrice()));
        product.setSellingPrice(BigDecimal.valueOf(productDTO.getSellingPrice()));
        product.setStockQuantity(productDTO.getStockQuantity() != null ? productDTO.getStockQuantity() : 0);
        product.setImages(productDTO.getImages());
        product.setColors(productDTO.getColors());
        product.setSizes(productDTO.getSizes());
        product.setIsFeatured(productDTO.getIsFeatured() != null ? productDTO.getIsFeatured() : false);
        product.setIsTrending(productDTO.getIsTrending() != null ? productDTO.getIsTrending() : false);
        product.setIsBestseller(productDTO.getIsBestseller() != null ? productDTO.getIsBestseller() : false);
        product.setIsNewArrival(productDTO.getIsNewArrival() != null ? productDTO.getIsNewArrival() : false);
        product.setFreeDelivery(productDTO.getFreeDelivery() != null ? productDTO.getFreeDelivery() : false);
        product.setIsActive(productDTO.getIsActive() != null ? productDTO.getIsActive() : true);
        
        if (productDTO.getRating() != null) {
            product.setRating(BigDecimal.valueOf(productDTO.getRating()));
        }
        
        product = productRepository.save(product);
        log.info("Product {} updated by admin: {}", productId, adminUserId);
        
        ProductDTO dto = entityMapper.toProductDTO(product);
        dto = enrichWithCategory(dto, product.getCategoryId());
        dto = enrichWithReviewData(dto, product.getId());
        dto = enrichWithCreatorInfo(dto, product.getCreatedBy());
        return dto;
    }

    @Override
    public void deleteProduct(String adminUserId, String productId) {  // Changed from UUID to String
        log.info("Admin {} deleting product: {}", adminUserId, productId);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        
        if (adminUserId != null && product.getCreatedBy() != null && !product.getCreatedBy().equals(adminUserId)) {
            log.warn("Admin {} tried to delete product {} created by {}", adminUserId, productId, product.getCreatedBy());
            throw new UnauthorizedException("You are not authorized to delete this product. Only the creator can delete it.");
        }
        
        product.setIsActive(false);
        productRepository.save(product);
        log.info("Product {} deactivated by admin: {}", productId, adminUserId);
    }

    @Override
    public Page<ProductDTO> getProductsByAdmin(String adminUserId, Pageable pageable) {  // Changed from UUID to String
        log.info("Fetching products created by admin: {}", adminUserId);
        return productRepository.findByCreatedBy(adminUserId, pageable)
                .map(product -> {
                    ProductDTO dto = entityMapper.toProductDTO(product);
                    dto = enrichWithCategory(dto, product.getCategoryId());
                    dto = enrichWithReviewData(dto, product.getId());
                    dto.setCreatedBy(adminUserId);
                    try {
                        User admin = userRepository.findById(adminUserId).orElse(null);
                        if (admin != null) {
                            String fullName = admin.getFullName();
                            if (fullName != null && !fullName.isEmpty()) {
                                dto.setCreatorName(fullName);
                            } else {
                                String firstName = admin.getFirstName();
                                String lastName = admin.getLastName();
                                if (firstName != null && lastName != null) {
                                    dto.setCreatorName(firstName + " " + lastName);
                                } else if (firstName != null) {
                                    dto.setCreatorName(firstName);
                                } else {
                                    dto.setCreatorName("Admin");
                                }
                            }
                        } else {
                            dto.setCreatorName("Admin");
                        }
                    } catch (Exception e) {
                        log.warn("Failed to fetch admin name: {}", e.getMessage());
                        dto.setCreatorName("Admin");
                    }
                    return dto;
                });
    }

    @Override
    public boolean canEditProduct(String userId, String productId) {  // Changed from UUID to String
        if (userId == null || productId == null) {
            return false;
        }
        try {
            Product product = productRepository.findById(productId).orElse(null);
            if (product == null) return false;
            if (product.getCreatedBy() == null) return false;
            return product.getCreatedBy().equals(userId);
        } catch (Exception e) {
            log.error("Error checking edit permission: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public String getProductOwner(String productId) {  // Changed from UUID to String (returns String)
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));
        return product.getCreatedBy();
    }

    private ProductDTO enrichWithCategory(ProductDTO dto, String categoryId) {  // Changed from UUID to String
        if (dto == null || categoryId == null) {
            return dto;
        }
        
        try {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) {
                dto.setCategory(ProductDTO.CategorySummary.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                        .build());
            }
        } catch (Exception e) {
            log.warn("Failed to fetch category for product: {}", e.getMessage());
        }
        
        return dto;
    }

    private ProductDTO enrichWithReviewData(ProductDTO dto, String productId) {  // Changed from UUID to String
        if (dto == null) {
            return null;
        }
        
        try {
            Long reviewCount = reviewRepository.countByProductId(productId);
            dto.setReviewCount(reviewCount != null ? reviewCount.intValue() : 0);
            
            Double avgRating = reviewRepository.getAverageRatingOrDefault(productId);
            dto.setRating(avgRating != null ? avgRating : 0.0);
            
            log.debug("Enriched product {} with reviewCount: {}, avgRating: {}", 
                productId, dto.getReviewCount(), dto.getRating());
                
        } catch (Exception e) {
            log.warn("Failed to fetch review data for product {}: {}", productId, e.getMessage());
            dto.setReviewCount(0);
            dto.setRating(0.0);
        }
        
        return dto;
    }

    private ProductDTO enrichWithCreatorInfo(ProductDTO dto, String creatorId) {  // Changed from UUID to String
        if (dto == null) {
            return null;
        }
        
        if (creatorId == null) {
            dto.setCreatedBy(null);
            dto.setCreatorName("Unknown");
            return dto;
        }
        
        try {
            dto.setCreatedBy(creatorId);
            User creator = userRepository.findById(creatorId).orElse(null);
            if (creator != null) {
                String fullName = creator.getFullName();
                if (fullName != null && !fullName.isEmpty()) {
                    dto.setCreatorName(fullName);
                } else {
                    String firstName = creator.getFirstName();
                    String lastName = creator.getLastName();
                    if (firstName != null && lastName != null) {
                        dto.setCreatorName(firstName + " " + lastName);
                    } else if (firstName != null) {
                        dto.setCreatorName(firstName);
                    } else {
                        dto.setCreatorName(creator.getEmail() != null ? creator.getEmail().split("@")[0] : "Admin");
                    }
                }
            } else {
                log.warn("Creator not found with ID: {}", creatorId);
                dto.setCreatorName("Unknown");
            }
            
            log.debug("Enriched product {} with creator: {}", dto.getId(), dto.getCreatorName());
        } catch (Exception e) {
            log.warn("Failed to fetch creator info for product {}: {}", dto.getId(), e.getMessage());
            dto.setCreatorName("Unknown");
        }
        
        return dto;
    }
}
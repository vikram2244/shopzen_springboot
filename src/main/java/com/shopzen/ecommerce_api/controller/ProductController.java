package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.dto.product.ProductFiltersDTO;
import com.shopzen.ecommerce_api.service.product.ProductService;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Product API", description = "Product management endpoints")
public class ProductController {

    private final ProductService productService;

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable String id) {  // Changed from UUID to String
        log.info("Fetching product by ID: {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products/slug/{slug}")
    @Operation(summary = "Get product by slug")
    public ResponseEntity<ProductDTO> getProductBySlug(@PathVariable String slug) {
        log.info("Fetching product by slug: {}", slug);
        return ResponseEntity.ok(productService.getProductBySlug(slug));
    }

    @GetMapping("/products/id/{id}")
    @Operation(summary = "Get product by ID (alternative)")
    public ResponseEntity<ProductDTO> getProductByIdAlt(@PathVariable String id) {  // Changed from UUID to String
        log.info("Fetching product by ID (alt): {}", id);
        return ResponseEntity.ok(productService.getProductById(id));
    }

    @GetMapping("/products")
    @Operation(summary = "Get products with filters")
    public ResponseEntity<Page<ProductDTO>> getProducts(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String brandId,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean isTrending,
            @RequestParam(required = false) Boolean isNewArrival,
            @RequestParam(required = false) Boolean isBestseller,
            @RequestParam(required = false) Boolean inStock,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "48") int size) {
        
        log.info("Fetching products with filters - page: {}, size: {}, sortBy: {}", page, size, sortBy);
        
        ProductFiltersDTO filters = ProductFiltersDTO.builder()
                .search(search)
                .categoryId(categoryId)
                .brandId(brandId)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .minRating(minRating)
                .isFeatured(isFeatured)
                .isTrending(isTrending)
                .isNewArrival(isNewArrival)
                .isBestseller(isBestseller)
                .inStock(inStock)
                .sortBy(sortBy != null ? sortBy : "newest")
                .build();
        
        Sort sort = getSortFromString(sortBy != null ? sortBy : "newest");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getProducts(filters, pageable);
        log.info("Found {} products", products.getTotalElements());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/categories/{categoryId}/products")
    @Operation(summary = "Get products by category ID")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryId(@PathVariable String categoryId) {  // Changed from UUID to String
        log.info("Fetching products for category ID: {}", categoryId);
        List<ProductDTO> products = productService.getProductsByCategory(categoryId);
        log.info("Found {} products for category {}", products.size(), categoryId);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/products/category/{categoryId}")
    @Operation(summary = "Get products by category (alternative)")
    public ResponseEntity<List<ProductDTO>> getProductsByCategoryAlt(@PathVariable String categoryId) {  // Changed from UUID to String
        log.info("Fetching products for category ID (alt): {}", categoryId);
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    @GetMapping("/products/featured")
    @Operation(summary = "Get featured products")
    public ResponseEntity<List<ProductDTO>> getFeaturedProducts() {
        log.info("Fetching featured products");
        return ResponseEntity.ok(productService.getFeaturedProducts());
    }

    @GetMapping("/products/trending")
    @Operation(summary = "Get trending products")
    public ResponseEntity<List<ProductDTO>> getTrendingProducts() {
        log.info("Fetching trending products");
        return ResponseEntity.ok(productService.getTrendingProducts());
    }

    @GetMapping("/products/new-arrivals")
    @Operation(summary = "Get new arrivals")
    public ResponseEntity<List<ProductDTO>> getNewArrivals() {
        log.info("Fetching new arrivals");
        return ResponseEntity.ok(productService.getNewArrivals());
    }

    @GetMapping("/products/best-sellers")
    @Operation(summary = "Get best sellers")
    public ResponseEntity<List<ProductDTO>> getBestSellers() {
        log.info("Fetching best sellers");
        return ResponseEntity.ok(productService.getBestSellers());
    }

    @GetMapping("/products/{productId}/related")
    @Operation(summary = "Get related products")
    public ResponseEntity<List<ProductDTO>> getRelatedProducts(@PathVariable String productId) {  // Changed from UUID to String
        log.info("Fetching related products for product: {}", productId);
        return ResponseEntity.ok(productService.getRelatedProducts(productId));
    }

    private Sort getSortFromString(String sortBy) {
        switch (sortBy) {
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "sellingPrice");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "sellingPrice");
            case "rating":
                return Sort.by(Sort.Direction.DESC, "rating");
            case "popular":
                return Sort.by(Sort.Direction.DESC, "reviewCount");
            case "newest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}
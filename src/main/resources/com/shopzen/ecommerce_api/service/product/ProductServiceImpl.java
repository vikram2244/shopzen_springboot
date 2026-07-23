package com.shopzen.ecommerce_api.service.product;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.dto.product.ProductFiltersDTO;
import com.shopzen.ecommerce_api.entity.Product;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final EntityMapper entityMapper;

    @Override
    public ProductDTO getProductBySlug(String slug) {
        Product product = productRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return entityMapper.toProductDTO(product);
    }

    @Override
    public ProductDTO getProductById(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return entityMapper.toProductDTO(product);
    }

    @Override
    public Page<ProductDTO> getProducts(ProductFiltersDTO filters, Pageable pageable) {
        // Handle sorting
        Pageable sortedPageable = pageable;
        
        if (filters.getSortBy() != null && !filters.getSortBy().isEmpty()) {
            Sort sort = getSortFromString(filters.getSortBy());
            // Create a new Pageable with the sort
            sortedPageable = PageRequest.of(
                pageable.getPageNumber(), 
                pageable.getPageSize(), 
                sort
            );
        }
        
        UUID categoryId = null;
        UUID brandId = null;
        
        try {
            if (filters.getCategoryId() != null && !filters.getCategoryId().isEmpty()) {
                categoryId = UUID.fromString(filters.getCategoryId());
            }
            if (filters.getBrandId() != null && !filters.getBrandId().isEmpty()) {
                brandId = UUID.fromString(filters.getBrandId());
            }
        } catch (IllegalArgumentException e) {
            // Invalid UUID format, ignore
        }
        
        BigDecimal minPrice = filters.getMinPrice() != null ? 
            BigDecimal.valueOf(filters.getMinPrice()) : BigDecimal.ZERO;
        BigDecimal maxPrice = filters.getMaxPrice() != null ? 
            BigDecimal.valueOf(filters.getMaxPrice()) : BigDecimal.valueOf(200000);
        
        String search = filters.getSearch() != null ? filters.getSearch() : null;
        
        // Handle inStock filter
        Boolean inStock = filters.getInStock();
        
        Page<Product> products = productRepository.findWithFilters(
            categoryId,
            brandId,
            search,
            minPrice,
            maxPrice,
            inStock,
            sortedPageable
        );
        
        return products.map(entityMapper::toProductDTO);
    }

    private Sort getSortFromString(String sortBy) {
        switch (sortBy) {
            case "newest":
                return Sort.by(Sort.Direction.DESC, "createdAt");
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "sellingPrice");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "sellingPrice");
            case "rating":
                return Sort.by(Sort.Direction.DESC, "rating");
            case "popular":
                return Sort.by(Sort.Direction.DESC, "reviewCount");
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }

    @Override
    public List<ProductDTO> getProductsByCategory(UUID categoryId) {
        return productRepository.findByCategoryIdAndIsActiveTrue(categoryId)
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getFeaturedProducts() {
        return productRepository.findTop8ByIsFeaturedTrueAndIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getTrendingProducts() {
        return productRepository.findTop8ByIsTrendingTrueAndIsActiveTrueOrderByRatingDesc()
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getNewArrivals() {
        return productRepository.findTop8ByIsNewArrivalTrueAndIsActiveTrueOrderByCreatedAtDesc()
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getBestSellers() {
        return productRepository.findTop8ByIsBestsellerTrueAndIsActiveTrueOrderByReviewCountDesc()
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProductDTO> getRelatedProducts(UUID productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        Pageable limit = Pageable.ofSize(4);
        return productRepository.findRelatedProducts(product.getCategoryId(), productId, limit)
                .stream()
                .map(entityMapper::toProductDTO)
                .collect(Collectors.toList());
    }

    @Override
    public ProductDTO createProduct(ProductDTO productDTO) {
        Product product = entityMapper.toProduct(productDTO);
        product = productRepository.save(product);
        return entityMapper.toProductDTO(product);
    }

    @Override
    public ProductDTO updateProduct(UUID id, ProductDTO productDTO) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        
        // Update fields
        existing.setName(productDTO.getName());
        existing.setSlug(productDTO.getSlug());
        existing.setDescription(productDTO.getDescription());
        existing.setOriginalPrice(BigDecimal.valueOf(productDTO.getOriginalPrice()));
        existing.setSellingPrice(BigDecimal.valueOf(productDTO.getSellingPrice()));
        existing.setStockQuantity(productDTO.getStockQuantity());
        existing.setImages(productDTO.getImages());
        existing.setColors(productDTO.getColors());
        existing.setSizes(productDTO.getSizes());
        existing.setIsFeatured(productDTO.getIsFeatured());
        existing.setIsTrending(productDTO.getIsTrending());
        existing.setIsBestseller(productDTO.getIsBestseller());
        existing.setIsNewArrival(productDTO.getIsNewArrival());
        existing.setFreeDelivery(productDTO.getFreeDelivery());
        existing.setIsActive(productDTO.getIsActive());
        
        existing = productRepository.save(existing);
        return entityMapper.toProductDTO(existing);
    }

    @Override
    public void deleteProduct(UUID id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        product.setIsActive(false);
        productRepository.save(product);
    }
}
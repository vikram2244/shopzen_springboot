package com.shopzen.ecommerce_api.service.product;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.dto.product.ProductFiltersDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    ProductDTO getProductBySlug(String slug);
    ProductDTO getProductById(String id);  // Changed from UUID to String
    Page<ProductDTO> getProducts(ProductFiltersDTO filters, Pageable pageable);
    List<ProductDTO> getProductsByCategory(String categoryId);  // Changed from UUID to String
    List<ProductDTO> getFeaturedProducts();
    List<ProductDTO> getTrendingProducts();
    List<ProductDTO> getNewArrivals();
    List<ProductDTO> getBestSellers();
    List<ProductDTO> getRelatedProducts(String productId);  // Changed from UUID to String
    ProductDTO createProduct(String adminUserId, ProductDTO productDTO);  // Changed from UUID to String
    ProductDTO updateProduct(String adminUserId, String productId, ProductDTO productDTO);  // Changed from UUID to String
    void deleteProduct(String adminUserId, String productId);  // Changed from UUID to String
    Page<ProductDTO> getProductsByAdmin(String adminUserId, Pageable pageable);  // Changed from UUID to String
    boolean canEditProduct(String userId, String productId);  // Changed from UUID to String
    String getProductOwner(String productId);  // Changed from UUID to String (returns String)
    
    @Deprecated
    ProductDTO createProduct(ProductDTO productDTO);
    @Deprecated
    ProductDTO updateProduct(String id, ProductDTO productDTO);  // Changed from UUID to String
    @Deprecated
    void deleteProduct(String id);  // Changed from UUID to String
}
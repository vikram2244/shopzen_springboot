package com.shopzen.ecommerce_api.service.product;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.dto.product.ProductFiltersDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    ProductDTO getProductBySlug(String slug);
    ProductDTO getProductById(UUID id);
    Page<ProductDTO> getProducts(ProductFiltersDTO filters, Pageable pageable);
    List<ProductDTO> getProductsByCategory(UUID categoryId);
    List<ProductDTO> getFeaturedProducts();
    List<ProductDTO> getTrendingProducts();
    List<ProductDTO> getNewArrivals();
    List<ProductDTO> getBestSellers();
    List<ProductDTO> getRelatedProducts(UUID productId);
    ProductDTO createProduct(ProductDTO productDTO);
    ProductDTO updateProduct(UUID id, ProductDTO productDTO);
    void deleteProduct(UUID id);
}
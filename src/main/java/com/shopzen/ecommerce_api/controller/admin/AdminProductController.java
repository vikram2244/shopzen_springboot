package com.shopzen.ecommerce_api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.service.product.ProductService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Product API", description = "Admin product management")
@Slf4j
public class AdminProductController {

    private final ProductService productService;

    @PostMapping
    @Operation(summary = "Create new product")
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} creating product", adminUserId);
        ProductDTO createdProduct = productService.createProduct(adminUserId, productDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable String id,  // Changed from UUID to String
            @Valid @RequestBody ProductDTO productDTO) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} updating product: {}", adminUserId, id);
        ProductDTO updatedProduct = productService.updateProduct(adminUserId, id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product (soft delete)")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} deleting product: {}", adminUserId, id);
        productService.deleteProduct(adminUserId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-products")
    @Operation(summary = "Get products created by the current admin")
    public ResponseEntity<Page<ProductDTO>> getMyProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sortBy) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Fetching products created by admin: {}", adminUserId);
        
        Sort sort = getSortFromString(sortBy != null ? sortBy : "newest");
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<ProductDTO> products = productService.getProductsByAdmin(adminUserId, pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}/can-edit")
    @Operation(summary = "Check if current admin can edit the product")
    public ResponseEntity<Boolean> canEditProduct(@PathVariable String productId) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        boolean canEdit = productService.canEditProduct(adminUserId, productId);
        return ResponseEntity.ok(canEdit);
    }

    private Sort getSortFromString(String sortBy) {
        switch (sortBy) {
            case "name_asc":
                return Sort.by(Sort.Direction.ASC, "name");
            case "name_desc":
                return Sort.by(Sort.Direction.DESC, "name");
            case "price_asc":
                return Sort.by(Sort.Direction.ASC, "sellingPrice");
            case "price_desc":
                return Sort.by(Sort.Direction.DESC, "sellingPrice");
            case "stock_asc":
                return Sort.by(Sort.Direction.ASC, "stockQuantity");
            case "stock_desc":
                return Sort.by(Sort.Direction.DESC, "stockQuantity");
            case "newest":
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt");
        }
    }
}
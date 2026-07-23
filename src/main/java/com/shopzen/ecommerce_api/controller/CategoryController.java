package com.shopzen.ecommerce_api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.product.CategoryDTO;
import com.shopzen.ecommerce_api.dto.product.CategoryRequestDTO;
import com.shopzen.ecommerce_api.service.category.CategoryService;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDTO>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @GetMapping("/categories/active")
    public ResponseEntity<List<CategoryDTO>> getActiveCategories() {
        return ResponseEntity.ok(categoryService.getActiveCategories());
    }

    @GetMapping("/categories/slug/{slug}")
    public ResponseEntity<CategoryDTO> getCategoryBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.getCategoryBySlug(slug));
    }

    @GetMapping("/categories/{id}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable String id) {  // Changed from UUID to String
        return ResponseEntity.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/categories/{parentId}/sub")
    public ResponseEntity<List<CategoryDTO>> getSubCategories(@PathVariable String parentId) {  // Changed from UUID to String
        return ResponseEntity.ok(categoryService.getSubCategories(parentId));
    }

    @GetMapping("/categories/tree")
    public ResponseEntity<List<CategoryDTO>> getCategoryTree() {
        return ResponseEntity.ok(categoryService.getCategoryTree());
    }

    @PostMapping("/admin/categories")
    public ResponseEntity<CategoryDTO> createCategory(@RequestBody CategoryRequestDTO request) {
        return ResponseEntity.ok(categoryService.createCategory(request));
    }

    @PutMapping("/admin/categories/{id}")
    public ResponseEntity<CategoryDTO> updateCategory(@PathVariable String id, @RequestBody CategoryRequestDTO request) {  // Changed from UUID to String
        return ResponseEntity.ok(categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/admin/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable String id) {  // Changed from UUID to String
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/admin/categories/{id}/toggle")
    public ResponseEntity<CategoryDTO> toggleCategoryStatus(@PathVariable String id) {  // Changed from UUID to String
        return ResponseEntity.ok(categoryService.toggleCategoryStatus(id));
    }

    @PatchMapping("/admin/categories/{id}/reorder")
    public ResponseEntity<CategoryDTO> reorderCategory(@PathVariable String id, @RequestParam Integer sortOrder) {  // Changed from UUID to String
        return ResponseEntity.ok(categoryService.reorderCategory(id, sortOrder));
    }
}
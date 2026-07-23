package com.shopzen.ecommerce_api.service.category;

import java.util.List;

import com.shopzen.ecommerce_api.dto.product.CategoryDTO;
import com.shopzen.ecommerce_api.dto.product.CategoryRequestDTO;

public interface CategoryService {
    List<CategoryDTO> getAllCategories();
    List<CategoryDTO> getActiveCategories();
    CategoryDTO getCategoryBySlug(String slug);
    CategoryDTO getCategoryById(String id);  // Changed from UUID to String
    List<CategoryDTO> getSubCategories(String parentId);  // Changed from UUID to String
    List<CategoryDTO> getCategoryTree();
    CategoryDTO createCategory(CategoryRequestDTO request);
    CategoryDTO updateCategory(String id, CategoryRequestDTO request);  // Changed from UUID to String
    void deleteCategory(String id);  // Changed from UUID to String
    CategoryDTO toggleCategoryStatus(String id);  // Changed from UUID to String
    CategoryDTO reorderCategory(String id, Integer newSortOrder);  // Changed from UUID to String
}
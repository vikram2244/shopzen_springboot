package com.shopzen.ecommerce_api.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.shopzen.ecommerce_api.dto.product.CategoryDTO;
import com.shopzen.ecommerce_api.dto.product.CategoryRequestDTO;
import com.shopzen.ecommerce_api.entity.Category;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAllByOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getActiveCategories() {
        return categoryRepository.findByIsActiveTrueOrderBySortOrderAsc()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDTO getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return toDTO(category);
    }

    @Override
    public CategoryDTO getCategoryById(String id) {  // Changed from UUID to String
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return toDTO(category);
    }

    @Override
    public List<CategoryDTO> getSubCategories(String parentId) {  // Changed from UUID to String
        return categoryRepository.findByParentIdAndIsActiveTrueOrderBySortOrderAsc(parentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getCategoryTree() {
        List<Category> allCategories = categoryRepository.findByIsActiveTrueOrderBySortOrderAsc();
        
        List<CategoryDTO> tree = new ArrayList<>();
        
        List<Category> rootCategories = allCategories.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());
        
        for (Category root : rootCategories) {
            CategoryDTO rootDTO = toDTO(root);
            rootDTO.setChildren(getChildrenDTO(root.getId(), allCategories));
            tree.add(rootDTO);
        }
        
        return tree;
    }

    @Override
    public CategoryDTO createCategory(CategoryRequestDTO request) {
        if (categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Category slug already exists: " + request.getSlug());
        }

        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            String parentId = request.getParentId();
            if (!categoryRepository.existsById(parentId)) {
                throw new ResourceNotFoundException("Parent category not found");
            }
        }

        Category category = Category.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .description(request.getDescription())
                .imageUrl(request.getImageUrl())
                .parentId(request.getParentId() != null ? request.getParentId() : null)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : getNextSortOrder())
                .build();

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Override
    public CategoryDTO updateCategory(String id, CategoryRequestDTO request) {  // Changed from UUID to String
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getSlug().equals(request.getSlug()) && 
            categoryRepository.existsBySlug(request.getSlug())) {
            throw new RuntimeException("Category slug already exists: " + request.getSlug());
        }
        
        if (request.getParentId() != null && !request.getParentId().isEmpty()) {
            String parentId = request.getParentId();
            if (!categoryRepository.existsById(parentId)) {
                throw new ResourceNotFoundException("Parent category not found");
            }
            if (parentId.equals(id)) {
                throw new RuntimeException("Category cannot be its own parent");
            }
        }

        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setImageUrl(request.getImageUrl());
        category.setParentId(request.getParentId() != null ? request.getParentId() : null);
        
        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }
        
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }

        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Override
    public void deleteCategory(String id) {  // Changed from UUID to String
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (categoryRepository.existsByParentId(id)) {
            throw new RuntimeException("Cannot delete category with sub-categories. Delete or move sub-categories first.");
        }

        category.setIsActive(false);
        categoryRepository.save(category);
    }

    @Override
    public CategoryDTO toggleCategoryStatus(String id) {  // Changed from UUID to String
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        category.setIsActive(!category.getIsActive());
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    @Override
    public CategoryDTO reorderCategory(String id, Integer newSortOrder) {  // Changed from UUID to String
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        
        if (!category.getSortOrder().equals(newSortOrder)) {
            category.setSortOrder(newSortOrder);
        }
        
        category = categoryRepository.save(category);
        return toDTO(category);
    }

    private List<CategoryDTO> getChildrenDTO(String parentId, List<Category> allCategories) {  // Changed from UUID to String
        List<Category> children = allCategories.stream()
                .filter(c -> c.getParentId() != null && c.getParentId().equals(parentId))
                .collect(Collectors.toList());
        
        List<CategoryDTO> childrenDTO = new ArrayList<>();
        for (Category child : children) {
            CategoryDTO childDTO = toDTO(child);
            childDTO.setChildren(getChildrenDTO(child.getId(), allCategories));
            childrenDTO.add(childDTO);
        }
        
        return childrenDTO;
    }

    private Integer getNextSortOrder() {
        return categoryRepository.findMaxSortOrder().orElse(0) + 1;
    }

    private CategoryDTO toDTO(Category category) {
        CategoryDTO dto = CategoryDTO.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .imageUrl(category.getImageUrl())
                .parentId(category.getParentId())
                .isActive(category.getIsActive())
                .sortOrder(category.getSortOrder())
                .createdAt(category.getCreatedAt() != null ? category.getCreatedAt().toString() : null)
                .updatedAt(category.getUpdatedAt() != null ? category.getUpdatedAt().toString() : null)
                .build();

        return dto;
    }

    private Category toEntity(CategoryDTO dto) {
        return Category.builder()
                .id(dto.getId())
                .name(dto.getName())
                .slug(dto.getSlug())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .parentId(dto.getParentId())
                .isActive(dto.getIsActive())
                .sortOrder(dto.getSortOrder())
                .build();
    }
}
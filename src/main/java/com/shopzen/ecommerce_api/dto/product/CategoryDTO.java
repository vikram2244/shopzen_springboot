package com.shopzen.ecommerce_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDTO {
 private String id;
 private String name;
 private String slug;
 private String description;
 private String imageUrl;
 private String parentId;
 private Boolean isActive;
 private Integer sortOrder;
 private String createdAt;
 private String updatedAt;
 private List<CategoryDTO> children;
 private CategoryDTO parent;
}

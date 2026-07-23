package com.shopzen.ecommerce_api.dto.wishlist;

import com.shopzen.ecommerce_api.dto.product.ProductDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WishlistItemDTO {
 private String id;
 private String productId;
 private String userId;
 private String createdAt;
 private ProductDTO product;
}

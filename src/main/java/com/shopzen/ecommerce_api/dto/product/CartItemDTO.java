package com.shopzen.ecommerce_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItemDTO {
    private String id;
    private String productId;
    private Integer quantity;
    private String selectedColor;
    private String selectedSize;
    private ProductDTO product;
}

package com.shopzen.ecommerce_api.dto.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddToCartRequest {
    @NotBlank(message = "Product ID is required")
    private String productId;
    
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity = 1;
    
    private String selectedColor;
    private String selectedSize;
}

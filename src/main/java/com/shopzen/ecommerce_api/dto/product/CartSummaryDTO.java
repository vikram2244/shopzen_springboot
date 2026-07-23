package com.shopzen.ecommerce_api.dto.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartSummaryDTO {
    private int itemCount;
    private BigDecimal subtotal;
    private List<CartItemDTO> items;
}
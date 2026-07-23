package com.shopzen.ecommerce_api.entity;

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
public class ProductSnapshot {
    private String id;
    private String name;
    private String slug;
    private String description;
    private BigDecimal originalPrice;
    private BigDecimal sellingPrice;
    private List<String> images;
    private List<String> colors;
    private List<String> sizes;
    private String brand;
}
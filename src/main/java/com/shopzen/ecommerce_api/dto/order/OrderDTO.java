package com.shopzen.ecommerce_api.dto.order;

import com.shopzen.ecommerce_api.entity.AddressSnapshot;
import com.shopzen.ecommerce_api.entity.ProductSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
    private String id;
    private String userId;
    private String orderNumber;
    private String status;
    private String paymentMethod;
    private String paymentStatus;
    private Double subtotal;
    private Double discount;
    private Double shippingCharge;
    private Double tax;
    private Double total;
    private String couponCode;
    private AddressSnapshot address;
    private String notes;
    private String createdAt;
    private String updatedAt;
    private String adminId; 
    private List<OrderItemDTO> items;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemDTO {
        private String id;
        private String orderId;
        private String productId;
        private ProductSnapshot product;
        private Integer quantity;
        private Double unitPrice;
        private Double totalPrice;
        private String selectedColor;
        private String selectedSize;
    }
}
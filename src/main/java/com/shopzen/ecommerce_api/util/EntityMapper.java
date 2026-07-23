package com.shopzen.ecommerce_api.util;

import com.shopzen.ecommerce_api.dto.order.OrderDTO;
import com.shopzen.ecommerce_api.dto.order.OrderRequestDTO.AddressDTO;
import com.shopzen.ecommerce_api.dto.product.CartItemDTO;
import com.shopzen.ecommerce_api.dto.product.ProductDTO;
import com.shopzen.ecommerce_api.entity.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class EntityMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public ProductDTO toProductDTO(Product product) {
        if (product == null) return null;
        
        return ProductDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .categoryId(product.getCategoryId())
                .brandId(product.getBrandId())
                .originalPrice(product.getOriginalPrice() != null ? product.getOriginalPrice().doubleValue() : 0.0)
                .sellingPrice(product.getSellingPrice() != null ? product.getSellingPrice().doubleValue() : 0.0)
                .stockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0)
                .images(product.getImages())
                .colors(product.getColors())
                .sizes(product.getSizes())
                .rating(product.getRating() != null ? product.getRating().doubleValue() : 0.0)
                .reviewCount(product.getReviewCount() != null ? product.getReviewCount() : 0)
                .isFeatured(product.getIsFeatured() != null ? product.getIsFeatured() : false)
                .isTrending(product.getIsTrending() != null ? product.getIsTrending() : false)
                .isBestseller(product.getIsBestseller() != null ? product.getIsBestseller() : false)
                .isNewArrival(product.getIsNewArrival() != null ? product.getIsNewArrival() : false)
                .isActive(product.getIsActive() != null ? product.getIsActive() : true)
                .freeDelivery(product.getFreeDelivery() != null ? product.getFreeDelivery() : false)
                .createdAt(product.getCreatedAt() != null ? product.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(product.getUpdatedAt() != null ? product.getUpdatedAt().format(FORMATTER) : null)
                .createdBy(product.getCreatedBy())
                .build();
    }

    public Product toProduct(ProductDTO dto) {
        if (dto == null) return null;
        
        Product product = new Product();
        
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            product.setId(dto.getId());
        }
        product.setName(dto.getName());
        product.setSlug(dto.getSlug());
        product.setDescription(dto.getDescription());
        product.setCategoryId(dto.getCategoryId());
        product.setBrandId(dto.getBrandId());
        product.setOriginalPrice(BigDecimal.valueOf(dto.getOriginalPrice()));
        product.setSellingPrice(BigDecimal.valueOf(dto.getSellingPrice()));
        product.setStockQuantity(dto.getStockQuantity());
        product.setImages(dto.getImages());
        product.setColors(dto.getColors());
        product.setSizes(dto.getSizes());
        product.setIsFeatured(dto.getIsFeatured() != null ? dto.getIsFeatured() : false);
        product.setIsTrending(dto.getIsTrending() != null ? dto.getIsTrending() : false);
        product.setIsBestseller(dto.getIsBestseller() != null ? dto.getIsBestseller() : false);
        product.setIsNewArrival(dto.getIsNewArrival() != null ? dto.getIsNewArrival() : false);
        product.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        product.setFreeDelivery(dto.getFreeDelivery() != null ? dto.getFreeDelivery() : false);
        
        return product;
    }
    
    public ProductDTO toProductDTO(Product product, String creatorName) {
        ProductDTO dto = toProductDTO(product);
        if (dto != null && creatorName != null) {
            dto.setCreatorName(creatorName);
        }
        return dto;
    }

    public CartItemDTO toCartItemDTO(CartItem cartItem) {
        if (cartItem == null) return null;
        
        return CartItemDTO.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProductId())
                .quantity(cartItem.getQuantity())
                .selectedColor(cartItem.getSelectedColor())
                .selectedSize(cartItem.getSelectedSize())
                .build();
    }

    public OrderDTO toOrderDTO(Order order) {
        if (order == null) return null;
        
        return OrderDTO.builder()
                .id(order.getId())
                .userId(order.getUserId())
                .orderNumber(order.getOrderNumber())
                .status(order.getStatus())
                .paymentMethod(order.getPaymentMethod())
                .paymentStatus(order.getPaymentStatus())
                .subtotal(order.getSubtotal() != null ? order.getSubtotal().doubleValue() : 0.0)
                .discount(order.getDiscount() != null ? order.getDiscount().doubleValue() : 0.0)
                .shippingCharge(order.getShippingCharge() != null ? order.getShippingCharge().doubleValue() : 0.0)
                .tax(order.getTax() != null ? order.getTax().doubleValue() : 0.0)
                .total(order.getTotal() != null ? order.getTotal().doubleValue() : 0.0)
                .couponCode(order.getCouponCode())
                .address(order.getAddressSnapshot())
                .notes(order.getNotes())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt().format(FORMATTER) : null)
                .updatedAt(order.getUpdatedAt() != null ? order.getUpdatedAt().format(FORMATTER) : null)
                .adminId(order.getAdminId())
                .items(order.getOrderItems() != null ? 
                        order.getOrderItems().stream()
                                .map(this::toOrderItemDTO)
                                .collect(java.util.stream.Collectors.toList()) : null)
                .build();
    }

    public OrderDTO.OrderItemDTO toOrderItemDTO(OrderItem orderItem) {
        if (orderItem == null) return null;
        
        return OrderDTO.OrderItemDTO.builder()
                .id(orderItem.getId())
                .orderId(orderItem.getOrderId())
                .productId(orderItem.getProductId())
                .product(orderItem.getProductSnapshot())
                .quantity(orderItem.getQuantity())
                .unitPrice(orderItem.getUnitPrice() != null ? orderItem.getUnitPrice().doubleValue() : 0.0)
                .totalPrice(orderItem.getTotalPrice() != null ? orderItem.getTotalPrice().doubleValue() : 0.0)
                .selectedColor(orderItem.getSelectedColor())
                .selectedSize(orderItem.getSelectedSize())
                .build();
    }

    public ProductSnapshot toProductSnapshot(Product product) {
        if (product == null) return null;
        
        return ProductSnapshot.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .originalPrice(product.getOriginalPrice())
                .sellingPrice(product.getSellingPrice())
                .images(product.getImages())
                .colors(product.getColors())
                .sizes(product.getSizes())
                .brand(product.getBrand() != null ? product.getBrand().getName() : null)
                .build();
    }

    public Order toOrder(OrderDTO dto) {
        if (dto == null) return null;
        
        Order order = new Order();
        
        if (dto.getId() != null && !dto.getId().isEmpty()) {
            order.setId(dto.getId());
        }
        if (dto.getUserId() != null && !dto.getUserId().isEmpty()) {
            order.setUserId(dto.getUserId());
        }
        order.setOrderNumber(dto.getOrderNumber());
        order.setStatus(dto.getStatus());
        order.setPaymentMethod(dto.getPaymentMethod());
        order.setPaymentStatus(dto.getPaymentStatus());
        order.setSubtotal(BigDecimal.valueOf(dto.getSubtotal()));
        order.setDiscount(BigDecimal.valueOf(dto.getDiscount()));
        order.setShippingCharge(BigDecimal.valueOf(dto.getShippingCharge()));
        order.setTax(BigDecimal.valueOf(dto.getTax()));
        order.setTotal(BigDecimal.valueOf(dto.getTotal()));
        order.setCouponCode(dto.getCouponCode());
        order.setAddressSnapshot(dto.getAddress());
        order.setNotes(dto.getNotes());
        
        return order;
    }

    public AddressSnapshot toAddressSnapshot(AddressDTO addressDTO) {
        if (addressDTO == null) return null;
        
        return AddressSnapshot.builder()
                .fullName(addressDTO.getFullName())
                .phone(addressDTO.getPhone())
                .addressLine1(addressDTO.getAddressLine1())
                .addressLine2(addressDTO.getAddressLine2())
                .city(addressDTO.getCity())
                .state(addressDTO.getState())
                .pincode(addressDTO.getPincode())
                .country(addressDTO.getCountry() != null ? addressDTO.getCountry() : "India")
                .addressType(addressDTO.getAddressType())
                .isDefault(addressDTO.isDefault())
                .build();
    }
}
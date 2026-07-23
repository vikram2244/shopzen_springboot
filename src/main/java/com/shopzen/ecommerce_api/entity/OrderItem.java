package com.shopzen.ecommerce_api.entity;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "order_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "CHAR(36)")
    private String id;  // Changed to String

    @Column(name = "order_id", nullable = false, columnDefinition = "CHAR(36)")
    private String orderId;  // Changed from UUID to String

    @Column(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private String productId;  // Changed from UUID to String

    @Column(name = "selected_color", length = 50)
    private String selectedColor;

    @Column(name = "selected_size", length = 50)
    private String selectedSize;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "total_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalPrice;

    @Type(JsonType.class)
    @Column(name = "product_snapshot", columnDefinition = "JSON")
    private ProductSnapshot productSnapshot;

    @Transient
    private Product product;

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }
}
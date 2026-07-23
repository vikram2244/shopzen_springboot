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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(columnDefinition = "CHAR(36)")
    private String id;  // Changed to String

    @Column(name = "user_id", columnDefinition = "CHAR(36)")
    private String userId;  // Changed from UUID to String

    @Column(name = "order_number", nullable = false, unique = true, length = 50)
    private String orderNumber;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "pending";

    @Column(name = "payment_method", length = 30)
    private String paymentMethod;

    @Column(name = "payment_status", length = 20)
    @Builder.Default
    private String paymentStatus = "pending";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "shipping_charge", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal shippingCharge = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Type(JsonType.class)
    @Column(name = "address_snapshot", columnDefinition = "JSON")
    private AddressSnapshot addressSnapshot;

    @Column(columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_by")
    private String createdBy;  // Changed from UUID to String
    
    @Column(name = "admin_id", columnDefinition = "CHAR(36)")
    private String adminId;  // Changed from UUID to String

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "order_id")
    @Builder.Default
    private List<OrderItem> orderItems = new ArrayList<>();

    @Transient
    private User user;

    public BigDecimal getGrandTotal() {
        return subtotal.subtract(discount).add(shippingCharge).add(tax);
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }
}
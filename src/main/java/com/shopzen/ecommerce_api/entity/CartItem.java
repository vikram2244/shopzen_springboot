package com.shopzen.ecommerce_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "cart_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "CHAR(36)", updatable = false, nullable = false)
    private String id;  // Changed to String
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
    
    @Column(name = "product_id", nullable = false, columnDefinition = "CHAR(36)")
    private String productId;  // Changed from UUID to String
    
    @Column(name = "quantity", nullable = false)
    private Integer quantity;
    
    @Column(name = "selected_color")
    private String selectedColor;
    
    @Column(name = "selected_size")
    private String selectedSize;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Transient
    private Product product;
    
    public String getUserId() {
        return cart != null ? cart.getUserId() : null;
    }
    
    public void setUserId(String userId) {
        if (cart == null) {
            cart = new Cart();
        }
        cart.setUserId(userId);
    }

    @PrePersist
    protected void onCreate() {
        if (id == null || id.isEmpty()) {
            id = UUID.randomUUID().toString();
        }
    }
}
package com.shopzen.ecommerce_api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Embeddable
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressSnapshot {
    
    @Column(name = "full_name", nullable = false)
    private String fullName;
    
    @Column(name = "phone", nullable = false)
    private String phone;
    
    @Column(name = "address_line1", nullable = false)
    private String addressLine1;
    
    @Column(name = "address_line2")
    private String addressLine2;
    
    @Column(name = "city", nullable = false)
    private String city;
    
    @Column(name = "state", nullable = false)
    private String state;
    
    @Column(name = "pincode", nullable = false)
    private String pincode;
    
    @Column(name = "country", nullable = false)
    private String country;
    
    @Column(name = "address_type")
    private String addressType;
    
    @Column(name = "is_default")
    @Builder.Default
    private boolean isDefault = false;
}
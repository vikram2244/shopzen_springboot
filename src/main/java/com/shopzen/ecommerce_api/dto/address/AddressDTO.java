// com.shopzen.ecommerce_api.dto.address.AddressDTO.java
package com.shopzen.ecommerce_api.dto.address;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddressDTO {
    private String id;
    private String userId;
    private String fullName;
    private String phone;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String pincode;
    private String country;
    private Boolean isDefault;
    private String addressType;
    private String createdAt;
    private String updatedAt;
}
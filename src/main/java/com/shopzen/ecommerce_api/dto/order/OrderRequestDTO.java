package com.shopzen.ecommerce_api.dto.order;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {
    
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;
    
    private String couponCode;
    
    @Valid
    @NotNull(message = "Address is required")
    private AddressDTO address;
    
    private String notes;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressDTO {
        @NotBlank(message = "Full name is required")
        private String fullName;
        
        @NotBlank(message = "Phone is required")
        private String phone;
        
        @NotBlank(message = "Address line 1 is required")
        private String addressLine1;
        
        private String addressLine2;
        
        @NotBlank(message = "City is required")
        private String city;
        
        @NotBlank(message = "State is required")
        private String state;
        
        @NotBlank(message = "Pincode is required")
        private String pincode;
        
        private String addressType;
        
        private boolean isDefault;
        
        private String country;
    }
}
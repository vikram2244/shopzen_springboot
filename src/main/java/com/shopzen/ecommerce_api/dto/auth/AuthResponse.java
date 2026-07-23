// src/main/java/com/shopzen/ecommerce_api/dto/auth/AuthResponse.java
package com.shopzen.ecommerce_api.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName;
    private Boolean isAdmin;
    private String message;
}
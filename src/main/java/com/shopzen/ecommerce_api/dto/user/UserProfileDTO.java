// src/main/java/com/shopzen/ecommerce_api/dto/user/UserProfileDTO.java
package com.shopzen.ecommerce_api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileDTO {
    private String id;
    private String email;
    private String firstName;
    private String lastName;
    private String fullName; // Computed from firstName + lastName
    private String avatarUrl;
    private String phone;
    private LocalDate dateOfBirth;
    private String gender;
    private Boolean isAdmin;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
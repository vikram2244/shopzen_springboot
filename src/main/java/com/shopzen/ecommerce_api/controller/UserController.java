// src/main/java/com/shopzen/ecommerce_api/controller/UserController.java
package com.shopzen.ecommerce_api.controller;

import com.shopzen.ecommerce_api.dto.user.UserProfileDTO;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.service.user.UserService;
import com.shopzen.ecommerce_api.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<?> getCurrentUserProfile() {
        try {
            String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
            log.info("Fetching profile for user ID: {}", userId);
            UserProfileDTO profile = userService.getUserProfile(userId);
            return ResponseEntity.ok(profile);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        } catch (Exception e) {
            log.error("Error fetching user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to fetch profile: " + e.getMessage());
        }
    }

    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody UserProfileDTO profileDTO) {
        try {
            String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
            log.info("Updating profile for user ID: {}", userId);
            UserProfileDTO updated = userService.updateUserProfile(userId, profileDTO);
            return ResponseEntity.ok(updated);
        } catch (IllegalStateException e) {
            log.error("Authentication error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("User not authenticated");
        } catch (Exception e) {
            log.error("Error updating user profile: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update profile: " + e.getMessage());
        }
    }
}
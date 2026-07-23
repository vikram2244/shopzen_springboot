// src/main/java/com/shopzen/ecommerce_api/service/user/UserService.java
package com.shopzen.ecommerce_api.service.user;

import com.shopzen.ecommerce_api.dto.auth.RegisterRequest;
import com.shopzen.ecommerce_api.dto.user.ProfileUpdateDTO;
import com.shopzen.ecommerce_api.dto.user.UserProfileDTO;
import com.shopzen.ecommerce_api.entity.User;

import java.util.List;

public interface UserService {
    
    User findByEmail(String email);
    
    User findById(String id);  // Changed from UUID to String
    
    List<User> findAllUsers();
    
    User registerUser(RegisterRequest request);
    
    User verifyUser(String token);
    
    void resendVerificationEmail(String email);
    
    User updateProfile(String userId, ProfileUpdateDTO profileUpdate);  // Changed from UUID to String
    
    UserProfileDTO getUserProfile(String userId);  // Changed from UUID to String
    
    UserProfileDTO updateUserProfile(String userId, UserProfileDTO profileDTO);  // Changed from UUID to String
    
    User makeAdmin(String userId);  // Changed from UUID to String
    
    void createPasswordResetToken(String email);
    
    void resetPassword(String token, String newPassword);
    
    boolean existsByEmail(String email);
}
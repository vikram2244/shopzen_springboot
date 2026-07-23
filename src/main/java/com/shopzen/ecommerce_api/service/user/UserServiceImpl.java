// src/main/java/com/shopzen/ecommerce_api/service/user/UserServiceImpl.java
package com.shopzen.ecommerce_api.service.user;

import com.shopzen.ecommerce_api.dto.auth.RegisterRequest;
import com.shopzen.ecommerce_api.dto.user.ProfileUpdateDTO;
import com.shopzen.ecommerce_api.dto.user.UserProfileDTO;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.UserRepository;
import com.shopzen.ecommerce_api.service.email.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    // ✅ Admin secret code constant
    private static final String ADMIN_SECRET_CODE = "SHOPZEN-ADMIN-2024";

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    @Override
    public User findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public User registerUser(RegisterRequest request) {
        log.info("📝 Registering user: {}", request.getEmail());
        
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists with email: " + request.getEmail());
        }

        // ✅ Check if this is an admin registration
        boolean isAdmin = false;
        if (request.getIsAdmin() != null && request.getIsAdmin()) {
            // Validate admin code
            if (request.getAdminCode() == null || request.getAdminCode().isEmpty()) {
                throw new RuntimeException("Admin code is required for admin registration");
            }
            if (!request.getAdminCode().equals(ADMIN_SECRET_CODE)) {
                throw new RuntimeException("Invalid admin secret code");
            }
            isAdmin = true;
            log.info("👑 Admin registration validated for: {}", request.getEmail());
        }

        // Generate verification token with 2 minutes expiry
        String verificationToken = UUID.randomUUID().toString();
        LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(2);
        
        log.info("🔑 Generated token: {}", verificationToken);
        log.info("⏰ Token expiry: {} (2 minutes from now)", expiryDate);

        // Create user as PENDING (not active)
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setIsActive(false);  // NOT active until verified
        user.setIsAdmin(isAdmin);  // ✅ Set based on admin registration
        user.setEmailVerified(false);
        user.setPhoneVerified(false);
        user.setIsPending(true);  // Mark as pending
        user.setVerificationToken(verificationToken);
        user.setVerificationTokenExpiry(expiryDate);

        log.info("💾 Saving pending user: email={}, firstName={}, lastName={}, isAdmin={}", 
            user.getEmail(), user.getFirstName(), user.getLastName(), user.getIsAdmin());
        user = userRepository.save(user);
        log.info("✅ Pending user saved with ID: {}", user.getId());

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);
            log.info("📧 Verification email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.warn("⚠️ Email not sent: {}", e.getMessage());
        }

        return user;
    }

    @Override
    public User verifyUser(String token) {
        log.info("🔍 Verifying token: {}", token);
        
        if (token == null || token.trim().isEmpty()) {
            log.error("❌ Token is null or empty");
            throw new RuntimeException("Verification token is required");
        }
        
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> {
                    log.error("❌ Invalid verification token: {}", token);
                    return new RuntimeException("Invalid verification token");
                });

        log.info("✅ Found user with token: {}", user.getEmail());

        if (user.getEmailVerified()) {
            log.warn("⚠️ Email already verified for user: {}", user.getEmail());
            throw new RuntimeException("Email already verified");
        }

        if (user.getVerificationTokenExpiry() == null) {
            log.error("❌ Token expiry is null for user: {}", user.getEmail());
            throw new RuntimeException("Invalid token: expiry date missing");
        }

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            log.error("❌ Token expired for user: {}, expiry: {}", 
                user.getEmail(), user.getVerificationTokenExpiry());
            throw new RuntimeException("Verification token has expired. Please register again.");
        }

        // Activate user after verification
        user.setEmailVerified(true);
        user.setIsActive(true);
        user.setIsPending(false);
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        user = userRepository.save(user);

        log.info("✅ Email verified and user activated: {} (isAdmin: {})", user.getEmail(), user.getIsAdmin());
        return user;
    }

    @Override
    public void resendVerificationEmail(String email) {
        log.info("📧 Resending verification email for: {}", email);
        
        User user = findByEmail(email);
        
        if (user.getEmailVerified()) {
            log.warn("⚠️ Email already verified for: {}", email);
            throw new RuntimeException("Email already verified");
        }

        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpiry = LocalDateTime.now().plusMinutes(2);
        
        user.setVerificationToken(newToken);
        user.setVerificationTokenExpiry(newExpiry);
        user = userRepository.save(user);
        
        log.info("🔄 New token generated for: {}", email);

        try {
            emailService.sendVerificationEmail(user.getEmail(), newToken);
            log.info("📧 Resent verification email to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("❌ Failed to send verification email: {}", e.getMessage());
            throw new RuntimeException("Failed to send verification email.");
        }
    }

    @Override
    public User updateProfile(String userId, ProfileUpdateDTO profileUpdate) {
        User user = findById(userId);

        if (profileUpdate.getFirstName() != null) {
            user.setFirstName(profileUpdate.getFirstName());
        }
        if (profileUpdate.getLastName() != null) {
            user.setLastName(profileUpdate.getLastName());
        }
        if (profileUpdate.getPhone() != null) {
            user.setPhone(profileUpdate.getPhone());
        }
        if (profileUpdate.getDateOfBirth() != null) {
            user.setDateOfBirth(profileUpdate.getDateOfBirth());
        }
        if (profileUpdate.getGender() != null) {
            user.setGender(profileUpdate.getGender());
        }
        if (profileUpdate.getProfileImage() != null) {
            user.setProfileImage(profileUpdate.getProfileImage());
        }

        return userRepository.save(user);
    }

    @Override
    public UserProfileDTO getUserProfile(String userId) {
        User user = findById(userId);
        return toUserProfileDTO(user);
    }

    @Override
    public UserProfileDTO updateUserProfile(String userId, UserProfileDTO profileDTO) {
        User user = findById(userId);
        
        if (profileDTO.getFirstName() != null) {
            user.setFirstName(profileDTO.getFirstName());
        }
        if (profileDTO.getLastName() != null) {
            user.setLastName(profileDTO.getLastName());
        }
        if (profileDTO.getPhone() != null) {
            user.setPhone(profileDTO.getPhone());
        }
        if (profileDTO.getDateOfBirth() != null) {
            user.setDateOfBirth(profileDTO.getDateOfBirth());
        }
        if (profileDTO.getGender() != null) {
            user.setGender(profileDTO.getGender());
        }
        if (profileDTO.getAvatarUrl() != null) {
            user.setProfileImage(profileDTO.getAvatarUrl());
        }
        
        user = userRepository.save(user);
        return toUserProfileDTO(user);
    }

    @Override
    public User makeAdmin(String userId) {
        User user = findById(userId);
        user.setIsAdmin(true);
        user = userRepository.save(user);
        log.info("✅ USER MADE ADMIN: {}", user.getEmail());
        return user;
    }

    @Override
    @Transactional
    public void createPasswordResetToken(String email) {
        User user = findByEmail(email);
        
        if (!user.getEmailVerified()) {
            throw new RuntimeException("Please verify your email first before resetting password");
        }
        
        String resetToken = UUID.randomUUID().toString();
        user.setVerificationToken(resetToken);
        user.setVerificationTokenExpiry(LocalDateTime.now().plusHours(1));
        userRepository.save(user);
        
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken);
            log.info("📧 Password reset email sent to: {}", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to send password reset email: {}", e.getMessage());
            throw new RuntimeException("Failed to send password reset email");
        }
        
        log.info("✅ Password reset token created for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(String token, String newPassword) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));

        if (user.getVerificationTokenExpiry() == null) {
            throw new RuntimeException("Invalid token: expiry date missing");
        }

        if (user.getVerificationTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset token has expired");
        }

        if (!user.getEmailVerified()) {
            throw new RuntimeException("Cannot reset password for unverified account");
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setVerificationToken(null);
        user.setVerificationTokenExpiry(null);
        userRepository.save(user);
        
        log.info("✅ Password reset successfully for user: {}", user.getEmail());
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    private UserProfileDTO toUserProfileDTO(User user) {
        return UserProfileDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .avatarUrl(user.getProfileImage())
                .phone(user.getPhone())
                .dateOfBirth(user.getDateOfBirth())
                .gender(user.getGender())
                .isAdmin(user.getIsAdmin())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
package com.shopzen.ecommerce_api.controller.admin;

import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin User API", description = "Admin user management")
public class AdminUserController {

    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "Get all users with pagination (shared across all admins)")
    public ResponseEntity<Page<User>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(userRepository.findAll(pageable));
    }

    @PutMapping("/{userId}/role")
    @Operation(summary = "Update user role (make admin or remove admin)")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable String userId,
            @RequestBody Map<String, Boolean> request) {
        
        Boolean isAdmin = request.get("isAdmin");
        if (isAdmin == null) {
            throw new IllegalArgumentException("isAdmin field is required");
        }
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        user.setIsAdmin(isAdmin);
        userRepository.save(user);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("userId", userId);
        response.put("isAdmin", isAdmin);
        response.put("message", isAdmin ? "User is now an admin" : "User admin privileges removed");
        
        return ResponseEntity.ok(response);
    }
}
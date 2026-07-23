// src/main/java/com/shopzen/ecommerce_api/repository/UserRepository.java
package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByVerificationToken(String token);
    
    boolean existsByEmail(String email);
    
    Optional<User> findByEmailAndEmailVerifiedTrue(String email);
    
    // ✅ Get active user only (not pending)
    @Query("SELECT u FROM User u WHERE u.email = :email AND u.isPending = false AND u.isActive = true")
    Optional<User> findActiveUserByEmail(@Param("email") String email);
    
    // ✅ Delete pending users with expired tokens
    @Modifying
    @Transactional
    @Query("DELETE FROM User u WHERE u.isPending = true AND u.verificationTokenExpiry < :expiry")
    int deleteByIsPendingTrueAndVerificationTokenExpiryBefore(@Param("expiry") LocalDateTime expiry);
    
    // ✅ Activate user after verification
    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.isPending = false, u.isActive = true, u.emailVerified = true WHERE u.id = :userId")
    void activateUser(@Param("userId") String userId);
}
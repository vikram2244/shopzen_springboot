// src/main/java/com/shopzen/ecommerce_api/repository/VerificationTokenRepository.java
package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.VerificationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationTokenRepository extends JpaRepository<VerificationToken, String> {  // ✅ Changed from UUID to String
    
    Optional<VerificationToken> findByToken(String token);
    
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);  // ✅ Changed from UUID to String
    
    @Modifying
    @Query("DELETE FROM VerificationToken vt WHERE vt.userId = :userId AND vt.tokenType = :tokenType")
    void deleteByUserIdAndTokenType(@Param("userId") String userId, @Param("tokenType") String tokenType);  // ✅ Changed from UUID to String
    
    Optional<VerificationToken> findByUserIdAndTokenType(@Param("userId") String userId, @Param("tokenType") String tokenType);  // ✅ Changed from UUID to String
}
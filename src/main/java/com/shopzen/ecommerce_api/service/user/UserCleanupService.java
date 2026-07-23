// src/main/java/com/shopzen/ecommerce_api/service/user/UserCleanupService.java
package com.shopzen.ecommerce_api.service.user;

import com.shopzen.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserCleanupService {

    private final UserRepository userRepository;

    // ✅ Run every 30 seconds to clean up unverified users (more frequent)
    @Scheduled(fixedDelay = 30000) // 30 seconds
    @Transactional
    public void cleanupUnverifiedUsers() {
        log.debug("🧹 Cleaning up unverified users...");
        
        // Delete users who are pending and verification token expired (older than 2 minutes)
        LocalDateTime expiryThreshold = LocalDateTime.now().minusMinutes(2);
        
        // Delete users who are pending and verification token expired
        int deletedCount = userRepository.deleteByIsPendingTrueAndVerificationTokenExpiryBefore(expiryThreshold);
        
        if (deletedCount > 0) {
            log.info("✅ Deleted {} unverified users (expired > 2 minutes)", deletedCount);
        }
    }
}
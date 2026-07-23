// src/main/java/com/shopzen/ecommerce_api/controller/NotificationController.java
package com.shopzen.ecommerce_api.controller;

import com.shopzen.ecommerce_api.dto.notification.NotificationDTO;
import com.shopzen.ecommerce_api.service.notification.NotificationService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Fetching notifications for user: {}", userId);
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/paginated")
    public ResponseEntity<Page<NotificationDTO>> getMyNotificationsPaginated(
            @PageableDefault(size = 20) Pageable pageable) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Fetching paginated notifications for user: {}", userId);
        return ResponseEntity.ok(notificationService.getUserNotificationsPaginated(userId, pageable));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PutMapping("/mark-all-read")
    public ResponseEntity<Void> markAllAsRead() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Marking all notifications as read for user: {}", userId);
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable String notificationId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Marking notification {} as read for user: {}", notificationId, userId);
        notificationService.markAsRead(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<Void> deleteNotification(@PathVariable String notificationId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Deleting notification {} for user: {}", notificationId, userId);
        notificationService.deleteNotification(notificationId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteAllNotifications() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Deleting all notifications for user: {}", userId);
        notificationService.deleteAllNotifications(userId);
        return ResponseEntity.ok().build();
    }
}
// src/main/java/com/shopzen/ecommerce_api/service/notification/NotificationService.java
package com.shopzen.ecommerce_api.service.notification;

import com.shopzen.ecommerce_api.dto.notification.NotificationDTO;
import com.shopzen.ecommerce_api.entity.Notification;
import com.shopzen.ecommerce_api.entity.Order;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.repository.NotificationRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public Notification createOrderStatusNotification(Order order, User user, String oldStatus, String newStatus) {
        String title = getStatusTitle(newStatus);
        String message = getStatusMessage(order, newStatus);
        String type = getStatusType(newStatus);
        String link = "/orders";

        Notification notification = Notification.builder()
                .userId(user.getId())
                .orderId(order.getId())
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .link(link)
                .build();

        notification = notificationRepository.save(notification);
        log.info("Notification created for user: {} about order: {}", user.getEmail(), order.getOrderNumber());

        return notification;
    }

    public void createOrderConfirmationNotification(Order order, User user) {
        Notification notification = Notification.builder()
                .userId(user.getId())
                .orderId(order.getId())
                .title("Order Confirmed! 🎉")
                .message(String.format("Your order #%s has been confirmed and is being processed.", order.getOrderNumber()))
                .type("ORDER_CONFIRMATION")
                .isRead(false)
                .link("/orders")
                .build();

        notificationRepository.save(notification);
        log.info("Order confirmation notification created for user: {}", user.getEmail());
    }

    public void createOrderShippedNotification(Order order, User user) {
        Notification notification = Notification.builder()
                .userId(user.getId())
                .orderId(order.getId())
                .title("Order Shipped! 🚚")
                .message(String.format("Your order #%s has been shipped and is on its way!", order.getOrderNumber()))
                .type("ORDER_SHIPPED")
                .isRead(false)
                .link("/orders")
                .build();

        notificationRepository.save(notification);
        log.info("Order shipped notification created for user: {}", user.getEmail());
    }

    public void createOrderDeliveredNotification(Order order, User user) {
        Notification notification = Notification.builder()
                .userId(user.getId())
                .orderId(order.getId())
                .title("Order Delivered! 📦")
                .message(String.format("Your order #%s has been delivered successfully. Enjoy your purchase!", order.getOrderNumber()))
                .type("ORDER_DELIVERED")
                .isRead(false)
                .link("/orders")
                .build();

        notificationRepository.save(notification);
        log.info("Order delivered notification created for user: {}", user.getEmail());
    }

    public void createOrderCancelledNotification(Order order, User user) {
        Notification notification = Notification.builder()
                .userId(user.getId())
                .orderId(order.getId())
                .title("Order Cancelled ❌")
                .message(String.format("Your order #%s has been cancelled.", order.getOrderNumber()))
                .type("ORDER_CANCELLED")
                .isRead(false)
                .link("/orders")
                .build();

        notificationRepository.save(notification);
        log.info("Order cancelled notification created for user: {}", user.getEmail());
    }

    public List<NotificationDTO> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public Page<NotificationDTO> getUserNotificationsPaginated(String userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toDTO);
    }

    public long getUnreadCount(String userId) {
        return notificationRepository.countUnreadByUserId(userId);
    }

    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
        log.info("All notifications marked as read for user: {}", userId);
    }

    @Transactional
    public void markAsRead(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to this user");
        }
        
        notificationRepository.markAsRead(notificationId, userId);
        log.info("Notification {} marked as read for user: {}", notificationId, userId);
    }

    // ✅ DELETE: Delete a single notification
    @Transactional
    public void deleteNotification(String notificationId, String userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + notificationId));
        
        // Verify the notification belongs to the user
        if (!notification.getUserId().equals(userId)) {
            throw new RuntimeException("Notification does not belong to this user");
        }
        
        notificationRepository.delete(notification);
        log.info("Notification {} deleted for user: {}", notificationId, userId);
    }

    // ✅ DELETE: Delete all notifications for a user
    @Transactional
    public void deleteAllNotifications(String userId) {
        // Verify user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        
        notificationRepository.deleteByUserId(userId);
        log.info("All notifications deleted for user: {}", userId);
    }

    private String getStatusTitle(String status) {
        if (status == null) return "Order Update";
        switch (status.toLowerCase()) {
            case "confirmed": return "Order Confirmed! ✅";
            case "processing": return "Order Processing 🔄";
            case "shipped": return "Order Shipped! 🚚";
            case "delivered": return "Order Delivered! 📦";
            case "cancelled": return "Order Cancelled ❌";
            default: return "Order Status Updated";
        }
    }

    private String getStatusMessage(Order order, String status) {
        if (status == null) return "";
        switch (status.toLowerCase()) {
            case "confirmed":
                return String.format("Your order #%s has been confirmed and is being prepared for processing.", order.getOrderNumber());
            case "processing":
                return String.format("Your order #%s is currently being processed and packed.", order.getOrderNumber());
            case "shipped":
                return String.format("Your order #%s has been shipped and is on its way to you!", order.getOrderNumber());
            case "delivered":
                return String.format("Your order #%s has been delivered successfully. We hope you love your purchase!", order.getOrderNumber());
            case "cancelled":
                return String.format("Your order #%s has been cancelled.", order.getOrderNumber());
            default:
                return String.format("Your order #%s status has been updated to: %s", order.getOrderNumber(), status);
        }
    }

    private String getStatusType(String status) {
        if (status == null) return "ORDER_STATUS";
        switch (status.toLowerCase()) {
            case "confirmed": return "ORDER_CONFIRMATION";
            case "processing": return "ORDER_PROCESSING";
            case "shipped": return "ORDER_SHIPPED";
            case "delivered": return "ORDER_DELIVERED";
            case "cancelled": return "ORDER_CANCELLED";
            default: return "ORDER_STATUS";
        }
    }

    private NotificationDTO toDTO(Notification notification) {
        return NotificationDTO.builder()
                .id(notification.getId())
                .userId(notification.getUserId())
                .orderId(notification.getOrderId())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .link(notification.getLink())
                .createdAt(notification.getCreatedAt())
                .readAt(notification.getReadAt())
                .build();
    }
}
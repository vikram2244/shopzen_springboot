// src/main/java/com/shopzen/ecommerce_api/dto/notification/NotificationDTO.java
package com.shopzen.ecommerce_api.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDTO {
    private String id;
    private String userId;
    private String orderId;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private String link;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
}
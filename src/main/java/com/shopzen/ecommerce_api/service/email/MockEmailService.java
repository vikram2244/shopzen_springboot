package com.shopzen.ecommerce_api.service.email;

import com.shopzen.ecommerce_api.entity.Order;
import com.shopzen.ecommerce_api.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("dev")
@Slf4j
public class MockEmailService extends EmailService {

    @Override
    public void sendVerificationEmail(String to, String token) {
        log.info("=========================================");
        log.info("📧 MOCK - Verification Email");
        log.info("To: {}", to);
        log.info("Token: {}", token);
        log.info("Verification URL: http://localhost:8080/api/auth/verify?token={}", token);
        log.info("=========================================");
    }

    @Override
    public void sendPasswordResetEmail(String to, String token) {
        log.info("=========================================");
        log.info("📧 MOCK - Password Reset Email");
        log.info("To: {}", to);
        log.info("Token: {}", token);
        log.info("Reset URL: http://localhost:8080/api/auth/reset-password?token={}", token);
        log.info("=========================================");
    }

    @Override
    public void sendOrderConfirmationEmail(Order order, User user) {
        log.info("=========================================");
        log.info("📧 MOCK - Order Confirmation Email");
        log.info("To: {}", user.getEmail());
        log.info("Order: {}", order.getOrderNumber());
        log.info("Total: ₹{}", order.getTotal());
        log.info("=========================================");
    }

    @Override
    public void sendOrderShippedEmail(Order order, User user) {
        log.info("=========================================");
        log.info("📧 MOCK - Order Shipped Email");
        log.info("To: {}", user.getEmail());
        log.info("Order: {}", order.getOrderNumber());
        log.info("=========================================");
    }

    @Override
    public void sendOrderDeliveredEmail(Order order, User user) {
        log.info("=========================================");
        log.info("📧 MOCK - Order Delivered Email");
        log.info("To: {}", user.getEmail());
        log.info("Order: {}", order.getOrderNumber());
        log.info("=========================================");
    }

    @Override
    public void sendOrderCancelledEmail(Order order, User user) {
        log.info("=========================================");
        log.info("📧 MOCK - Order Cancelled Email");
        log.info("To: {}", user.getEmail());
        log.info("Order: {}", order.getOrderNumber());
        log.info("=========================================");
    }

    @Override
    public void sendOrderStatusUpdateEmail(Order order, User user, String oldStatus, String newStatus) {
        log.info("=========================================");
        log.info("📧 MOCK - Order Status Update Email");
        log.info("To: {}", user.getEmail());
        log.info("Order: {}", order.getOrderNumber());
        log.info("Status: {} → {}", oldStatus, newStatus);
        log.info("=========================================");
    }

    @Override
    protected void sendEmail(String to, String subject, String htmlContent, String textContent) {
        log.info("📧 MOCK - sendEmail() called");
        log.info("To: {}", to);
        log.info("Subject: {}", subject);
        log.info("Text Content: {}", textContent);
    }
}
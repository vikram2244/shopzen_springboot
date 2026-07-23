package com.shopzen.ecommerce_api.service.email;

import com.shopzen.ecommerce_api.entity.Order;
import com.shopzen.ecommerce_api.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
@Slf4j
public class EmailService {

    @Value("${brevo.api.key:}")
    private String apiKey;

    @Value("${brevo.sender.email:vikramkids11@gmail.com}")
    private String fromEmail;

    @Value("${brevo.sender.name:ShopZen}")
    private String fromName;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${brevo.enabled:true}")
    private boolean brevoEnabled;

    private final RestTemplate restTemplate;

    public EmailService() {
        this.restTemplate = new RestTemplate();
    }

    @PostConstruct
    public void init() {
        log.info("📧 Email Service initialized");
        log.info("📧 Base URL: {}", baseUrl);
        log.info("📧 Sender: {} <{}>", fromName, fromEmail);
        log.info("📧 Brevo Enabled: {}", brevoEnabled);
        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("⚠️ Brevo API key is not set! Emails will be logged only.");
        }
    }

    // ==================== Verification & Password Reset ====================

    public void sendVerificationEmail(String to, String token) {
        try {
            String verificationUrl = baseUrl + "/api/auth/verify?token=" + token;
            
            log.info("🔗 Verification link: {}", verificationUrl);
            
            String htmlContent = buildVerificationEmailHtml(verificationUrl);
            String plainText = buildVerificationEmailPlain(verificationUrl);

            sendEmail(to, "Verify Your Email - ShopZen", htmlContent, plainText);
            log.info("✅ Verification email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send verification email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send verification email: " + e.getMessage());
        }
    }

    public void sendPasswordResetEmail(String to, String token) {
        try {
            String resetUrl = baseUrl + "/api/auth/reset-password?token=" + token;
            
            log.info("🔗 Password reset link: {}", resetUrl);
            
            String htmlContent = buildPasswordResetEmailHtml(resetUrl);
            String plainText = buildPasswordResetEmailPlain(resetUrl);

            sendEmail(to, "Reset Your Password - ShopZen", htmlContent, plainText);
            log.info("✅ Password reset email sent to: {}", to);

        } catch (Exception e) {
            log.error("❌ Failed to send password reset email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send password reset email: " + e.getMessage());
        }
    }

    // ==================== Email Sending Logic ====================

    protected void sendEmail(String to, String subject, String htmlContent, String textContent) {
        // If Brevo is disabled or API key is not set, just log
        if (!brevoEnabled || apiKey == null || apiKey.isEmpty()) {
            log.info("📧 MOCK: Email would be sent to: {}", to);
            log.info("📧 Subject: {}", subject);
            log.info("📧 Content preview: {}", 
                textContent != null ? textContent.substring(0, Math.min(100, textContent.length())) + "..." : "No text content");
            log.info("📧 HTML content preview: {}", 
                htmlContent != null ? htmlContent.substring(0, Math.min(100, htmlContent.length())) + "..." : "No HTML content");
            return;
        }

        String url = "https://api.brevo.com/v3/smtp/email";

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("sender", Map.of(
            "email", fromEmail,
            "name", fromName
        ));
        requestBody.put("to", new Object[]{Map.of("email", to)});
        requestBody.put("subject", subject);
        requestBody.put("htmlContent", htmlContent);
        if (textContent != null && !textContent.isEmpty()) {
            requestBody.put("textContent", textContent);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api-key", apiKey);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode() == HttpStatus.CREATED || response.getStatusCode() == HttpStatus.OK) {
                log.info("✅ Email sent successfully to: {}", to);
                log.info("🔗 Brevo API response: {}", response.getBody());
            } else {
                log.error("❌ Failed to send email to {}: Status: {}, Response: {}", 
                    to, response.getStatusCode(), response.getBody());
                throw new RuntimeException("Failed to send email: " + response.getBody());
            }
        } catch (Exception e) {
            log.error("❌ Error sending email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Failed to send email: " + e.getMessage(), e);
        }
    }

    // ==================== Email HTML Builders ====================

    private String buildVerificationEmailHtml(String verificationUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                    .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .header p { margin: 5px 0 0; opacity: 0.9; }
                    .content { padding: 40px 30px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #555; line-height: 1.6; }
                    .button { display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; text-decoration: none; border-radius: 8px; font-weight: 600; margin: 20px 0; }
                    .button:hover { opacity: 0.9; }
                    .code-box { background: #f0f0f0; padding: 15px; border-radius: 8px; word-break: break-all; font-family: monospace; font-size: 14px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    .footer a { color: #667eea; text-decoration: none; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🛍️ ShopZen</h1>
                        <p>Your Shopping Destination</p>
                    </div>
                    <div class="content">
                        <h2>Welcome to ShopZen! 🎉</h2>
                        <p>Thank you for registering. Please verify your email address to start shopping.</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">✅ Verify Email</a>
                        </div>
                        <p style="text-align: center; color: #888;">Or copy and paste this link in your browser:</p>
                        <div class="code-box">%s</div>
                        <p style="color: #999; font-size: 14px;">⏰ This link will expire in 24 hours.</p>
                        <p style="color: #999; font-size: 14px;">🔒 If you didn't create an account, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 ShopZen. All rights reserved.</p>
                        <p>Need help? <a href="mailto:%s">Contact Support</a></p>
                    </div>
                </div>
            </body>
            </html>
            """, verificationUrl, verificationUrl, fromEmail);
    }

    private String buildVerificationEmailPlain(String verificationUrl) {
        return String.format("""
            Welcome to ShopZen!
            
            Thank you for registering. Please verify your email address to start shopping.
            
            Verify your email by clicking this link:
            %s
            
            This link will expire in 24 hours.
            
            If you didn't create an account, please ignore this email.
            
            Thank you,
            ShopZen Team
            """, verificationUrl);
    }

    private String buildPasswordResetEmailHtml(String resetUrl) {
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <style>
                    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                    .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                    .header { background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; padding: 30px; text-align: center; }
                    .header h1 { margin: 0; font-size: 28px; }
                    .content { padding: 40px 30px; }
                    .content h2 { color: #333; margin-top: 0; }
                    .content p { color: #555; line-height: 1.6; }
                    .button { display: inline-block; padding: 14px 32px; background: linear-gradient(135deg, #f093fb 0%%, #f5576c 100%%); color: white; text-decoration: none; border-radius: 8px; font-weight: 600; margin: 20px 0; }
                    .button:hover { opacity: 0.9; }
                    .code-box { background: #f0f0f0; padding: 15px; border-radius: 8px; word-break: break-all; font-family: monospace; font-size: 14px; margin: 15px 0; }
                    .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🔐 ShopZen</h1>
                        <p>Password Reset</p>
                    </div>
                    <div class="content">
                        <h2>Reset Your Password</h2>
                        <p>We received a request to reset your password. Click the button below to create a new password.</p>
                        <div style="text-align: center;">
                            <a href="%s" class="button">🔐 Reset Password</a>
                        </div>
                        <p style="text-align: center; color: #888;">Or copy and paste this link in your browser:</p>
                        <div class="code-box">%s</div>
                        <p style="color: #999; font-size: 14px;">⏰ This link will expire in 1 hour.</p>
                        <p style="color: #999; font-size: 14px;">🔒 If you didn't request this, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 ShopZen. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """, resetUrl, resetUrl);
    }

    private String buildPasswordResetEmailPlain(String resetUrl) {
        return String.format("""
            Reset Your Password
            
            We received a request to reset your password.
            
            Click the link below to create a new password:
            %s
            
            This link will expire in 1 hour.
            
            If you didn't request this, please ignore this email.
            
            Thank you,
            ShopZen Team
            """, resetUrl);
    }

    // ==================== Order Email Methods ====================

    public void sendOrderConfirmationEmail(Order order, User user) {
        try {
            String subject = "Order Confirmation - ShopZen";
            
            StringBuilder itemsHtml = new StringBuilder();
            StringBuilder itemsPlain = new StringBuilder();
            
            if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
                order.getOrderItems().forEach(item -> {
                    String productName = item.getProductSnapshot() != null ? 
                        item.getProductSnapshot().getName() : "Product";
                    itemsHtml.append(String.format("""
                        <tr>
                            <td>%s</td>
                            <td style="text-align: center;">%d</td>
                            <td style="text-align: right;">₹%s</td>
                        </tr>
                        """,
                        productName,
                        item.getQuantity(),
                        item.getTotalPrice()
                    ));
                    itemsPlain.append(String.format("  • %s x %d = ₹%s\n",
                        productName,
                        item.getQuantity(),
                        item.getTotalPrice()
                    ));
                });
            } else {
                itemsHtml.append("<tr><td colspan=\"3\" style=\"text-align: center;\">No items found</td></tr>");
                itemsPlain.append("  No items found\n");
            }

            String formattedDate = order.getCreatedAt() != null ? 
                order.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM yyyy")) : 
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));

            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                        .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #4CAF50 0%%, #45a049 100%%); color: white; padding: 30px; text-align: center; }
                        .header h1 { margin: 0; font-size: 28px; }
                        .content { padding: 40px 30px; }
                        .content h2 { color: #333; margin-top: 0; }
                        .content p { color: #555; line-height: 1.6; }
                        .order-details { background: #f8f9fa; padding: 15px; border-radius: 8px; margin: 20px 0; }
                        table { width: 100%%; border-collapse: collapse; margin: 20px 0; }
                        th { background: #4CAF50; color: white; padding: 12px; text-align: left; }
                        td { padding: 12px; border-bottom: 1px solid #eee; }
                        .total { font-size: 20px; font-weight: bold; color: #4CAF50; text-align: right; }
                        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🛍️ Order Confirmed!</h1>
                            <p>Order #%s</p>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Thank you for your order! We're excited to confirm that your order has been received and is being processed.</p>
                            
                            <div class="order-details">
                                <p><strong>Order Number:</strong> #%s</p>
                                <p><strong>Order Date:</strong> %s</p>
                                <p><strong>Payment Method:</strong> %s</p>
                                <p><strong>Payment Status:</strong> %s</p>
                            </div>
                            
                            <h3>Order Summary</h3>
                            <table>
                                <tr>
                                    <th>Item</th>
                                    <th style="text-align: center;">Qty</th>
                                    <th style="text-align: right;">Price</th>
                                </tr>
                                %s
                            </table>
                            
                            <div style="border-top: 2px solid #eee; padding: 15px 0;">
                                <p><strong>Subtotal:</strong> ₹%s</p>
                                <p><strong>Shipping:</strong> ₹%s</p>
                                <p><strong>Tax:</strong> ₹%s</p>
                                <p class="total">Total: ₹%s</p>
                            </div>
                            
                            <p>You will receive another email when your order is shipped.</p>
                            <p style="color: #4CAF50; font-weight: 600;">Thank you for shopping with ShopZen! 🎉</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 ShopZen. All rights reserved.</p>
                            <p>Need help? <a href="mailto:%s" style="color: #4CAF50; text-decoration: none;">Contact Support</a></p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                order.getOrderNumber(),
                user.getFullName(),
                order.getOrderNumber(),
                formattedDate,
                order.getPaymentMethod() != null ? order.getPaymentMethod() : "Not specified",
                order.getPaymentStatus() != null ? order.getPaymentStatus() : "Pending",
                itemsHtml.toString(),
                order.getSubtotal() != null ? order.getSubtotal() : 0,
                order.getShippingCharge() != null ? order.getShippingCharge() : 0,
                order.getTax() != null ? order.getTax() : 0,
                order.getTotal() != null ? order.getTotal() : 0,
                fromEmail
            );

            String plainText = String.format("""
                Order Confirmation
                
                Hello %s,
                
                Order #%s has been confirmed.
                
                Order Summary:
                %s
                
                Subtotal: ₹%s
                Shipping: ₹%s
                Tax: ₹%s
                Total: ₹%s
                
                Thank you for shopping with ShopZen!
                """,
                user.getFullName(),
                order.getOrderNumber(),
                itemsPlain.toString(),
                order.getSubtotal() != null ? order.getSubtotal() : 0,
                order.getShippingCharge() != null ? order.getShippingCharge() : 0,
                order.getTax() != null ? order.getTax() : 0,
                order.getTotal() != null ? order.getTotal() : 0
            );

            sendEmail(user.getEmail(), subject, htmlContent, plainText);
            log.info("✅ Order confirmation email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send order confirmation email: {}", e.getMessage());
            throw new RuntimeException("Failed to send order confirmation email: " + e.getMessage());
        }
    }

    // Simplified order methods (same pattern as above)
    public void sendOrderShippedEmail(Order order, User user) {
        try {
            String subject = "Your Order Has Been Shipped! - ShopZen";
            String formattedDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMM yyyy"));
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                        .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #2196F3 0%%, #1976D2 100%%); color: white; padding: 30px; text-align: center; }
                        .content { padding: 40px 30px; }
                        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>🚚 Your Order Has Shipped!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Great news! Your order <strong>#%s</strong> has been shipped and is on its way to you!</p>
                            <div style="background: #e3f2fd; padding: 15px; border-radius: 8px; margin: 20px 0;">
                                <p><strong>📦 Order Details:</strong></p>
                                <p>Order Number: %s</p>
                                <p>Shipped Date: %s</p>
                                <p>Items: %d</p>
                                <p>Total: ₹%s</p>
                            </div>
                            <p>Expected delivery: 3-5 business days.</p>
                            <p style="color: #2196F3; font-weight: 600;">Thank you for shopping with ShopZen! 🎉</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 ShopZen. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                user.getFullName(),
                order.getOrderNumber(),
                order.getOrderNumber(),
                formattedDate,
                order.getOrderItems() != null ? order.getOrderItems().size() : 0,
                order.getTotal() != null ? order.getTotal() : 0
            );

            String plainText = String.format("""
                Order Shipped!
                
                Hello %s,
                
                Your order #%s has been shipped!
                
                Thank you for shopping with ShopZen!
                """,
                user.getFullName(),
                order.getOrderNumber()
            );

            sendEmail(user.getEmail(), subject, htmlContent, plainText);
            log.info("✅ Order shipped email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send order shipped email: {}", e.getMessage());
            throw new RuntimeException("Failed to send order shipped email: " + e.getMessage());
        }
    }

    public void sendOrderDeliveredEmail(Order order, User user) {
        try {
            String subject = "Your Order Has Been Delivered! - ShopZen";
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                        .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #4CAF50 0%%, #388E3C 100%%); color: white; padding: 30px; text-align: center; }
                        .content { padding: 40px 30px; }
                        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>📦 Delivered!</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Your order <strong>#%s</strong> has been successfully delivered!</p>
                            <div style="background: #e8f5e9; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center;">
                                <p style="font-size: 48px; margin: 0;">🎁</p>
                                <p style="font-size: 18px; font-weight: 600; color: #4CAF50;">We hope you love your purchase!</p>
                            </div>
                            <p>If you have any questions or concerns, please don't hesitate to contact us.</p>
                            <p style="color: #4CAF50; font-weight: 600;">Thank you for shopping with ShopZen! ❤️</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 ShopZen. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                user.getFullName(),
                order.getOrderNumber()
            );

            String plainText = String.format("""
                Order Delivered!
                
                Hello %s,
                
                Your order #%s has been delivered.
                
                Thank you for shopping with ShopZen!
                """,
                user.getFullName(),
                order.getOrderNumber()
            );

            sendEmail(user.getEmail(), subject, htmlContent, plainText);
            log.info("✅ Order delivered email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send order delivered email: {}", e.getMessage());
            throw new RuntimeException("Failed to send order delivered email: " + e.getMessage());
        }
    }

    public void sendOrderCancelledEmail(Order order, User user) {
        try {
            String subject = "Order Cancelled - ShopZen";
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                        .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #f44336 0%%, #c62828 100%%); color: white; padding: 30px; text-align: center; }
                        .content { padding: 40px 30px; }
                        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>❌ Order Cancelled</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s,</h2>
                            <p>Your order <strong>#%s</strong> has been cancelled.</p>
                            <p>If this was a mistake or you have any questions, please contact our support team.</p>
                            <p style="color: #666;">We hope to serve you again soon!</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 ShopZen. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                user.getFullName(),
                order.getOrderNumber()
            );

            String plainText = String.format("""
                Order Cancelled
                
                Hello %s,
                
                Your order #%s has been cancelled.
                
                Thank you for shopping with ShopZen!
                """,
                user.getFullName(),
                order.getOrderNumber()
            );

            sendEmail(user.getEmail(), subject, htmlContent, plainText);
            log.info("✅ Order cancelled email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send order cancelled email: {}", e.getMessage());
            throw new RuntimeException("Failed to send order cancelled email: " + e.getMessage());
        }
    }

    public void sendOrderStatusUpdateEmail(Order order, User user, String oldStatus, String newStatus) {
        try {
            String subject = "Order Status Update - ShopZen";
            String statusEmoji = getStatusEmoji(newStatus);
            
            String htmlContent = String.format("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <style>
                        body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; background: #f5f7fa; }
                        .container { background: white; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 6px rgba(0,0,0,0.1); }
                        .header { background: linear-gradient(135deg, #FF9800 0%%, #F57C00 100%%); color: white; padding: 30px; text-align: center; }
                        .content { padding: 40px 30px; }
                        .status-box { background: #fff3e0; padding: 15px; border-radius: 8px; margin: 20px 0; text-align: center; }
                        .status-box h3 { margin: 0; font-size: 24px; }
                        .footer { text-align: center; padding: 20px; color: #999; font-size: 12px; border-top: 1px solid #eee; }
                    </style>
                </head>
                <body>
                    <div class="container">
                        <div class="header">
                            <h1>%s Status Update</h1>
                        </div>
                        <div class="content">
                            <h2>Hello %s! 👋</h2>
                            <p>Your order <strong>#%s</strong> has been updated!</p>
                            <div class="status-box">
                                <h3>%s Order Status: <span style="color: #FF9800;">%s</span></h3>
                            </div>
                            <p>You can track your order status anytime by logging into your account.</p>
                            <p style="color: #FF9800; font-weight: 600;">Thank you for shopping with ShopZen!</p>
                        </div>
                        <div class="footer">
                            <p>© 2024 ShopZen. All rights reserved.</p>
                        </div>
                    </div>
                </body>
                </html>
                """,
                statusEmoji,
                user.getFullName(),
                order.getOrderNumber(),
                statusEmoji,
                newStatus.toUpperCase()
            );

            String plainText = String.format("""
                Order Status Update
                
                Hello %s,
                
                Your order #%s has been updated to: %s
                
                Thank you for shopping with ShopZen!
                """,
                user.getFullName(),
                order.getOrderNumber(),
                newStatus.toUpperCase()
            );

            sendEmail(user.getEmail(), subject, htmlContent, plainText);
            log.info("✅ Order status update email sent to: {}", user.getEmail());

        } catch (Exception e) {
            log.error("❌ Failed to send order status update email: {}", e.getMessage());
            throw new RuntimeException("Failed to send order status update email: " + e.getMessage());
        }
    }

    // ==================== Helper Methods ====================

    private String getStatusEmoji(String status) {
        if (status == null) return "📦";
        return switch (status.toLowerCase()) {
            case "pending" -> "⏳";
            case "confirmed" -> "✅";
            case "processing" -> "🔄";
            case "shipped" -> "🚚";
            case "delivered" -> "📦";
            case "cancelled" -> "❌";
            default -> "📦";
        };
    }
}
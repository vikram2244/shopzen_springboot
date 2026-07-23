// src/main/java/com/shopzen/ecommerce_api/controller/auth/AuthController.java
package com.shopzen.ecommerce_api.controller.auth;

import com.shopzen.ecommerce_api.dto.auth.AuthResponse;
import com.shopzen.ecommerce_api.dto.auth.ForgotPasswordRequest;
import com.shopzen.ecommerce_api.dto.auth.LoginRequest;
import com.shopzen.ecommerce_api.dto.auth.RegisterRequest;
import com.shopzen.ecommerce_api.dto.auth.ResetPasswordRequest;
import com.shopzen.ecommerce_api.entity.User;
import com.shopzen.ecommerce_api.security.jwt.JwtTokenProvider;
import com.shopzen.ecommerce_api.service.user.UserService;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.frontend-url:https://shopgen.netlify.app}")
    private String frontendUrl;

    @Value("${app.base-url:https://shopzen-springboot.onrender.com}")
    private String baseUrl;
    
    @PostConstruct
    public void init() {
        log.info("🔍 AuthController Configuration:");
        log.info("   frontendUrl: {}", frontendUrl);
        log.info("   baseUrl: {}", baseUrl);
    }

 // Update the register method in AuthController.java

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            log.info("📝 Register request: firstName={}, lastName={}, email={}, isAdmin={}", 
                request.getFirstName(), 
                request.getLastName(), 
                request.getEmail(),
                request.getIsAdmin()
            );
            
            User user = userService.registerUser(request);
            
            AuthResponse response = AuthResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .isAdmin(user.getIsAdmin())
                    .message("Registration successful. Please verify your email before logging in.")
                    .build();
            
            log.info("✅ User registered successfully: {} (isAdmin: {}, emailVerified: {}, isPending: {})", 
                user.getEmail(), user.getIsAdmin(), user.getEmailVerified(), user.getIsPending());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (Exception e) {
            log.error("❌ Registration error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(Map.of(
                "message", "Registration failed: " + e.getMessage(),
                "error", e.getMessage()
            ));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            log.info("🔐 Login request for: {}", request.getEmail());
            
            User user = userService.findByEmail(request.getEmail());
            
            // Check if user is pending (not verified)
            if (user.getIsPending() != null && user.getIsPending()) {
                log.warn("⚠️ Login attempt for unverified (pending) email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Please verify your email before logging in. Check your inbox for the verification link.");
            }
            
            if (!user.getEmailVerified()) {
                log.warn("⚠️ Login attempt for unverified email: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Please verify your email before logging in. Check your inbox for the verification link.");
            }
            
            if (!user.getIsActive()) {
                log.warn("⚠️ Login attempt for inactive account: {}", request.getEmail());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Account is deactivated. Please contact support.");
            }
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String token = tokenProvider.generateToken(user.getId(), user.getEmail());
            
            AuthResponse response = AuthResponse.builder()
                    .token(token)
                    .id(user.getId())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .fullName(user.getFirstName() + " " + user.getLastName())
                    .isAdmin(user.getIsAdmin())
                    .message("Login successful")
                    .build();
            
            log.info("✅ User logged in: {} (isAdmin: {})", user.getEmail(), user.getIsAdmin());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("❌ Login error: {}", e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }
    }
    
    @GetMapping("/verify")
    public ResponseEntity<String> verifyEmail(@RequestParam String token) {
        try {
            log.info("📧 Verifying email with token: {}", token);
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(getVerificationErrorHTML("Verification token is required", frontendUrl));
            }
            
            User user = userService.verifyUser(token);
            log.info("✅ Email verified successfully for: {}", user.getEmail());
            
            return ResponseEntity.ok()
                .header("Content-Type", "text/html")
                .body(getVerificationSuccessHTML(user.getEmail(), frontendUrl));
            
        } catch (Exception e) {
            log.error("❌ Email verification error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest()
                .header("Content-Type", "text/html")
                .body(getVerificationErrorHTML(e.getMessage(), frontendUrl));
        }
    }
    
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestParam String email) {
        try {
            log.info("📧 Resending verification email for: {}", email);
            userService.resendVerificationEmail(email);
            log.info("✅ Verification email resent for: {}", email);
            return ResponseEntity.ok("Verification email sent successfully. Please check your inbox.");
        } catch (Exception e) {
            log.error("❌ Resend verification error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to resend verification email: " + e.getMessage());
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        try {
            log.info("🔑 Forgot password request for: {}", request.getEmail());
            
            if (!userService.existsByEmail(request.getEmail())) {
                return ResponseEntity.ok("If the email exists, a password reset link has been sent");
            }
            
            userService.createPasswordResetToken(request.getEmail());
            log.info("✅ Password reset token created for: {}", request.getEmail());
            
            return ResponseEntity.ok("If the email exists, a password reset link has been sent");
            
        } catch (Exception e) {
            log.error("❌ Forgot password error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to process password reset request");
        }
    }

    @GetMapping("/reset-password")
    public ResponseEntity<String> showResetPasswordPage(@RequestParam String token) {
        log.info("📄 Showing reset password page for token: {}", token);
        
        if (token == null || token.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(getResetPasswordErrorHTML("Invalid reset token", frontendUrl));
        }
        
        return ResponseEntity.ok()
            .header("Content-Type", "text/html")
            .body(getResetPasswordHTML(token, frontendUrl, baseUrl));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        try {
            log.info("🔑 Reset password request received");
            userService.resetPassword(request.getToken(), request.getNewPassword());
            log.info("✅ Password reset successfully");
            return ResponseEntity.ok("Password reset successfully");
        } catch (Exception e) {
            log.error("❌ Reset password error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Password reset failed: " + e.getMessage());
        }
    }
    
    @PostMapping("/make-admin/{userId}")
    public ResponseEntity<?> makeAdmin(@PathVariable String userId) {
        try {
            log.info("👑 Making user admin: {}", userId);
            User user = userService.makeAdmin(userId);
            log.info("✅ User made admin: {} (isAdmin: {})", user.getEmail(), user.getIsAdmin());
            return ResponseEntity.ok("User made admin successfully");
        } catch (Exception e) {
            log.error("❌ Make admin error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Failed to make user admin: " + e.getMessage());
        }
    }

    // ==================== HTML Builder Methods ====================

    private String getVerificationSuccessHTML(String email, String frontendUrl) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Email Verified - ShopZen</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n" +
            "            background: linear-gradient(135deg, #667eea 0" + "%" + ", #764ba2 100" + "%" + ");\n" +
            "            min-height: 100vh;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        .container {\n" +
            "            background: white;\n" +
            "            border-radius: 20px;\n" +
            "            padding: 50px 40px;\n" +
            "            max-width: 500px;\n" +
            "            width: 100" + "%" + ";\n" +
            "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .icon { font-size: 64px; margin-bottom: 20px; }\n" +
            "        .success-icon { color: #28a745; }\n" +
            "        h1 { color: #333; font-size: 28px; margin-bottom: 15px; }\n" +
            "        p { color: #666; font-size: 16px; line-height: 1.6; margin-bottom: 25px; }\n" +
            "        .email {\n" +
            "            background: #f8f9fa;\n" +
            "            padding: 10px;\n" +
            "            border-radius: 8px;\n" +
            "            color: #007bff;\n" +
            "            font-weight: bold;\n" +
            "            margin: 10px 0 20px 0;\n" +
            "            word-break: break-all;\n" +
            "        }\n" +
            "        .btn {\n" +
            "            display: inline-block;\n" +
            "            padding: 14px 40px;\n" +
            "            background: linear-gradient(135deg, #667eea 0" + "%" + ", #764ba2 100" + "%" + ");\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 50px;\n" +
            "            font-weight: 600;\n" +
            "            transition: transform 0.2s, box-shadow 0.2s;\n" +
            "            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);\n" +
            "        }\n" +
            "        .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6); }\n" +
            "        .footer { margin-top: 20px; color: #999; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"icon success-icon\">✅</div>\n" +
            "        <h1>Email Verified!</h1>\n" +
            "        <p>Your email address has been successfully verified.</p>\n" +
            "        <div class=\"email\">📧 " + email + "</div>\n" +
            "        <p>You can now login to your ShopZen account and start shopping!</p>\n" +
            "        <a href=\"" + frontendUrl + "/login\" class=\"btn\">Login to ShopZen</a>\n" +
            "        <div class=\"footer\">Thank you for joining ShopZen!</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }

    private String getVerificationErrorHTML(String message, String frontendUrl) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Verification Failed - ShopZen</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n" +
            "            background: linear-gradient(135deg, #f093fb 0" + "%" + ", #f5576c 100" + "%" + ");\n" +
            "            min-height: 100vh;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        .container {\n" +
            "            background: white;\n" +
            "            border-radius: 20px;\n" +
            "            padding: 50px 40px;\n" +
            "            max-width: 500px;\n" +
            "            width: 100" + "%" + ";\n" +
            "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .icon { font-size: 64px; margin-bottom: 20px; }\n" +
            "        .error-icon { color: #dc3545; }\n" +
            "        h1 { color: #333; font-size: 28px; margin-bottom: 15px; }\n" +
            "        .error-message {\n" +
            "            background: #f8d7da;\n" +
            "            color: #721c24;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            margin: 10px 0 20px 0;\n" +
            "            border: 1px solid #f5c6cb;\n" +
            "            word-break: break-word;\n" +
            "        }\n" +
            "        .btn {\n" +
            "            display: inline-block;\n" +
            "            padding: 14px 40px;\n" +
            "            background: linear-gradient(135deg, #f093fb 0" + "%" + ", #f5576c 100" + "%" + ");\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 50px;\n" +
            "            font-weight: 600;\n" +
            "            transition: transform 0.2s, box-shadow 0.2s;\n" +
            "            box-shadow: 0 4px 15px rgba(245, 87, 108, 0.4);\n" +
            "        }\n" +
            "        .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(245, 87, 108, 0.6); }\n" +
            "        .footer { margin-top: 20px; color: #999; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"icon error-icon\">❌</div>\n" +
            "        <h1>Verification Failed</h1>\n" +
            "        <div class=\"error-message\">" + message + "</div>\n" +
            "        <p>Please try again or contact support if the problem persists.</p>\n" +
            "        <a href=\"" + frontendUrl + "/register\" class=\"btn\">Back to Register</a>\n" +
            "        <div class=\"footer\">Need help? Contact our support team.</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }

    private String getResetPasswordHTML(String token, String frontendUrl, String baseUrl) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Reset Password - ShopZen</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n" +
            "            background: linear-gradient(135deg, #667eea 0" + "%" + ", #764ba2 100" + "%" + ");\n" +
            "            min-height: 100vh;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        .container {\n" +
            "            background: white;\n" +
            "            border-radius: 20px;\n" +
            "            padding: 50px 40px;\n" +
            "            max-width: 450px;\n" +
            "            width: 100" + "%" + ";\n" +
            "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
            "        }\n" +
            "        .logo {\n" +
            "            text-align: center;\n" +
            "            font-size: 28px;\n" +
            "            font-weight: bold;\n" +
            "            color: #667eea;\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "        h2 {\n" +
            "            color: #333;\n" +
            "            font-size: 24px;\n" +
            "            margin-bottom: 10px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .subtitle {\n" +
            "            color: #666;\n" +
            "            font-size: 14px;\n" +
            "            text-align: center;\n" +
            "            margin-bottom: 30px;\n" +
            "        }\n" +
            "        .form-group {\n" +
            "            margin-bottom: 20px;\n" +
            "        }\n" +
            "        label {\n" +
            "            display: block;\n" +
            "            color: #555;\n" +
            "            font-size: 14px;\n" +
            "            font-weight: 600;\n" +
            "            margin-bottom: 5px;\n" +
            "        }\n" +
            "        input {\n" +
            "            width: 100" + "%" + ";\n" +
            "            padding: 12px 15px;\n" +
            "            border: 2px solid #e0e0e0;\n" +
            "            border-radius: 10px;\n" +
            "            font-size: 16px;\n" +
            "            transition: border-color 0.3s;\n" +
            "            outline: none;\n" +
            "        }\n" +
            "        input:focus {\n" +
            "            border-color: #667eea;\n" +
            "        }\n" +
            "        .btn {\n" +
            "            width: 100" + "%" + ";\n" +
            "            padding: 14px;\n" +
            "            background: linear-gradient(135deg, #667eea 0" + "%" + ", #764ba2 100" + "%" + ");\n" +
            "            color: white;\n" +
            "            border: none;\n" +
            "            border-radius: 10px;\n" +
            "            font-size: 16px;\n" +
            "            font-weight: 600;\n" +
            "            cursor: pointer;\n" +
            "            transition: transform 0.2s, box-shadow 0.2s;\n" +
            "            box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);\n" +
            "        }\n" +
            "        .btn:hover {\n" +
            "            transform: translateY(-2px);\n" +
            "            box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);\n" +
            "        }\n" +
            "        .error {\n" +
            "            color: #dc3545;\n" +
            "            font-size: 14px;\n" +
            "            margin-top: 10px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .success {\n" +
            "            color: #28a745;\n" +
            "            font-size: 14px;\n" +
            "            margin-top: 10px;\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .footer {\n" +
            "            margin-top: 20px;\n" +
            "            text-align: center;\n" +
            "            color: #999;\n" +
            "            font-size: 14px;\n" +
            "        }\n" +
            "        .footer a {\n" +
            "            color: #667eea;\n" +
            "            text-decoration: none;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"logo\">🛍️ ShopZen</div>\n" +
            "        <h2>Reset Your Password</h2>\n" +
            "        <p class=\"subtitle\">Enter your new password below</p>\n" +
            "        <form id=\"resetForm\" onsubmit=\"resetPassword(event)\">\n" +
            "            <input type=\"hidden\" id=\"token\" value=\"" + token + "\">\n" +
            "            <div class=\"form-group\">\n" +
            "                <label for=\"newPassword\">New Password</label>\n" +
            "                <input type=\"password\" id=\"newPassword\" placeholder=\"Enter new password\" required minlength=\"6\">\n" +
            "            </div>\n" +
            "            <div class=\"form-group\">\n" +
            "                <label for=\"confirmPassword\">Confirm Password</label>\n" +
            "                <input type=\"password\" id=\"confirmPassword\" placeholder=\"Confirm new password\" required minlength=\"6\">\n" +
            "            </div>\n" +
            "            <button type=\"submit\" class=\"btn\">Reset Password</button>\n" +
            "            <div id=\"message\"></div>\n" +
            "        </form>\n" +
            "        <div class=\"footer\">\n" +
            "            <a href=\"" + frontendUrl + "/login\">Back to Login</a>\n" +
            "        </div>\n" +
            "    </div>\n" +
            "    <script>\n" +
            "        const frontendUrl = '" + frontendUrl + "';\n" +
            "        const baseUrl = '" + baseUrl + "';\n" +
            "        \n" +
            "        async function resetPassword(event) {\n" +
            "            event.preventDefault();\n" +
            "            const token = document.getElementById('token').value;\n" +
            "            const newPassword = document.getElementById('newPassword').value;\n" +
            "            const confirmPassword = document.getElementById('confirmPassword').value;\n" +
            "            const messageDiv = document.getElementById('message');\n" +
            "            \n" +
            "            if (newPassword.length < 6) {\n" +
            "                messageDiv.innerHTML = '<div class=\"error\">Password must be at least 6 characters</div>';\n" +
            "                return;\n" +
            "            }\n" +
            "            if (newPassword !== confirmPassword) {\n" +
            "                messageDiv.innerHTML = '<div class=\"error\">Passwords do not match</div>';\n" +
            "                return;\n" +
            "            }\n" +
            "            \n" +
            "            try {\n" +
            "                const response = await fetch(baseUrl + '/api/auth/reset-password', {\n" +
            "                    method: 'POST',\n" +
            "                    headers: { 'Content-Type': 'application/json' },\n" +
            "                    body: JSON.stringify({ token: token, newPassword: newPassword })\n" +
            "                });\n" +
            "                const data = await response.text();\n" +
            "                if (response.ok) {\n" +
            "                    messageDiv.innerHTML = '<div class=\"success\">✅ Password reset successfully! Redirecting to login...</div>';\n" +
            "                    setTimeout(() => { window.location.href = frontendUrl + '/login'; }, 2000);\n" +
            "                } else {\n" +
            "                    messageDiv.innerHTML = '<div class=\"error\">❌ ' + data + '</div>';\n" +
            "                }\n" +
            "            } catch (error) {\n" +
            "                messageDiv.innerHTML = '<div class=\"error\">❌ An error occurred. Please try again.</div>';\n" +
            "            }\n" +
            "        }\n" +
            "    </script>\n" +
            "</body>\n" +
            "</html>";
    }

    private String getResetPasswordErrorHTML(String message, String frontendUrl) {
        return "<!DOCTYPE html>\n" +
            "<html>\n" +
            "<head>\n" +
            "    <meta charset=\"UTF-8\">\n" +
            "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "    <title>Invalid Reset Link - ShopZen</title>\n" +
            "    <style>\n" +
            "        * { margin: 0; padding: 0; box-sizing: border-box; }\n" +
            "        body {\n" +
            "            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Arial, sans-serif;\n" +
            "            background: linear-gradient(135deg, #f093fb 0" + "%" + ", #f5576c 100" + "%" + ");\n" +
            "            min-height: 100vh;\n" +
            "            display: flex;\n" +
            "            justify-content: center;\n" +
            "            align-items: center;\n" +
            "            margin: 0;\n" +
            "            padding: 20px;\n" +
            "        }\n" +
            "        .container {\n" +
            "            background: white;\n" +
            "            border-radius: 20px;\n" +
            "            padding: 50px 40px;\n" +
            "            max-width: 450px;\n" +
            "            width: 100" + "%" + ";\n" +
            "            box-shadow: 0 20px 60px rgba(0,0,0,0.3);\n" +
            "            text-align: center;\n" +
            "        }\n" +
            "        .icon { font-size: 64px; margin-bottom: 20px; }\n" +
            "        .error-icon { color: #dc3545; }\n" +
            "        h1 { color: #333; font-size: 28px; margin-bottom: 15px; }\n" +
            "        .error-message {\n" +
            "            background: #f8d7da;\n" +
            "            color: #721c24;\n" +
            "            padding: 15px;\n" +
            "            border-radius: 8px;\n" +
            "            margin: 10px 0 20px 0;\n" +
            "            border: 1px solid #f5c6cb;\n" +
            "        }\n" +
            "        .btn {\n" +
            "            display: inline-block;\n" +
            "            padding: 14px 40px;\n" +
            "            background: linear-gradient(135deg, #f093fb 0" + "%" + ", #f5576c 100" + "%" + ");\n" +
            "            color: white;\n" +
            "            text-decoration: none;\n" +
            "            border-radius: 50px;\n" +
            "            font-weight: 600;\n" +
            "            transition: transform 0.2s, box-shadow 0.2s;\n" +
            "            box-shadow: 0 4px 15px rgba(245, 87, 108, 0.4);\n" +
            "        }\n" +
            "        .btn:hover { transform: translateY(-2px); box-shadow: 0 6px 20px rgba(245, 87, 108, 0.6); }\n" +
            "        .footer { margin-top: 20px; color: #999; font-size: 14px; }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "    <div class=\"container\">\n" +
            "        <div class=\"icon error-icon\">❌</div>\n" +
            "        <h1>Invalid Reset Link</h1>\n" +
            "        <div class=\"error-message\">" + message + "</div>\n" +
            "        <p>Please request a new password reset link.</p>\n" +
            "        <a href=\"" + frontendUrl + "/forgot-password\" class=\"btn\">Request New Link</a>\n" +
            "        <div class=\"footer\">Need help? Contact our support team.</div>\n" +
            "    </div>\n" +
            "</body>\n" +
            "</html>";
    }
}
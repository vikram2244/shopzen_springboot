// src/main/java/com/shopzen/ecommerce_api/security/jwt/SimpleJwtTokenProvider.java
package com.shopzen.ecommerce_api.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class SimpleJwtTokenProvider {

    @Value("${app.jwtSecret:your-256-bit-secret-key-for-jwt-signing-please-change-this-in-production}")
    private String jwtSecret;

    @Value("${app.jwtExpirationMs:86400000}")
    private long jwtExpirationMs;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String generateToken(String userId, String email) {
        try {
            Map<String, Object> header = new HashMap<>();
            header.put("alg", "HS256");
            header.put("typ", "JWT");
            String headerJson = objectMapper.writeValueAsString(header);
            String headerBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(headerJson.getBytes(StandardCharsets.UTF_8));
            long now = System.currentTimeMillis();
            Map<String, Object> payload = new HashMap<>();
            payload.put("sub", userId);
            payload.put("email", email);
            payload.put("iat", now / 1000);
            payload.put("exp", (now + jwtExpirationMs) / 1000);
            String payloadJson = objectMapper.writeValueAsString(payload);
            String payloadBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signatureInput = headerBase64 + "." + payloadBase64;
            String signature = hmacSha256(signatureInput, jwtSecret);
            String signatureBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(signature.getBytes(StandardCharsets.UTF_8));

            return headerBase64 + "." + payloadBase64 + "." + signatureBase64;

        } catch (Exception e) {
            log.error("Error generating token: {}", e.getMessage(), e);
            return null;
        }
    }

    public String getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]), 
                StandardCharsets.UTF_8
            );
            
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            return (String) payload.get("sub");
        } catch (Exception e) {
            log.error("Error extracting userId: {}", e.getMessage());
            return null;
        }
    }

    public String getEmailFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return null;

            String payloadJson = new String(
                Base64.getUrlDecoder().decode(parts[1]), 
                StandardCharsets.UTF_8
            );
            
            Map<String, Object> payload = objectMapper.readValue(payloadJson, Map.class);
            return (String) payload.get("email");
        } catch (Exception e) {
            log.error("Error extracting email: {}", e.getMessage());
            return null;
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;

            String header = parts[0];
            String payload = parts[1];
            String signature = parts[2];

            String signatureInput = header + "." + payload;
            String expectedSignature = hmacSha256(signatureInput, jwtSecret);
            String expectedSignatureBase64 = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(expectedSignature.getBytes(StandardCharsets.UTF_8));

            if (!signature.equals(expectedSignatureBase64)) {
                log.warn("Invalid signature");
                return false;
            }

            String payloadJson = new String(
                Base64.getUrlDecoder().decode(payload), 
                StandardCharsets.UTF_8
            );
            Map<String, Object> payloadMap = objectMapper.readValue(payloadJson, Map.class);
            
            Long exp = (Long) payloadMap.get("exp");
            if (exp != null) {
                long now = System.currentTimeMillis() / 1000;
                if (exp < now) {
                    log.warn("Token has expired");
                    return false;
                }
            }

            return true;

        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }

    private String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                key.getBytes(StandardCharsets.UTF_8), 
                "HmacSHA256"
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hash);
        } catch (Exception e) {
            log.error("Error generating HMAC: {}", e.getMessage());
            return null;
        }
    }
}
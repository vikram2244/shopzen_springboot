package com.shopzen.ecommerce_api.service.payment;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.shopzen.ecommerce_api.dto.payment.CreateOrderRequest;
import com.shopzen.ecommerce_api.dto.payment.CreateOrderResponse;
import com.shopzen.ecommerce_api.dto.payment.PaymentVerificationRequest;
import com.shopzen.ecommerce_api.dto.payment.PaymentVerificationResponse;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Slf4j
@Service
public class RazorpayService {

    @Value("${razorpay.key.id:rzp_test_dummy}")
    private String razorpayKeyId;

    @Value("${razorpay.key.secret:dummy_secret}")
    private String razorpayKeySecret;

    private RazorpayClient razorpayClient;

    @PostConstruct
    public void init() {
        try {
            this.razorpayClient = new RazorpayClient(razorpayKeyId, razorpayKeySecret);
            log.info("Razorpay client initialized successfully with key: {}", razorpayKeyId);
        } catch (RazorpayException e) {
            log.error("Failed to initialize Razorpay client: {}", e.getMessage());
        }
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request) throws RazorpayException {
        try {
            log.info("Creating order with amount: {}", request.getAmount());
            
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", request.getAmount() * 100); // Amount in paise
            orderRequest.put("currency", request.getCurrency() != null ? request.getCurrency() : "INR");
            orderRequest.put("receipt", request.getReceipt() != null ? request.getReceipt() : "receipt_" + System.currentTimeMillis());
            JSONObject notes = new JSONObject();
            notes.put("order_id", request.getOrderId());
            notes.put("user_id", request.getUserId());
            if (request.getNotes() != null) {
                request.getNotes().forEach(notes::put);
            }
            orderRequest.put("notes", notes);

            Order order = razorpayClient.orders.create(orderRequest);

            CreateOrderResponse response = new CreateOrderResponse();
            response.setOrderId(order.get("id"));
            response.setAmount(order.get("amount"));
            response.setCurrency(order.get("currency"));
            response.setReceipt(order.get("receipt"));
            response.setStatus(order.get("status"));
            
            log.info("Order created successfully: {}", response.getOrderId());
            return response;
            
        } catch (RazorpayException e) {
            log.error("Razorpay exception: {}", e.getMessage());
            throw new RazorpayException("Failed to create Razorpay order: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            throw new RazorpayException("Unexpected error: " + e.getMessage());
        }
    }

    public PaymentVerificationResponse verifyPayment(PaymentVerificationRequest request) {
        PaymentVerificationResponse response = new PaymentVerificationResponse();
        
        try {
            log.info("Verifying payment for order: {}", request.getRazorpayOrderId());
            String generatedSignature = generateSignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId()
            );
            if (generatedSignature.equals(request.getRazorpaySignature())) {
                response.setValid(true);
                response.setMessage("Payment verified successfully");
                log.info("Payment verified successfully for order: {}", request.getRazorpayOrderId());
            } else {
                response.setValid(false);
                response.setMessage("Payment verification failed: Invalid signature");
                log.warn("Invalid signature for order: {}", request.getRazorpayOrderId());
            }
        } catch (Exception e) {
            log.error("Verification error: {}", e.getMessage());
            response.setValid(false);
            response.setMessage("Payment verification failed: " + e.getMessage());
        }
        
        return response;
    }

    private String generateSignature(String orderId, String paymentId) 
            throws NoSuchAlgorithmException, InvalidKeyException {
        String data = orderId + "|" + paymentId;
        
        Mac hmacSha256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKey = new SecretKeySpec(
            razorpayKeySecret.getBytes(StandardCharsets.UTF_8),
            "HmacSHA256"
        );
        hmacSha256.init(secretKey);
        
        byte[] hash = hmacSha256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hash);
    }

    public String getRazorpayKeyId() {
        return razorpayKeyId;
    }
}
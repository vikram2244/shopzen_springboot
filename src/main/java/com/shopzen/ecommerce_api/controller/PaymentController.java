package com.shopzen.ecommerce_api.controller;

import com.razorpay.RazorpayException;
import com.shopzen.ecommerce_api.dto.payment.CreateOrderRequest;
import com.shopzen.ecommerce_api.dto.payment.CreateOrderResponse;
import com.shopzen.ecommerce_api.dto.payment.PaymentVerificationRequest;
import com.shopzen.ecommerce_api.dto.payment.PaymentVerificationResponse;
import com.shopzen.ecommerce_api.service.payment.RazorpayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final RazorpayService razorpayService;

    @GetMapping("/ping")
    public ResponseEntity<Map<String, String>> ping() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "Payment controller is working!");
        response.put("timestamp", String.valueOf(System.currentTimeMillis()));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/create-order")
    public ResponseEntity<?> createOrder(@RequestBody CreateOrderRequest request) {
        try {
            log.info("Creating Razorpay order for amount: {}", request.getAmount());
            CreateOrderResponse response = razorpayService.createOrder(request);
            log.info("Razorpay order created: {}", response.getOrderId());
            return ResponseEntity.ok(response);
        } catch (RazorpayException e) {
            log.error("Failed to create Razorpay order", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            error.put("status", "FAILED");
            return ResponseEntity.internalServerError().body(error);
        } catch (Exception e) {
            log.error("Unexpected error", e);
            Map<String, String> error = new HashMap<>();
            error.put("error", "An unexpected error occurred");
            error.put("status", "FAILED");
            return ResponseEntity.internalServerError().body(error);
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<PaymentVerificationResponse> verifyPayment(@RequestBody PaymentVerificationRequest request) {
        try {
            log.info("Verifying payment for order: {}", request.getOrderId());
            PaymentVerificationResponse response = razorpayService.verifyPayment(request);
            log.info("Payment verification result: {}", response.isValid());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Verification error", e);
            PaymentVerificationResponse errorResponse = new PaymentVerificationResponse();
            errorResponse.setValid(false);
            errorResponse.setMessage("Verification failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}
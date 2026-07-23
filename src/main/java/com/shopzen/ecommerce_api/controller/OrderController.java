package com.shopzen.ecommerce_api.controller;

import com.shopzen.ecommerce_api.dto.order.OrderDTO;
import com.shopzen.ecommerce_api.dto.order.OrderRequestDTO;
import com.shopzen.ecommerce_api.service.order.OrderService;
import com.shopzen.ecommerce_api.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Tag(name = "Order API", description = "Order management endpoints for users")
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Creates a new order from the current user's cart")
    public ResponseEntity<?> createOrder(@Valid @RequestBody OrderRequestDTO request) {
        try {
            String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
            log.info("Creating order for user: {}", userId);
            log.info("Order request: {}", request);
            
            OrderDTO order = orderService.createOrder(userId, request);
            
            log.info("Order created successfully: {}", order.getId());
            return ResponseEntity.ok(order);
            
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "FAILED");
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @GetMapping
    @Operation(summary = "Get current user's orders", description = "Retrieves all orders for the authenticated user")
    public ResponseEntity<List<OrderDTO>> getUserOrders() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        return ResponseEntity.ok(orderService.getUserOrders(userId));
    }

    @GetMapping("/{orderId}")
    @Operation(summary = "Get specific order by ID", description = "Retrieves a specific order by its ID")
    public ResponseEntity<OrderDTO> getOrder(@PathVariable String orderId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        return ResponseEntity.ok(orderService.getOrder(userId, orderId));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by order number", description = "Retrieves an order by its unique order number")
    public ResponseEntity<OrderDTO> getOrderByNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }
}
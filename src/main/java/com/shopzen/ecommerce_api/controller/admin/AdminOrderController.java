package com.shopzen.ecommerce_api.controller.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.order.OrderDTO;
import com.shopzen.ecommerce_api.service.order.OrderService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Order API", description = "Admin order management")
@Slf4j
public class AdminOrderController {

    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders created by the current admin")
    public ResponseEntity<Page<OrderDTO>> getAllOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status) {
        
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} fetching orders", adminUserId);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        Page<OrderDTO> orders;
        if (status != null && !status.isEmpty() && !"all".equalsIgnoreCase(status)) {
            orders = orderService.getOrdersByStatusAndAdmin(status, adminUserId, pageable);
        } else {
            orders = orderService.getOrdersByAdmin(adminUserId, pageable);
        }
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/my-orders")
    @Operation(summary = "Get orders created by the current admin (alias)")
    public ResponseEntity<Page<OrderDTO>> getMyOrders(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(orderService.getOrdersByAdmin(adminUserId, pageable));
    }

    @PutMapping("/{orderId}/status")
    @Operation(summary = "Update order status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @PathVariable String orderId,  // Changed from UUID to String
            @RequestParam String status) {
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Admin {} updating order {} status to: {}", adminUserId, orderId, status);
        
        OrderDTO updatedOrder = orderService.updateOrderStatus(adminUserId, orderId, status);
        return ResponseEntity.ok(updatedOrder);
    }

    @GetMapping("/{orderId}/can-update")
    @Operation(summary = "Check if current admin can update the order")
    public ResponseEntity<Boolean> canUpdateOrder(@PathVariable String orderId) {  // Changed from UUID to String
        String adminUserId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        boolean canUpdate = orderService.canUpdateOrder(adminUserId, orderId);
        return ResponseEntity.ok(canUpdate);
    }
}
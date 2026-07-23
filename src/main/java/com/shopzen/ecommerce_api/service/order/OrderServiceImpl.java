package com.shopzen.ecommerce_api.service.order;

import com.shopzen.ecommerce_api.dto.order.OrderDTO;
import com.shopzen.ecommerce_api.dto.order.OrderRequestDTO;
import com.shopzen.ecommerce_api.entity.*;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.exception.UnauthorizedException;
import com.shopzen.ecommerce_api.repository.CartItemRepository;
import com.shopzen.ecommerce_api.repository.CartRepository;
import com.shopzen.ecommerce_api.repository.CouponRepository;
import com.shopzen.ecommerce_api.repository.OrderRepository;
import com.shopzen.ecommerce_api.repository.ProductRepository;
import com.shopzen.ecommerce_api.repository.UserRepository;
import com.shopzen.ecommerce_api.service.email.EmailService;
import com.shopzen.ecommerce_api.service.notification.NotificationService;
import com.shopzen.ecommerce_api.util.EntityMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final CartItemRepository cartItemRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final NotificationService notificationService;
    private final EntityMapper entityMapper;

    private static final BigDecimal TAX_RATE = new BigDecimal("0.18");
    private static final BigDecimal FREE_SHIPPING_THRESHOLD = new BigDecimal("999");
    private static final BigDecimal SHIPPING_CHARGE = new BigDecimal("49");

    @Override
    public OrderDTO createOrder(String userId, OrderRequestDTO request) {  // Changed from UUID to String
        log.info("Creating order for user: {}", userId);
        
        try {
            if (request.getAddress() == null) {
                throw new IllegalArgumentException("Address is required");
            }
            
            Cart cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new IllegalStateException("Cart not found for user: " + userId));
            List<CartItem> cartItems = cartItemRepository.findByCartId(cart.getId());
            log.info("Found {} items in cart for user: {}", cartItems.size(), userId);
            
            if (cartItems.isEmpty()) {
                throw new IllegalStateException("Cart is empty");
            }

            BigDecimal subtotal = BigDecimal.ZERO;
            String adminId = null;  // Changed from UUID to String
            boolean multipleAdmins = false;
            
            for (CartItem item : cartItems) {
                Product product = productRepository.findById(item.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + item.getProductId()));
                
                if (product.getStockQuantity() < item.getQuantity()) {
                    throw new IllegalStateException("Insufficient stock for product: " + product.getName() + 
                            ". Available: " + product.getStockQuantity() + ", Requested: " + item.getQuantity());
                }
                
                String productAdminId = product.getCreatedBy();
                if (productAdminId != null) {
                    if (adminId == null) {
                        adminId = productAdminId;
                    } else if (!adminId.equals(productAdminId)) {
                        multipleAdmins = true;
                        log.warn("Cart contains products from multiple admins. Products from admin {} and {}", 
                            adminId, productAdminId);
                    }
                }
                
                subtotal = subtotal.add(product.getSellingPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
            }

            if (multipleAdmins) {
                log.warn("Order contains products from multiple admins. Using admin {} for the order", adminId);
            }

            BigDecimal discount = BigDecimal.ZERO;
            Coupon coupon = null;
            if (request.getCouponCode() != null && !request.getCouponCode().isEmpty()) {
                coupon = couponRepository.findByCodeAndIsActiveTrue(request.getCouponCode()).orElse(null);
                if (coupon != null && coupon.isValid()) {
                    if (adminId != null && coupon.getCreatedBy() != null && !coupon.getCreatedBy().equals(adminId)) {
                        log.warn("Coupon {} belongs to admin {} but products belong to admin {}", 
                            request.getCouponCode(), coupon.getCreatedBy(), adminId);
                        coupon = null;
                    } else {
                        discount = coupon.calculateDiscount(subtotal);
                        coupon.setUsedCount(coupon.getUsedCount() + 1);
                        couponRepository.save(coupon);
                        log.info("Applied coupon: {}, discount: {}", coupon.getCode(), discount);
                    }
                } else {
                    log.warn("Invalid or expired coupon: {}", request.getCouponCode());
                }
            }

            BigDecimal discountedSubtotal = subtotal.subtract(discount);
            BigDecimal shippingCharge = discountedSubtotal.compareTo(FREE_SHIPPING_THRESHOLD) >= 0 ?
                    BigDecimal.ZERO : SHIPPING_CHARGE;

            BigDecimal taxableAmount = discountedSubtotal.add(shippingCharge);
            BigDecimal tax = taxableAmount.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

            BigDecimal total = discountedSubtotal.add(shippingCharge).add(tax);

            AddressSnapshot addressSnapshot = entityMapper.toAddressSnapshot(request.getAddress());

            Order order = Order.builder()
                    .userId(userId)
                    .orderNumber(generateOrderNumber())
                    .status("pending")
                    .paymentMethod(request.getPaymentMethod())
                    .paymentStatus(request.getPaymentMethod().equals("cod") ? "pending" : "paid")
                    .subtotal(subtotal)
                    .discount(discount)
                    .shippingCharge(shippingCharge)
                    .tax(tax)
                    .total(total)
                    .couponCode(coupon != null ? coupon.getCode() : null)
                    .addressSnapshot(addressSnapshot)
                    .notes(request.getNotes())
                    .createdBy(userId)
                    .adminId(adminId)
                    .build();

            order = orderRepository.save(order);
            log.info("Order created with ID: {} by user: {}, Admin ID: {}", order.getId(), userId, adminId);

            // Create a final copy of order for use in lambda
            final Order finalOrderForItems = order;

            for (CartItem cartItem : cartItems) {
                Product product = productRepository.findById(cartItem.getProductId())
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + cartItem.getProductId()));

                OrderItem orderItem = OrderItem.builder()
                        .orderId(finalOrderForItems.getId())
                        .productId(product.getId())
                        .productSnapshot(entityMapper.toProductSnapshot(product))
                        .quantity(cartItem.getQuantity())
                        .unitPrice(product.getSellingPrice())
                        .totalPrice(product.getSellingPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                        .selectedColor(cartItem.getSelectedColor())
                        .selectedSize(cartItem.getSelectedSize())
                        .build();

                finalOrderForItems.getOrderItems().add(orderItem);

                int newStock = product.getStockQuantity() - cartItem.getQuantity();
                product.setStockQuantity(newStock);
                productRepository.save(product);
                
                log.debug("Updated stock for product {}: {}", product.getId(), newStock);
            }

            Order savedOrder = orderRepository.save(order);

            cartItemRepository.deleteByCartId(cart.getId());
            log.info("Cleared cart for user: {}", userId);

            // Send order confirmation email and notification
            try {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
                
                emailService.sendOrderConfirmationEmail(savedOrder, user);
                log.info("Order confirmation email sent to: {}", user.getEmail());
                
                notificationService.createOrderConfirmationNotification(savedOrder, user);
                log.info("Order confirmation notification created for user: {}", user.getEmail());
                
            } catch (Exception e) {
                log.error("Failed to send order confirmation email/notification: {}", e.getMessage());
            }

            return entityMapper.toOrderDTO(savedOrder);
            
        } catch (Exception e) {
            log.error("Error creating order: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create order: " + e.getMessage());
        }
    }

    @Override
    public OrderDTO getOrder(String userId, String orderId) {  // Changed from UUID to String
        log.info("Fetching order {} for user {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        return entityMapper.toOrderDTO(order);
    }

    @Override
    public OrderDTO getOrderByNumber(String orderNumber) {
        log.info("Fetching order by number: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with number: " + orderNumber));
        return entityMapper.toOrderDTO(order);
    }

    @Override
    public List<OrderDTO> getUserOrders(String userId) {  // Changed from UUID to String
        log.info("Fetching all orders for user: {}", userId);
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(entityMapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Deprecated
    public OrderDTO updateOrderStatus(String orderId, String status) {  // Changed from UUID to String
        log.warn("Deprecated updateOrderStatus called without admin user ID");
        return updateOrderStatus(null, orderId, status);
    }

    @Override
    public OrderDTO updateOrderStatus(String adminUserId, String orderId, String status) {  // Changed from UUID to String
        log.info("Admin {} updating order {} status to: {}", adminUserId, orderId, status);
        
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be null or empty");
        }
        
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (adminUserId != null && order.getAdminId() != null && !order.getAdminId().equals(adminUserId)) {
            log.warn("Admin {} tried to update order {} owned by admin {}", adminUserId, orderId, order.getAdminId());
            throw new UnauthorizedException("You are not authorized to update this order. Only the admin who owns the products can update it.");
        }
        
        String oldStatus = order.getStatus();
        
        if (order.getStatus().equalsIgnoreCase(status)) {
            log.info("Order {} is already in status: {}", orderId, status);
            return entityMapper.toOrderDTO(order);
        }
        
        validateStatusTransition(order.getStatus(), status);
        
        order.setStatus(status);
        
        if ("delivered".equalsIgnoreCase(status)) {
            order.setPaymentStatus("completed");
        }
        
        if ("cancelled".equalsIgnoreCase(status)) {
            order.setPaymentStatus("cancelled");
            restoreStock(order);
        }
        
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} status updated to: {}", orderId, status);
        
        // Send email and notification for status change
        try {
            User user = userRepository.findById(savedOrder.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + savedOrder.getUserId()));
            
            sendStatusUpdateEmail(savedOrder, user, oldStatus, status);
            log.info("Status update email sent to: {} for order: {}", user.getEmail(), savedOrder.getOrderNumber());
            
            createNotificationForStatusChange(savedOrder, user, oldStatus, status);
            log.info("Notification created for user: {} for order: {}", user.getEmail(), savedOrder.getOrderNumber());
            
        } catch (Exception e) {
            log.error("Failed to send status update email/notification: {}", e.getMessage());
        }
        
        return entityMapper.toOrderDTO(savedOrder);
    }

    @Override
    public List<OrderDTO> getOrdersByStatus(String status) {
        log.info("Fetching orders with status: {}", status);
        if ("all".equalsIgnoreCase(status)) {
            return orderRepository.findAllByOrderByCreatedAtDesc()
                    .stream()
                    .map(entityMapper::toOrderDTO)
                    .collect(Collectors.toList());
        }
        return orderRepository.findByStatus(status)
                .stream()
                .map(entityMapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<OrderDTO> getAllOrders(Pageable pageable) {
        log.info("Fetching all orders with pagination: page={}, size={}", 
            pageable.getPageNumber(), pageable.getPageSize());
        return orderRepository.findAll(pageable)
                .map(entityMapper::toOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable) {
        log.info("Fetching orders with status: {} with pagination", status);
        if ("all".equalsIgnoreCase(status)) {
            return orderRepository.findAll(pageable)
                    .map(entityMapper::toOrderDTO);
        }
        return orderRepository.findByStatus(status, pageable)
                .map(entityMapper::toOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOrdersByAdmin(String adminUserId, Pageable pageable) {  // Changed from UUID to String
        log.info("Fetching orders for admin (product owner): {}", adminUserId);
        return orderRepository.findByAdminId(adminUserId, pageable)
                .map(entityMapper::toOrderDTO);
    }

    @Override
    public Page<OrderDTO> getOrdersByStatusAndAdmin(String status, String adminUserId, Pageable pageable) {  // Changed from UUID to String
        log.info("Fetching orders with status: {} for admin: {} with pagination", status, adminUserId);
        
        Page<Order> orders;
        if ("all".equalsIgnoreCase(status)) {
            orders = orderRepository.findByAdminId(adminUserId, pageable);
        } else {
            orders = orderRepository.findByStatusAndAdminId(status, adminUserId, pageable);
        }
        return orders.map(entityMapper::toOrderDTO);
    }

    @Override
    public boolean canUpdateOrder(String userId, String orderId) {  // Changed from UUID to String
        if (userId == null || orderId == null) {
            return false;
        }
        try {
            Order order = orderRepository.findById(orderId).orElse(null);
            if (order == null) return false;
            if (order.getAdminId() == null) return false;
            return order.getAdminId().equals(userId);
        } catch (Exception e) {
            log.error("Error checking update permission: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public OrderDTO cancelOrder(String userId, String orderId) {  // Changed from UUID to String
        log.info("Cancelling order {} for user {}", orderId, userId);
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        
        if (!canCancelOrder(order)) {
            throw new IllegalStateException("Order cannot be cancelled in its current state: " + order.getStatus());
        }
        
        String oldStatus = order.getStatus();
        order.setStatus("cancelled");
        order.setPaymentStatus("refunded");
        
        restoreStock(order);
        
        Order cancelledOrder = orderRepository.save(order);
        log.info("Order {} cancelled successfully", orderId);
        
        // Send cancellation email and notification
        try {
            User user = userRepository.findById(cancelledOrder.getUserId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + cancelledOrder.getUserId()));
            
            emailService.sendOrderCancelledEmail(cancelledOrder, user);
            log.info("Order cancellation email sent to: {}", user.getEmail());
            
            notificationService.createOrderCancelledNotification(cancelledOrder, user);
            log.info("Order cancellation notification created for user: {}", user.getEmail());
            
        } catch (Exception e) {
            log.error("Failed to send cancellation email/notification: {}", e.getMessage());
        }
        
        return entityMapper.toOrderDTO(cancelledOrder);
    }

    // ==================== Helper Methods ====================

    private String generateOrderNumber() {
        return "ORD" + System.currentTimeMillis() + 
               UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private void validateStatusTransition(String currentStatus, String newStatus) {
        List<String> validTransitions = getValidTransitions(currentStatus);
        if (!validTransitions.contains(newStatus.toLowerCase())) {
            String errorMsg = String.format("Invalid status transition from %s to %s. Allowed transitions: %s", 
                    currentStatus, newStatus, validTransitions);
            log.error(errorMsg);
            throw new IllegalStateException(errorMsg);
        }
    }

    private List<String> getValidTransitions(String currentStatus) {
        if (currentStatus == null) {
            return List.of("pending", "confirmed", "processing");
        }
        
        String status = currentStatus.toLowerCase();
        switch (status) {
            case "pending":
                return List.of("confirmed", "processing", "cancelled");
            case "confirmed":
                return List.of("processing", "shipped", "cancelled");
            case "processing":
                return List.of("shipped", "cancelled");
            case "shipped":
                return List.of("delivered", "cancelled");
            case "delivered":
                return List.of();
            case "cancelled":
                return List.of();
            default:
                return List.of("pending", "confirmed", "processing", "shipped", "delivered", "cancelled");
        }
    }

    private boolean canCancelOrder(Order order) {
        if (order == null || order.getStatus() == null) {
            return false;
        }
        String status = order.getStatus().toLowerCase();
        return "pending".equals(status) || 
               "confirmed".equals(status) || 
               "processing".equals(status) || 
               "shipped".equals(status);
    }

    private void restoreStock(Order order) {
        if (order.getOrderItems() != null && !order.getOrderItems().isEmpty()) {
            for (OrderItem item : order.getOrderItems()) {
                try {
                    Product product = productRepository.findById(item.getProductId())
                            .orElse(null);
                    if (product != null) {
                        int newStock = product.getStockQuantity() + item.getQuantity();
                        product.setStockQuantity(newStock);
                        productRepository.save(product);
                        log.debug("Restored stock for product {}: {}", product.getId(), newStock);
                    }
                } catch (Exception e) {
                    log.error("Error restoring stock for product: {}", item.getProductId(), e);
                }
            }
        }
    }

    private void sendStatusUpdateEmail(Order order, User user, String oldStatus, String newStatus) {
        switch (newStatus.toLowerCase()) {
            case "confirmed":
                emailService.sendOrderStatusUpdateEmail(order, user, oldStatus, newStatus);
                break;
            case "processing":
                emailService.sendOrderStatusUpdateEmail(order, user, oldStatus, newStatus);
                break;
            case "shipped":
                emailService.sendOrderShippedEmail(order, user);
                break;
            case "delivered":
                emailService.sendOrderDeliveredEmail(order, user);
                break;
            case "cancelled":
                emailService.sendOrderCancelledEmail(order, user);
                break;
            default:
                emailService.sendOrderStatusUpdateEmail(order, user, oldStatus, newStatus);
                break;
        }
    }

    private void createNotificationForStatusChange(Order order, User user, String oldStatus, String newStatus) {
        switch (newStatus.toLowerCase()) {
            case "confirmed":
                notificationService.createOrderConfirmationNotification(order, user);
                break;
            case "processing":
                notificationService.createOrderStatusNotification(order, user, oldStatus, newStatus);
                break;
            case "shipped":
                notificationService.createOrderShippedNotification(order, user);
                break;
            case "delivered":
                notificationService.createOrderDeliveredNotification(order, user);
                break;
            case "cancelled":
                notificationService.createOrderCancelledNotification(order, user);
                break;
            default:
                notificationService.createOrderStatusNotification(order, user, oldStatus, newStatus);
                break;
        }
    }
}
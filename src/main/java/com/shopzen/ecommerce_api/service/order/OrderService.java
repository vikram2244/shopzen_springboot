package com.shopzen.ecommerce_api.service.order;

import com.shopzen.ecommerce_api.dto.order.OrderDTO;
import com.shopzen.ecommerce_api.dto.order.OrderRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO createOrder(String userId, OrderRequestDTO request);  // Changed from UUID to String
    OrderDTO getOrder(String userId, String orderId);  // Changed from UUID to String
    OrderDTO getOrderByNumber(String orderNumber);
    List<OrderDTO> getUserOrders(String userId);  // Changed from UUID to String
    OrderDTO updateOrderStatus(String orderId, String status);  // Changed from UUID to String
    List<OrderDTO> getOrdersByStatus(String status);
    OrderDTO cancelOrder(String userId, String orderId);  // Changed from UUID to String
    Page<OrderDTO> getAllOrders(Pageable pageable);
    Page<OrderDTO> getOrdersByStatus(String status, Pageable pageable);
    OrderDTO updateOrderStatus(String adminUserId, String orderId, String status);  // Changed from UUID to String
    Page<OrderDTO> getOrdersByAdmin(String adminUserId, Pageable pageable);  // Changed from UUID to String
    boolean canUpdateOrder(String userId, String orderId);  // Changed from UUID to String
    Page<OrderDTO> getOrdersByStatusAndAdmin(String status, String adminUserId, Pageable pageable);  // Changed from UUID to String
}
package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {  // Changed from UUID to String
    
    Optional<Order> findByOrderNumber(String orderNumber);
    
    Optional<Order> findByIdAndUserId(String id, String userId);  // Changed from UUID to String
    
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);  // Changed from UUID to String
    
    List<Order> findByStatus(String status);
    
    List<Order> findAllByOrderByCreatedAtDesc();
    
    Page<Order> findByStatus(String status, Pageable pageable);
    
    Page<Order> findByCreatedBy(String createdBy, Pageable pageable);  // Changed from UUID to String
    
    List<Order> findByCreatedBy(String createdBy);  // Changed from UUID to String
    
    Page<Order> findByAdminId(String adminId, Pageable pageable);  // Changed from UUID to String
    
    @Query("SELECT o FROM Order o WHERE o.adminId = :adminId")
    Page<Order> findOrdersByAdminId(@Param("adminId") String adminId, Pageable pageable);  // Changed from UUID to String
    
    @Query("SELECT o FROM Order o WHERE o.status = :status AND o.adminId = :adminId")
    Page<Order> findByStatusAndAdminId(@Param("status") String status, @Param("adminId") String adminId, Pageable pageable);  // Changed from UUID to String
    
    boolean existsByIdAndCreatedBy(String id, String createdBy);  // Changed from UUID to String
    
    boolean existsByIdAndAdminId(String id, String adminId);  // Changed from UUID to String
}
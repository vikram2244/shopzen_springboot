package com.shopzen.ecommerce_api.repository;

import com.shopzen.ecommerce_api.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CartItemRepository extends JpaRepository<CartItem, String> {
    
    List<CartItem> findByCartId(String cartId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id IN (SELECT c.id FROM Cart c WHERE c.userId = :userId)")
    List<CartItem> findByUserId(@Param("userId") String userId);
    
    Optional<CartItem> findByCartIdAndProductId(String cartId, String productId);
    
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId AND ci.selectedColor = :color AND ci.selectedSize = :size")
    Optional<CartItem> findByUserIdAndProductIdAndSelectedColorAndSelectedSize(
            @Param("userId") String userId,
            @Param("productId") String productId,
            @Param("color") String color,
            @Param("size") String size);
    
    @Query("SELECT CASE WHEN COUNT(ci) > 0 THEN true ELSE false END FROM CartItem ci WHERE ci.cart.userId = :userId AND ci.productId = :productId")
    boolean existsByUserIdAndProductId(@Param("userId") String userId, @Param("productId") String productId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
    void deleteByCartId(@Param("cartId") String cartId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem ci WHERE ci.cart.userId = :userId")
    void deleteByUserId(@Param("userId") String userId);
    
    @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.userId = :userId")
    long countByUserId(@Param("userId") String userId);
}
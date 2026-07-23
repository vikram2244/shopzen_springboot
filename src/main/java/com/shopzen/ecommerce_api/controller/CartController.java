// com.shopzen.ecommerce_api.controller.CartController.java
package com.shopzen.ecommerce_api.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.shopzen.ecommerce_api.dto.cart.AddToCartRequest;
import com.shopzen.ecommerce_api.dto.product.CartItemDTO;
import com.shopzen.ecommerce_api.dto.product.CartSummaryDTO;
import com.shopzen.ecommerce_api.exception.ResourceNotFoundException;
import com.shopzen.ecommerce_api.service.cart.CartService;
import com.shopzen.ecommerce_api.util.SecurityUtils;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Tag(name = "Cart API", description = "Shopping cart operations")
@Slf4j
public class CartController {

    private final CartService cartService;

    @GetMapping
    @Operation(summary = "Get current user's cart items")
    public ResponseEntity<List<CartItemDTO>> getCart() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.debug("Getting cart for user: {}", userId);
        return ResponseEntity.ok(cartService.getCartItems(userId));
    }

    @PostMapping("/add")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<CartItemDTO> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Adding to cart - userId: {}, productId: {}", userId, request.getProductId());
        return ResponseEntity.ok(cartService.addToCart(
                userId,
                request.getProductId(),  // Already a String, no need to convert
                request.getQuantity(),
                request.getSelectedColor(),
                request.getSelectedSize()
        ));
    }

    @PutMapping("/items/{itemId}")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<?> updateQuantity(
            @PathVariable String itemId,  // Changed from UUID to String
            @RequestParam Integer quantity) {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("UPDATE QUANTITY - userId: {}, itemId: {}, quantity: {}", userId, itemId, quantity);
        
        try {
            if (quantity == null || quantity < 0) {
                log.warn("Invalid quantity: {}", quantity);
                return ResponseEntity.badRequest().body("Quantity must be a positive number");
            }
            
            CartItemDTO updatedItem = cartService.updateQuantity(userId, itemId, quantity);
            if (updatedItem == null) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(updatedItem);
        } catch (ResourceNotFoundException e) {
            log.error("Cart item not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid argument: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            log.error("Error updating cart quantity: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Failed to update quantity: " + e.getMessage());
        }
    }

    @DeleteMapping("/items/{itemId}")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<Void> removeFromCart(@PathVariable String itemId) {  // Changed from UUID to String
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Removing from cart - userId: {}, itemId: {}", userId, itemId);
        cartService.removeFromCart(userId, itemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    @Operation(summary = "Clear cart")
    public ResponseEntity<Void> clearCart() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.info("Clearing cart for user: {}", userId);
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/summary")
    @Operation(summary = "Get cart summary")
    public ResponseEntity<CartSummaryDTO> getCartSummary() {
        String userId = SecurityUtils.getCurrentUserId();  // Changed from UUID to String
        log.debug("Getting cart summary for user: {}", userId);
        return ResponseEntity.ok(cartService.getCartSummary(userId));
    }
}
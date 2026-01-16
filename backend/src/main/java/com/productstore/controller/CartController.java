package com.productstore.backend.controller;

import com.productstore.backend.model.Cart;
import com.productstore.backend.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
@CrossOrigin(origins = "http://localhost:3000")
public class CartController {

    @Autowired
    private CartService cartService;

    // GET cart by user ID
    // URL: http://localhost:8081/api/cart/user123
    @GetMapping("/{userId}")
    public ResponseEntity<Cart> getCart(@PathVariable String userId) {
        return cartService.getCartByUserId(userId)
                .map(cart -> ResponseEntity.ok().body(cart))
                .orElse(ResponseEntity.ok(cartService.getOrCreateCart(userId)));
    }

    // POST - Add item to cart
    // URL: http://localhost:8081/api/cart/user123/add
    // Body: { "productId": 1, "quantity": 2 }
    @PostMapping("/{userId}/add")
    public ResponseEntity<Cart> addItemToCart(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Cart cart = cartService.addItemToCart(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT - Update cart item quantity
    // URL: http://localhost:8081/api/cart/user123/update
    // Body: { "productId": 1, "quantity": 3 }
    @PutMapping("/{userId}/update")
    public ResponseEntity<Cart> updateCartItem(
            @PathVariable String userId,
            @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());

            Cart cart = cartService.updateCartItemQuantity(userId, productId, quantity);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // DELETE - Remove item from cart
    // URL: http://localhost:8081/api/cart/user123/remove/1
    @DeleteMapping("/{userId}/remove/{productId}")
    public ResponseEntity<Cart> removeItemFromCart(
            @PathVariable String userId,
            @PathVariable Long productId) {
        try {
            Cart cart = cartService.removeItemFromCart(userId, productId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Clear cart
    // URL: http://localhost:8081/api/cart/user123/clear
    @DeleteMapping("/{userId}/clear")
    public ResponseEntity<Cart> clearCart(@PathVariable String userId) {
        try {
            Cart cart = cartService.clearCart(userId);
            return ResponseEntity.ok(cart);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Delete cart
    // URL: http://localhost:8081/api/cart/user123
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteCart(@PathVariable String userId) {
        try {
            cartService.deleteCart(userId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET - Cart summary (total price, total items)
    // URL: http://localhost:8081/api/cart/user123/summary
    @GetMapping("/{userId}/summary")
    public ResponseEntity<Map<String, Object>> getCartSummary(@PathVariable String userId) {
        return cartService.getCartByUserId(userId)
                .map(cart -> {
                    Map<String, Object> summary = new HashMap<>();
                    summary.put("totalPrice", cart.getTotalPrice());
                    summary.put("totalItems", cart.getTotalItems());
                    summary.put("itemCount", cart.getCartItems().size());
                    return ResponseEntity.ok(summary);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}

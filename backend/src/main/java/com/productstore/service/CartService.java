package com.productstore.backend.service;

import com.productstore.backend.model.Cart;
import com.productstore.backend.model.CartItem;
import com.productstore.backend.model.Product;
import com.productstore.backend.repository.CartItemRepository;
import com.productstore.backend.repository.CartRepository;
import com.productstore.backend.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class CartService {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get or create cart for user
    public Cart getOrCreateCart(String userId) {
        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart(userId);
                    return cartRepository.save(cart);
                });
    }

    // Get cart by user ID
    public Optional<Cart> getCartByUserId(String userId) {
        return cartRepository.findByUserId(userId);
    }

    // Get cart by ID
    public Optional<Cart> getCartById(Long id) {
        return cartRepository.findById(id);
    }

    // Add item to cart
    @Transactional
    public Cart addItemToCart(String userId, Long productId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Check if product already exists in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId);

        if (existingItem.isPresent()) {
            // Update quantity if item already exists
            CartItem item = existingItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            cartItemRepository.save(item);
        } else {
            // Create new cart item
            CartItem newItem = new CartItem(product, quantity);
            cart.addItem(newItem);
            cartItemRepository.save(newItem);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Update cart item quantity
    @Transactional
    public Cart updateCartItemQuantity(String userId, Long productId, Integer quantity) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        if (quantity <= 0) {
            cartItemRepository.delete(item);
            cart.removeItem(item);
        } else {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Remove item from cart
    @Transactional
    public Cart removeItemFromCart(String userId, Long productId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        CartItem item = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cart.removeItem(item);
        cartItemRepository.delete(item);

        cart.setUpdatedAt(LocalDateTime.now());
        return cartRepository.save(cart);
    }

    // Clear all items from cart
    @Transactional
    public Cart clearCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));

        cartItemRepository.deleteByCartId(cart.getId());
        cart.getCartItems().clear();
        cart.setUpdatedAt(LocalDateTime.now());

        return cartRepository.save(cart);
    }

    // Delete cart
    @Transactional
    public void deleteCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Cart not found for user: " + userId));
        cartRepository.delete(cart);
    }
}

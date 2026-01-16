package com.productstore.backend.repository;

import com.productstore.backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find cart items by cart ID
    List<CartItem> findByCartId(Long cartId);

    // Find a specific cart item by cart ID and product ID
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    // Delete all cart items for a cart
    void deleteByCartId(Long cartId);
}

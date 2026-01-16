package com.productstore.backend.repository;

import com.productstore.backend.model.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    // Find cart by user ID
    Optional<Cart> findByUserId(String userId);

    // Check if cart exists for user
    boolean existsByUserId(String userId);

    // Delete cart by user ID
    void deleteByUserId(String userId);
}

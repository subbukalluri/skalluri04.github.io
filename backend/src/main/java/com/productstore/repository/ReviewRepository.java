package com.productstore.backend.repository;

import com.productstore.backend.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Find all reviews for a specific product
    List<Review> findByProductId(Long productId);

    // Find reviews by rating for a specific product
    List<Review> findByProductIdAndRating(Long productId, Integer rating);

    // Find reviews by reviewer name
    List<Review> findByReviewerNameContainingIgnoreCase(String reviewerName);

    // Get average rating for a product
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = :productId")
    Double getAverageRatingForProduct(@Param("productId") Long productId);

    // Count reviews for a product
    Long countByProductId(Long productId);

    // Find reviews with rating greater than or equal to
    List<Review> findByProductIdAndRatingGreaterThanEqual(Long productId, Integer minRating);
}

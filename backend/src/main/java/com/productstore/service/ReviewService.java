package com.productstore.backend.service;

import com.productstore.backend.model.Product;
import com.productstore.backend.model.Review;
import com.productstore.backend.repository.ProductRepository;
import com.productstore.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ProductRepository productRepository;

    // Get all reviews
    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    // Get review by ID
    public Optional<Review> getReviewById(Long id) {
        return reviewRepository.findById(id);
    }

    // Get all reviews for a product
    public List<Review> getReviewsByProductId(Long productId) {
        return reviewRepository.findByProductId(productId);
    }

    // Get reviews by rating for a product
    public List<Review> getReviewsByProductIdAndRating(Long productId, Integer rating) {
        return reviewRepository.findByProductIdAndRating(productId, rating);
    }

    // Get average rating for a product
    public Double getAverageRatingForProduct(Long productId) {
        Double average = reviewRepository.getAverageRatingForProduct(productId);
        return average != null ? Math.round(average * 10.0) / 10.0 : 0.0;
    }

    // Count reviews for a product
    public Long countReviewsForProduct(Long productId) {
        return reviewRepository.countByProductId(productId);
    }

    // Add a new review
    public Review addReview(Long productId, Review review) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        review.setProduct(product);
        return reviewRepository.save(review);
    }

    // Update review
    public Review updateReview(Long id, Review reviewDetails) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));

        review.setReviewerName(reviewDetails.getReviewerName());
        review.setRating(reviewDetails.getRating());
        review.setComment(reviewDetails.getComment());

        return reviewRepository.save(review);
    }

    // Delete review
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review not found with id: " + id));
        reviewRepository.delete(review);
    }

    // Find reviews with minimum rating
    public List<Review> getReviewsByMinRating(Long productId, Integer minRating) {
        return reviewRepository.findByProductIdAndRatingGreaterThanEqual(productId, minRating);
    }
}

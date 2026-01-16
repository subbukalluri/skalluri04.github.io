package com.productstore.backend.controller;

import com.productstore.backend.dto.ReviewRequest;
import com.productstore.backend.model.Review;
import com.productstore.backend.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "http://localhost:3000")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // GET all reviews
    // URL: http://localhost:8081/api/reviews
    @GetMapping
    public List<Review> getAllReviews() {
        return reviewService.getAllReviews();
    }

    // GET review by ID
    // URL: http://localhost:8081/api/reviews/1
    @GetMapping("/{id}")
    public ResponseEntity<Review> getReviewById(@PathVariable Long id) {
        return reviewService.getReviewById(id)
                .map(review -> ResponseEntity.ok().body(review))
                .orElse(ResponseEntity.notFound().build());
    }

    // GET all reviews for a specific product
    // URL: http://localhost:8081/api/reviews/product/1
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<Review>> getReviewsByProduct(@PathVariable Long productId) {
        List<Review> reviews = reviewService.getReviewsByProductId(productId);
        return ResponseEntity.ok(reviews);
    }

    // GET reviews by product and rating
    // URL: http://localhost:8081/api/reviews/product/1/rating/5
    @GetMapping("/product/{productId}/rating/{rating}")
    public ResponseEntity<List<Review>> getReviewsByProductAndRating(
            @PathVariable Long productId,
            @PathVariable Integer rating) {
        List<Review> reviews = reviewService.getReviewsByProductIdAndRating(productId, rating);
        return ResponseEntity.ok(reviews);
    }

    // GET reviews with minimum rating
    // URL: http://localhost:8081/api/reviews/product/1/min-rating?minRating=4
    @GetMapping("/product/{productId}/min-rating")
    public ResponseEntity<List<Review>> getReviewsByMinRating(
            @PathVariable Long productId,
            @RequestParam Integer minRating) {
        List<Review> reviews = reviewService.getReviewsByMinRating(productId, minRating);
        return ResponseEntity.ok(reviews);
    }

    // GET average rating and review count for a product
    // URL: http://localhost:8081/api/reviews/product/1/stats
    @GetMapping("/product/{productId}/stats")
    public ResponseEntity<Map<String, Object>> getProductReviewStats(@PathVariable Long productId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("averageRating", reviewService.getAverageRatingForProduct(productId));
        stats.put("reviewCount", reviewService.countReviewsForProduct(productId));
        return ResponseEntity.ok(stats);
    }

    // POST - Add new review for a product
    // URL: http://localhost:8081/api/reviews/product/1
    @PostMapping("/product/{productId}")
    public ResponseEntity<?> addReview(@PathVariable Long productId, @RequestBody ReviewRequest reviewRequest) {
        try {
            Review review = new Review();
            review.setReviewerName(reviewRequest.getReviewerName());
            review.setRating(reviewRequest.getRating());
            review.setComment(reviewRequest.getComment());

            Review savedReview = reviewService.addReview(productId, review);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedReview);
        } catch (IllegalArgumentException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // PUT - Update review
    // URL: http://localhost:8081/api/reviews/1
    @PutMapping("/{id}")
    public ResponseEntity<Review> updateReview(@PathVariable Long id, @RequestBody ReviewRequest reviewRequest) {
        try {
            Review reviewDetails = new Review();
            reviewDetails.setReviewerName(reviewRequest.getReviewerName());
            reviewDetails.setRating(reviewRequest.getRating());
            reviewDetails.setComment(reviewRequest.getComment());

            Review updatedReview = reviewService.updateReview(id, reviewDetails);
            return ResponseEntity.ok(updatedReview);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE - Delete review
    // URL: http://localhost:8081/api/reviews/1
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewService.deleteReview(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}

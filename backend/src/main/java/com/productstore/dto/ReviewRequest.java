package com.productstore.backend.dto;

public class ReviewRequest {
    private String reviewerName;
    private Integer rating;
    private String comment;

    // Constructors
    public ReviewRequest() {
    }

    public ReviewRequest(String reviewerName, Integer rating, String comment) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and Setters
    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}

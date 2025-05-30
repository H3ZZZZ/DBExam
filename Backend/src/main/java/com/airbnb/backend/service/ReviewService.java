package com.airbnb.backend.service;

import com.airbnb.backend.repository.ReviewRepository;
import com.airbnb.backend.service.PropertyRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private PropertyRatingService propertyRatingService;
    
    /**
     * Get reviews for a specific property with pagination and sorting
     */
    public Map<String, Object> getPropertyReviews(Integer propertyId, Integer limit, Integer skip, String sortBy, String sortOrder) {
        return reviewRepository.getPropertyReviews(propertyId, limit, skip, sortBy, sortOrder);
    }
    
    /**
     * Add a new review
     */
    public Map<String, Object> addReview(Integer propertyId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        return reviewRepository.addReview(propertyId, cleanlinessRating, satisfactionRating, comment);
    }
    
    /**
     * Add a review and automatically trigger property rating update
     */
    public Map<String, Object> addReviewWithRatingUpdate(Integer propertyId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        // First add the review
        Map<String, Object> reviewResult = reviewRepository.addReview(propertyId, cleanlinessRating, satisfactionRating, comment);
        
        if (reviewResult != null && Boolean.TRUE.equals(reviewResult.get("success"))) {
            // Then trigger property rating recalculation
            Map<String, Object> ratingResult = propertyRatingService.updatePropertyRatingAfterNewReview(propertyId);
            
            // Combine results
            reviewResult.put("rating_update", ratingResult);
            reviewResult.put("trigger_simulation", "Automatic property rating recalculation after review insert");
            reviewResult.put("cross_service_operation", true);
        }
        
        return reviewResult;
    }
    
    /**
     * Get all reviews with advanced filtering options
     */
    public Map<String, Object> getAllReviews(Integer limit, Integer skip, String sortBy, String sortOrder, Integer minRating, Integer maxRating) {
        return reviewRepository.getAllReviews(limit, skip, sortBy, sortOrder, minRating, maxRating);
    }
    
    /**
     * Get review analytics for a property
     */
    public Map<String, Object> getReviewAnalytics(Integer propertyId) {
        return reviewRepository.getReviewAnalytics(propertyId);
    }
    
    /**
     * Get overall review summary statistics
     */
    public Map<String, Object> getReviewSummary() {
        return reviewRepository.getReviewSummary();
    }
    
    /**
     * Validate review data
     */
    public boolean isValidReview(Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        if (cleanlinessRating == null || cleanlinessRating < 1 || cleanlinessRating > 5) {
            return false;
        }
        if (satisfactionRating == null || satisfactionRating < 1 || satisfactionRating > 5) {
            return false;
        }
        if (comment == null || comment.trim().isEmpty() || comment.length() > 1000) {
            return false;
        }
        return true;
    }
} 
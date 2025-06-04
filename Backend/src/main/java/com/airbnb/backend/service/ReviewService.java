package com.airbnb.backend.service;

import com.airbnb.backend.repository.ReviewRepository;
import com.airbnb.backend.repository.BookingRepository;
import com.airbnb.backend.service.PropertyRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.LinkedHashMap;

@Service
public class ReviewService {
    
    @Autowired
    private ReviewRepository reviewRepository;
    
    @Autowired
    private PropertyRatingService propertyRatingService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    /**
     * Get reviews for a specific property with pagination and sorting
     */
    public Map<String, Object> getPropertyReviews(Integer propertyId, Integer limit, Integer skip, String sortBy, String sortOrder) {
        return reviewRepository.getPropertyReviews(propertyId, limit, skip, sortBy, sortOrder);
    }
    
    /**
     * Add a new review with comprehensive validation using MySQL stored procedures
     */
    public Map<String, Object> addReview(Integer propertyId, Integer bookingId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        try {
            // Step 1: Validate input parameters
            if (!isValidReview(cleanlinessRating, satisfactionRating, comment)) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("booking_id", bookingId);
                response.put("error", "Invalid review parameters - ratings must be 0-100, comment must be 1-1000 characters");
                return response;
            }
            
            // Step 2: Check booking exists and matches property
            boolean bookingValid = validateBookingExists(bookingId, propertyId);
            if (!bookingValid) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("booking_id", bookingId);
                response.put("error", "Invalid booking - booking_id " + bookingId + " not found for property_id " + propertyId);
                return response;
            }
            
            // Step 3: Enforce business rule - booking must be completed
            boolean bookingCompleted = isBookingCompleted(bookingId);
            if (!bookingCompleted) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("booking_id", bookingId);
                response.put("booking_status", "active or upcoming");
                response.put("error", "Review cannot be created - booking is not yet completed");
                return response;
            }
            
            // Step 4: Create review in MongoDB
            return reviewRepository.addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("error", "Cross-database validation error: " + e.getMessage());
            return response;
        }
    }
    
    /**
     * Add a review with validation and automatically trigger property rating update
     */
    public Map<String, Object> addReviewWithRatingUpdate(Integer propertyId, Integer bookingId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        Map<String, Object> reviewResult = addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
        
        if (reviewResult != null && Boolean.TRUE.equals(reviewResult.get("success"))) {
            Map<String, Object> ratingResult = propertyRatingService.updatePropertyRatingAfterNewReview(propertyId);
            
            // Add rating update info to the review result
            reviewResult.put("rating_updated", Boolean.TRUE.equals(ratingResult.get("success")));
            if (Boolean.TRUE.equals(ratingResult.get("success"))) {
                reviewResult.put("new_avg_cleanliness", ratingResult.get("avg_cleanliness_rating"));
                reviewResult.put("new_avg_satisfaction", ratingResult.get("avg_satisfaction_rating"));
                reviewResult.put("total_reviews", ratingResult.get("total_reviews"));
            }
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
     * Enhanced review validation
     */
    public boolean isValidReview(Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        if (cleanlinessRating == null || cleanlinessRating < 0 || cleanlinessRating > 100) {
            return false;
        }
        if (satisfactionRating == null || satisfactionRating < 0 || satisfactionRating > 100) {
            return false;
        }
        if (comment == null || comment.trim().isEmpty() || comment.length() > 1000) {
            return false;
        }
        return true;
    }
    
    /**
     * Get reviews with enriched guest information from MySQL using stored procedures
     */
    public Map<String, Object> getReviewsWithGuestInfo(Integer propertyId) {
        Map<String, Object> reviewsResult = reviewRepository.getPropertyReviews(propertyId, 10, 0, "created_at", "desc");
        
        if (!(Boolean) reviewsResult.get("success")) {
            return reviewsResult;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) reviewsResult.get("reviews");
        
        int enrichedCount = 0;
        
        // Enrich reviews with guest information from MySQL via booking_id using stored procedures
        for (Map<String, Object> review : reviews) {
            Integer bookingId = (Integer) review.get("booking_id");
            if (bookingId != null) {
                try {
                    Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
                    
                    if (bookingInfo != null) {
                        review.put("guest_name", bookingInfo.get("guest_name"));
                        review.put("guest_email", bookingInfo.get("guest_email"));
                        review.put("booking_price", bookingInfo.get("booking_price"));
                        enrichedCount++;
                    } else {
                        review.put("guest_name", "Booking not found");
                    }
                } catch (Exception e) {
                    review.put("guest_name", "MySQL error");
                }
            } else {
                review.put("guest_name", "No booking_id");
            }
        }
        
        // Clean response structure
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("property_id", propertyId);
        response.put("reviews_with_guest_info", reviews);
        response.put("total_reviews", reviewsResult.get("total_count"));
        response.put("enriched_reviews", enrichedCount);
        response.put("cross_database_note", "MongoDB reviews + MySQL guest data");
        
        return response;
    }
    
    /**
     * Validate booking exists using MySQL stored procedure
     */
    public boolean validateBookingExists(Integer bookingId, Integer propertyId) {
        try {
            return bookingRepository.validateBookingExists(bookingId, propertyId);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if booking is completed using MySQL stored procedure
     */
    public boolean isBookingCompleted(Integer bookingId) {
        try {
            return bookingRepository.isBookingCompleted(bookingId);
        } catch (Exception e) {
            return false;
        }
    }
} 
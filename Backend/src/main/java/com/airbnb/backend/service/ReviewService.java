package com.airbnb.backend.service;

import com.airbnb.backend.repository.ReviewRepository;
import com.airbnb.backend.repository.BookingRepository;
import com.airbnb.backend.service.PropertyRatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.util.Arrays;

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
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Validate input parameters
            if (!isValidReview(cleanlinessRating, satisfactionRating, comment)) {
                result.put("success", false);
                result.put("error", "Invalid review parameters - ratings must be 1-5, comment must be 1-1000 characters");
                result.put("validation_stage", "input_validation");
                return result;
            }
            
            // Step 2: ENHANCED VALIDATION - Check booking exists and matches property using MySQL stored procedure
            boolean bookingValid = validateBookingExists(bookingId, propertyId);
            if (!bookingValid) {
                result.put("success", false);
                result.put("error", "Invalid booking - booking_id " + bookingId + " not found for property_id " + propertyId);
                result.put("validation_stage", "mysql_stored_procedure_validation");
                result.put("stored_procedure_used", "ValidateBookingExists");
                return result;
            }
            
            // Step 3: Check if booking is completed (optional but recommended business logic)
            boolean bookingCompleted = isBookingCompleted(bookingId);
            if (!bookingCompleted) {
                result.put("warning", "Review created for active/upcoming booking - typically reviews are only allowed after completion");
                result.put("booking_status_check", "MySQL stored procedure: IsBookingCompleted");
            }
            
            // Step 4: Create review in MongoDB
            Map<String, Object> mongoResult = reviewRepository.addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
            
            // Step 5: Enhance result with validation details
            mongoResult.put("validation_details", createValidationMetadata(bookingId, propertyId, bookingCompleted));
            
            return mongoResult;
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Cross-database validation error: " + e.getMessage());
            result.put("validation_stage", "stored_procedure_error");
            return result;
        }
    }
    
    /**
     * Add a review with validation and automatically trigger property rating update
     */
    public Map<String, Object> addReviewWithRatingUpdate(Integer propertyId, Integer bookingId, Integer cleanlinessRating, Integer satisfactionRating, String comment) {
        // First add the review with enhanced validation
        Map<String, Object> reviewResult = addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
        
        if (reviewResult != null && Boolean.TRUE.equals(reviewResult.get("success"))) {
            // Then trigger property rating recalculation
            Map<String, Object> ratingResult = propertyRatingService.updatePropertyRatingAfterNewReview(propertyId);
            
            // Combine results
            reviewResult.put("rating_update", ratingResult);
            reviewResult.put("trigger_simulation", "Automatic property rating recalculation after review insert");
            reviewResult.put("cross_service_operation", true);
            reviewResult.put("dual_database_workflow", "MySQL validation -> MongoDB review -> MongoDB rating update");
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
    
    /**
     * Get reviews with enriched guest information from MySQL using stored procedures (demonstrates cross-database data transformation)
     */
    public Map<String, Object> getReviewsWithGuestInfo(Integer propertyId) {
        Map<String, Object> reviewsResult = reviewRepository.getPropertyReviews(propertyId, 10, 0, "created_at", "desc");
        
        if (!(Boolean) reviewsResult.get("success")) {
            return reviewsResult;
        }
        
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> reviews = (List<Map<String, Object>>) reviewsResult.get("reviews");
        
        // Enhanced enrichment with better error handling
        int transformationSuccessCount = 0;
        int transformationErrorCount = 0;
        
        // Enrich reviews with guest information from MySQL via booking_id using stored procedures
        for (Map<String, Object> review : reviews) {
            Integer bookingId = (Integer) review.get("booking_id");
            if (bookingId != null) {
                try {
                    // REAL CROSS-DATABASE TRANSFORMATION: MongoDB booking_id -> MySQL stored procedure call
                    Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
                    
                    if (bookingInfo != null) {
                        review.put("guest_name", bookingInfo.get("guest_name"));
                        review.put("guest_email", bookingInfo.get("guest_email"));
                        review.put("booking_status", bookingInfo.get("booking_status"));
                        review.put("booking_price", bookingInfo.get("booking_price"));
                        review.put("booking_start", bookingInfo.get("booking_start"));
                        review.put("booking_end", bookingInfo.get("booking_end"));
                        review.put("data_source", "MySQL stored procedure: GetGuestInfoFromBooking");
                        review.put("transformation_success", true);
                        review.put("stored_procedure_used", true);
                        transformationSuccessCount++;
                    } else {
                        review.put("guest_name", "Booking not found");
                        review.put("data_source", "MySQL stored procedure returned null");
                        review.put("transformation_success", false);
                        review.put("stored_procedure_used", true);
                        transformationErrorCount++;
                    }
                } catch (Exception e) {
                    review.put("guest_name", "MySQL Stored Procedure Error");
                    review.put("data_source", "Stored procedure transformation failed: " + e.getMessage());
                    review.put("transformation_success", false);
                    review.put("stored_procedure_used", true);
                    transformationErrorCount++;
                }
            } else {
                review.put("guest_name", "No booking_id");
                review.put("transformation_success", false);
                review.put("stored_procedure_used", false);
                transformationErrorCount++;
            }
        }
        
        // Enhanced metadata about the REAL transformation process using stored procedures
        Map<String, Object> transformationInfo = new HashMap<>();
        transformationInfo.put("description", "REAL Cross-database data transformation using MySQL stored procedures");
        transformationInfo.put("process", "MongoDB reviews -> booking_id -> MySQL stored procedure GetGuestInfoFromBooking -> guest enrichment");
        transformationInfo.put("databases_involved", Arrays.asList("MongoDB (reviews collection)", "MySQL (stored procedures: GetGuestInfoFromBooking)"));
        transformationInfo.put("transformation_type", "Real-time SimpleJdbcCall MySQL stored procedure integration");
        transformationInfo.put("stored_procedure", "CALL GetGuestInfoFromBooking(?)");
        transformationInfo.put("architecture", "Dual stored procedure approach: MongoDB aggregation pipelines + MySQL stored procedures");
        transformationInfo.put("success_count", transformationSuccessCount);
        transformationInfo.put("error_count", transformationErrorCount);
        transformationInfo.put("total_reviews", reviews.size());
        
        reviewsResult.put("transformation_info", transformationInfo);
        return reviewsResult;
    }
    
    /**
     * Validate booking exists in MySQL using stored procedure (real database query)
     */
    public boolean validateBookingExists(Integer bookingId, Integer propertyId) {
        try {
            return bookingRepository.validateBookingExists(bookingId, propertyId);
        } catch (Exception e) {
            // If there's an error, return false but log it
            System.err.println("MySQL stored procedure validation error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if booking is completed using stored procedure (real database query)
     */
    public boolean isBookingCompleted(Integer bookingId) {
        try {
            return bookingRepository.isBookingCompleted(bookingId);
        } catch (Exception e) {
            System.err.println("MySQL stored procedure booking completion check error: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Create validation metadata for documentation purposes
     */
    private Map<String, Object> createValidationMetadata(Integer bookingId, Integer propertyId, boolean bookingCompleted) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("booking_validation", "MySQL stored procedure: ValidateBookingExists(" + bookingId + ", " + propertyId + ")");
        metadata.put("completion_check", "MySQL stored procedure: IsBookingCompleted(" + bookingId + ")");
        metadata.put("booking_completed", bookingCompleted);
        metadata.put("validation_approach", "Cross-database stored procedure validation before MongoDB insert");
        metadata.put("data_integrity", "Ensures MongoDB reviews reference valid MySQL bookings");
        metadata.put("architecture_benefit", "Demonstrates ACID validation with eventual consistency analytics");
        return metadata;
    }
} 
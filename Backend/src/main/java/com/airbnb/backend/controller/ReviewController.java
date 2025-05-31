package com.airbnb.backend.controller;

import com.airbnb.backend.service.ReviewService;
import com.airbnb.backend.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "API for managing property reviews using MongoDB aggregation pipelines with MySQL stored procedure integration")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get reviews for a property", 
               description = "Retrieve reviews for a specific property with pagination and sorting options using MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> getPropertyReviews(
            @PathVariable Integer propertyId,
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        Map<String, Object> result = reviewService.getPropertyReviews(propertyId, limit, skip, sortBy, sortOrder);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/add")
    @Operation(summary = "Add a new review with stored procedure validation", 
               description = "Add a review with comprehensive validation using MySQL stored procedures (ValidateBookingExists, IsBookingCompleted). BUSINESS RULE: Reviews can only be created for completed bookings.")
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestParam Integer propertyId,
            @RequestParam Integer bookingId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        Map<String, Object> result = reviewService.addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/add-with-rating-update")
    @Operation(summary = "Add review and update property rating with stored procedure validation", 
               description = "Add a review with MySQL stored procedure validation and automatically trigger MongoDB aggregation pipeline rating recalculation. BUSINESS RULE: Reviews can only be created for completed bookings.")
    public ResponseEntity<Map<String, Object>> addReviewWithRatingUpdate(
            @RequestParam Integer propertyId,
            @RequestParam Integer bookingId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        Map<String, Object> result = reviewService.addReviewWithRatingUpdate(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all reviews with pagination", 
               description = "Retrieve all reviews with advanced filtering and sorting options using MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> getAllReviews(
            @RequestParam(defaultValue = "20") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip,
            @RequestParam(defaultValue = "created_at") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(required = false) Integer maxRating) {
        Map<String, Object> result = reviewService.getAllReviews(limit, skip, sortBy, sortOrder, minRating, maxRating);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/analytics/{propertyId}")
    @Operation(summary = "Get review analytics for a property", 
               description = "Generate advanced analytics and trends for property reviews using MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> getReviewAnalytics(@PathVariable Integer propertyId) {
        Map<String, Object> result = reviewService.getReviewAnalytics(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "Get review summary statistics", 
               description = "Generate system-wide review summary statistics using MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> getReviewSummary() {
        Map<String, Object> result = reviewService.getReviewSummary();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/property/{propertyId}/with-guest-info")
    @Operation(summary = "Get reviews with cross-database guest information", 
               description = "Demonstrates REAL cross-database data transformation: MongoDB reviews enriched with MySQL guest data via booking_id using stored procedure GetGuestInfoFromBooking")
    public ResponseEntity<Map<String, Object>> getReviewsWithGuestInfo(@PathVariable Integer propertyId) {
        Map<String, Object> result = reviewService.getReviewsWithGuestInfo(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/validate-booking/{bookingId}/{propertyId}")
    @Operation(summary = "Validate booking for review creation", 
               description = "Demonstrates MySQL stored procedure integration: ValidateBookingExists and IsBookingCompleted for review validation. BUSINESS RULE: Reviews require completed bookings.")
    public ResponseEntity<Map<String, Object>> validateBookingForReview(
            @PathVariable Integer bookingId, 
            @PathVariable Integer propertyId) {
        
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: Validate booking exists using stored procedure
            boolean bookingExists = reviewService.validateBookingExists(bookingId, propertyId);
            
            // Step 2: Check if booking is completed using stored procedure
            boolean bookingCompleted = reviewService.isBookingCompleted(bookingId);
            
            // Step 3: Get detailed booking information using stored procedure
            Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
            
            // Build comprehensive response
            result.put("success", true);
            
            // Validation Results
            result.put("booking_exists", bookingExists);
            result.put("booking_completed", bookingCompleted);
            result.put("can_create_review", bookingExists && bookingCompleted);
            
            // Business Rule
            result.put("business_rule", "Reviews can only be created for completed bookings");
            
            // Status and Recommendation
            if (bookingCompleted && bookingExists) {
                result.put("status", "Ready for review");
                result.put("recommendation", "Review creation allowed - booking is completed");
            } else if (bookingExists && !bookingCompleted) {
                result.put("status", "Booking active or upcoming");
                result.put("recommendation", "Review creation blocked - booking not yet completed");
            } else {
                result.put("status", "Invalid booking");
                result.put("recommendation", "Review creation blocked - booking not found");
            }
            
            // Booking Details
            if (bookingInfo != null) {
                result.put("booking_details", bookingInfo);
            }
            
            // Technical Documentation
            Map<String, Object> storedProcedures = new HashMap<>();
            storedProcedures.put("database", "MySQL");
            storedProcedures.put("validation_procedure", "CALL ValidateBookingExists(" + bookingId + ", " + propertyId + ")");
            storedProcedures.put("completion_procedure", "CALL IsBookingCompleted(" + bookingId + ")");
            storedProcedures.put("info_procedure", "CALL GetGuestInfoFromBooking(" + bookingId + ")");
            storedProcedures.put("architecture", "Dual stored procedure approach demonstrating traditional SQL procedures");
            storedProcedures.put("business_rule_enforcement", "Reviews require both valid and completed bookings");
            
            result.put("stored_procedures_used", storedProcedures);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "MySQL stored procedure validation error: " + e.getMessage());
            result.put("booking_id", bookingId);
            result.put("property_id", propertyId);
            return ResponseEntity.status(500).body(result);
        }
    }
    
    @GetMapping("/cross-database-demo/{propertyId}")
    @Operation(summary = "Comprehensive cross-database integration demonstration", 
               description = "Complete demonstration of dual stored procedure architecture: MySQL procedures + MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> crossDatabaseDemo(@PathVariable Integer propertyId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Step 1: MongoDB aggregation pipeline - get reviews
            Map<String, Object> reviewsResult = reviewService.getPropertyReviews(propertyId, 5, 0, "created_at", "desc");
            
            // Step 2: MongoDB aggregation pipeline - get analytics
            Map<String, Object> analyticsResult = reviewService.getReviewAnalytics(propertyId);
            
            // Step 3: Cross-database transformation - reviews with guest info
            Map<String, Object> enrichedReviews = reviewService.getReviewsWithGuestInfo(propertyId);
            
            // Step 4: MySQL stored procedure - get all related bookings
            Map<String, Object> allBookings = bookingRepository.getAllBookings();
            
            // Build comprehensive demonstration response
            result.put("success", true);
            result.put("demonstration_type", "Complete Dual Database Stored Procedure Architecture");
            
            // MongoDB operations
            Map<String, Object> mongodbOperations = new HashMap<>();
            mongodbOperations.put("reviews", reviewsResult);
            mongodbOperations.put("analytics", analyticsResult);
            mongodbOperations.put("cross_database_enrichment", enrichedReviews);
            mongodbOperations.put("approach", "MongoDB aggregation pipelines (modern stored procedure equivalent)");
            result.put("mongodb_operations", mongodbOperations);
            
            // MySQL operations
            Map<String, Object> mysqlOperations = new HashMap<>();
            mysqlOperations.put("all_bookings", allBookings);
            mysqlOperations.put("approach", "Traditional MySQL stored procedures");
            result.put("mysql_operations", mysqlOperations);
            
            // Architecture documentation
            Map<String, Object> architecture = new HashMap<>();
            architecture.put("mysql_procedures", new String[]{
                "ValidateBookingExists", "IsBookingCompleted", "GetGuestInfoFromBooking", "GetAllBookings"
            });
            architecture.put("mongodb_pipelines", new String[]{
                "property reviews aggregation", "review analytics aggregation", "rating calculations aggregation"
            });
            architecture.put("data_transformation", "Real-time MongoDB booking_id -> MySQL stored procedure calls -> enriched responses");
            architecture.put("integration_method", "Spring Boot SimpleJdbcCall + MongoTemplate");
            architecture.put("academic_value", "Demonstrates both traditional SQL and modern NoSQL stored procedure approaches");
            
            result.put("architecture_details", architecture);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", "Cross-database demonstration error: " + e.getMessage());
            result.put("property_id", propertyId);
            return ResponseEntity.status(500).body(result);
        }
    }
} 
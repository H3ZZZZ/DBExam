package com.airbnb.backend.controller;

import com.airbnb.backend.service.ReviewService;
import com.airbnb.backend.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.LinkedHashMap;

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
    @Operation(summary = "Add review and update property rating", 
               description = "Add a review with MySQL stored procedure validation and automatically trigger MongoDB aggregation pipeline rating recalculation.")
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestParam Integer propertyId,
            @RequestParam Integer bookingId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        Map<String, Object> result = reviewService.addReview(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
        
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
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
               description = "Generate analytics and trends for property reviews using MongoDB aggregation pipelines")
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
               description = "Demonstrates cross-database data transformation: MongoDB reviews enriched with MySQL guest data via stored procedures")
    public ResponseEntity<Map<String, Object>> getReviewsWithGuestInfo(@PathVariable Integer propertyId) {
        Map<String, Object> result = reviewService.getReviewsWithGuestInfo(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/validate-booking/{bookingId}/{propertyId}")
    @Operation(summary = "Validate booking for review creation", 
               description = "Demonstrates MySQL stored procedure integration for review validation")
    public ResponseEntity<Map<String, Object>> validateBookingForReview(
            @PathVariable Integer bookingId, 
            @PathVariable Integer propertyId) {
        
        try {
            // Check if booking exists and matches property
            boolean bookingValid = reviewService.validateBookingExists(bookingId, propertyId);
            if (!bookingValid) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("booking_id", bookingId);
                response.put("error", "Invalid booking - booking not found for property");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Check if booking is completed
            boolean bookingCompleted = reviewService.isBookingCompleted(bookingId);
            
            // Get additional booking information
            Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("booking_valid", true);
            response.put("booking_completed", bookingCompleted);
            response.put("can_create_review", bookingCompleted);
            response.put("guest_name", bookingInfo != null ? bookingInfo.get("guest_name") : "Unknown");
            response.put("booking_price", bookingInfo != null ? bookingInfo.get("booking_price") : null);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("error", "MySQL stored procedure validation error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/cross-database-demo/{propertyId}")
    @Operation(summary = "Cross-database integration demonstration", 
               description = "Complete demonstration of dual database architecture: MySQL procedures + MongoDB aggregation pipelines")
    public ResponseEntity<Map<String, Object>> crossDatabaseDemo(@PathVariable Integer propertyId) {
        try {
            // Get MongoDB operations
            Map<String, Object> reviewsResult = reviewService.getPropertyReviews(propertyId, 5, 0, "created_at", "desc");
            Map<String, Object> analyticsResult = reviewService.getReviewAnalytics(propertyId);
            Map<String, Object> enrichedReviews = reviewService.getReviewsWithGuestInfo(propertyId);
            
            // Get MySQL operations
            Map<String, Object> allBookings = bookingRepository.getAllBookings();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("mongodb_reviews", reviewsResult.get("reviews"));
            response.put("mongodb_analytics", analyticsResult);
            response.put("cross_database_enriched_reviews", enrichedReviews.get("reviews_with_guest_info"));
            response.put("mysql_bookings", allBookings.get("bookings"));
            response.put("demonstration_note", "Dual Database Architecture: MySQL stored procedures + MongoDB aggregation pipelines");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("error", "Cross-database demonstration error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 
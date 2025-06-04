package com.airbnb.backend.controller;

import com.airbnb.backend.service.ReviewService;
import com.airbnb.backend.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/workflow")
@Tag(name = "Booking Workflow", description = "Demonstration of the complete booking-to-review workflow")
public class WorkflowController {
    
    @Autowired
    private ReviewService reviewService;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @PostMapping("/complete-booking-review")
    @Operation(summary = "Complete booking workflow with review and rating update", 
               description = "Demonstrates: User books property → Booking ends → User reviews → Property ratings updated via cross-database transformation")
    public ResponseEntity<Map<String, Object>> completeBookingReview(
            @RequestParam Integer propertyId,
            @RequestParam Integer bookingId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        
        try {
            // Step 1: MySQL stored procedure validation - check booking exists and matches property
            boolean bookingValid = reviewService.validateBookingExists(bookingId, propertyId);
            if (!bookingValid) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("property_id", propertyId);
                response.put("booking_id", bookingId);
                response.put("error", "MySQL validation failed - booking not found for property");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Step 2: Check if booking is completed using stored procedure
            boolean bookingCompleted = reviewService.isBookingCompleted(bookingId);
            
            // Step 3: Add review to MongoDB with booking_id reference (cross-database workflow)
            Map<String, Object> reviewResult = reviewService.addReviewWithRatingUpdate(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
            
            // Build clean workflow response
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("booking_completed", bookingCompleted);
            response.put("review_created", Boolean.TRUE.equals(reviewResult.get("success")));
            response.put("rating_updated", reviewResult.get("rating_updated"));
            
            if (Boolean.TRUE.equals(reviewResult.get("success"))) {
                response.put("review_id", reviewResult.get("review_id"));
                response.put("new_avg_cleanliness", reviewResult.get("new_avg_cleanliness"));
                response.put("new_avg_satisfaction", reviewResult.get("new_avg_satisfaction"));
                response.put("total_reviews", reviewResult.get("total_reviews"));
            } else {
                response.put("review_error", reviewResult.get("error"));
            }
            
            response.put("workflow_note", "MySQL validation → MongoDB review → Rating update");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("property_id", propertyId);
            response.put("booking_id", bookingId);
            response.put("error", "Workflow error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/booking-status/{bookingId}")
    @Operation(summary = "Check booking status", 
               description = "MySQL stored procedure query to check if a booking is completed and eligible for review")
    public ResponseEntity<Map<String, Object>> checkBookingStatus(@PathVariable Integer bookingId) {
        try {
            // MySQL stored procedure call for booking information
            Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
            
            if (bookingInfo != null) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("booking_id", bookingInfo.get("booking_id"));
                response.put("property_id", bookingInfo.get("property_id"));
                response.put("booking_found", true);
                response.put("guest_name", bookingInfo.get("guest_name"));
                response.put("booking_status", bookingInfo.get("booking_status"));
                response.put("booking_start", bookingInfo.get("booking_start"));
                response.put("booking_end", bookingInfo.get("booking_end"));
                response.put("booking_price", bookingInfo.get("booking_price"));
                response.put("can_review", true);
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", false);
                response.put("booking_id", bookingId);
                response.put("booking_found", false);
                response.put("error", "Booking not found in MySQL database");
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("booking_id", bookingId);
            response.put("error", "MySQL stored procedure error: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/demo-data")
    @Operation(summary = "Get demonstration data", 
               description = "Returns actual booking data from MySQL stored procedure for testing the workflow")
    public ResponseEntity<Map<String, Object>> getDemoData() {
        try {
            // Get booking data from MySQL using stored procedure
            Map<String, Object> allBookingsResult = bookingRepository.getAllBookings();
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("mysql_bookings", allBookingsResult.get("bookings"));
            response.put("total_bookings", allBookingsResult.get("total_bookings"));
            response.put("usage_note", "Use these booking IDs to test the workflow endpoints");
            response.put("workflow_endpoint", "/api/workflow/complete-booking-review");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Failed to retrieve booking data: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
} 
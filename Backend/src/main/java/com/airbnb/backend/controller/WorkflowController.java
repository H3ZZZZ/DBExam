package com.airbnb.backend.controller;

import com.airbnb.backend.service.ReviewService;
import com.airbnb.backend.repository.BookingRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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
               description = "Demonstrates: User2 books property → Booking ends → User2 reviews (using booking_id) → Property ratings updated via cross-database transformation using stored procedures")
    public ResponseEntity<Map<String, Object>> completeBookingReview(
            @RequestParam Integer propertyId,
            @RequestParam Integer bookingId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        
        Map<String, Object> workflowResult = new HashMap<>();
        
        try {
            // Step 1: REAL MySQL stored procedure validation - check booking exists and matches property
            boolean bookingValid = reviewService.validateBookingExists(bookingId, propertyId);
            if (!bookingValid) {
                workflowResult.put("success", false);
                workflowResult.put("error", "REAL MySQL stored procedure validation failed - booking_id " + bookingId + " not found for property_id " + propertyId);
                workflowResult.put("validation_source", "MySQL stored procedure: ValidateBookingExists");
                return ResponseEntity.badRequest().body(workflowResult);
            }
            
            // Step 2: Check if booking is completed using stored procedure (optional validation)
            boolean bookingCompleted = reviewService.isBookingCompleted(bookingId);
            if (!bookingCompleted) {
                workflowResult.put("warning", "Booking not yet completed - review can still be created but typically would wait");
                workflowResult.put("booking_status", "active or upcoming");
                workflowResult.put("completion_check", "MySQL stored procedure: IsBookingCompleted");
            } else {
                workflowResult.put("booking_status", "completed");
                workflowResult.put("completion_check", "MySQL stored procedure: IsBookingCompleted");
            }
            
            // Step 3: Add review to MongoDB with booking_id reference (demonstrates data transformation pattern)
            Map<String, Object> reviewResult = reviewService.addReviewWithRatingUpdate(propertyId, bookingId, cleanlinessRating, satisfactionRating, comment);
            
            // Step 4: Document the cross-database workflow completed
            workflowResult.put("success", true);
            workflowResult.put("workflow_completed", "REAL Stored Procedure Validation → MongoDB Review → Rating Update");
            workflowResult.put("review_result", reviewResult);
            
            // Step 5: Add REAL transformation metadata with stored procedure details
            Map<String, Object> transformationProcess = new HashMap<>();
            transformationProcess.put("step_1", "MySQL stored procedure: CALL ValidateBookingExists(?, ?)");
            transformationProcess.put("step_2", "MySQL stored procedure: CALL IsBookingCompleted(?)");
            transformationProcess.put("step_3", "MongoDB review creation with booking_id reference");
            transformationProcess.put("step_4", "MongoDB aggregation pipeline: property rating recalculation");
            transformationProcess.put("data_transformation", "REAL cross-database stored procedure validation and data enrichment");
            transformationProcess.put("databases_involved", "MySQL (stored procedures) + MongoDB (aggregation pipelines)");
            transformationProcess.put("validation_method", "SimpleJdbcCall with MySQL stored procedures");
            transformationProcess.put("architecture", "Dual stored procedure approach: MySQL stored procedures + MongoDB aggregation pipelines");
            
            workflowResult.put("transformation_details", transformationProcess);
            
            return ResponseEntity.ok(workflowResult);
            
        } catch (Exception e) {
            workflowResult.put("success", false);
            workflowResult.put("error", "Workflow error: " + e.getMessage());
            workflowResult.put("error_type", "Real MySQL stored procedure integration error");
            return ResponseEntity.status(500).body(workflowResult);
        }
    }
    
    @GetMapping("/booking-status/{bookingId}")
    @Operation(summary = "Check booking status", 
               description = "REAL MySQL stored procedure query to check if a booking is completed and eligible for review")
    public ResponseEntity<Map<String, Object>> checkBookingStatus(@PathVariable Integer bookingId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // REAL MySQL stored procedure call for booking information
            Map<String, Object> bookingInfo = bookingRepository.getGuestInfoFromBooking(bookingId);
            
            if (bookingInfo != null) {
                response.put("success", true);
                response.put("booking_found", true);
                response.put("booking_id", bookingInfo.get("booking_id"));
                response.put("property_id", bookingInfo.get("property_id"));
                response.put("guest_name", bookingInfo.get("guest_name"));
                response.put("booking_status", bookingInfo.get("booking_status"));
                response.put("booking_start", bookingInfo.get("booking_start"));
                response.put("booking_end", bookingInfo.get("booking_end"));
                response.put("booking_price", bookingInfo.get("booking_price"));
                response.put("can_review", true); // Could add business logic here
                response.put("data_source", "MySQL stored procedure: GetGuestInfoFromBooking");
                response.put("stored_procedure", "CALL GetGuestInfoFromBooking(?)");
            } else {
                response.put("success", false);
                response.put("booking_found", false);
                response.put("booking_id", bookingId);
                response.put("error", "Booking not found in MySQL database");
                response.put("data_source", "MySQL stored procedure returned null");
                response.put("stored_procedure", "CALL GetGuestInfoFromBooking(?)");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "MySQL stored procedure error: " + e.getMessage());
            response.put("booking_id", bookingId);
            response.put("stored_procedure", "CALL GetGuestInfoFromBooking(?)");
            return ResponseEntity.status(500).body(response);
        }
    }
    
    @GetMapping("/demo-data")
    @Operation(summary = "Get REAL demonstration data", 
               description = "Returns actual booking data from MySQL stored procedure for testing the workflow")
    public ResponseEntity<Map<String, Object>> getDemoData() {
        try {
            // Get REAL booking data from MySQL using stored procedure
            Map<String, Object> allBookingsResult = bookingRepository.getAllBookings();
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data_source", "MySQL stored procedure: GetAllBookings");
            response.put("mysql_bookings", allBookingsResult);
            response.put("usage", "Use these REAL booking IDs to test the workflow endpoints");
            response.put("workflow_endpoint", "/api/workflow/complete-booking-review");
            response.put("validation_note", "All booking validations use real MySQL stored procedures");
            response.put("stored_procedure", "CALL GetAllBookings()");
            response.put("architecture", "Dual stored procedure approach: MySQL + MongoDB");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("error", "Failed to retrieve real booking data: " + e.getMessage());
            response.put("stored_procedure", "CALL GetAllBookings()");
            return ResponseEntity.status(500).body(response);
        }
    }
} 
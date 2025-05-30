package com.airbnb.backend.controller;

import com.airbnb.backend.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/reviews")
@Tag(name = "Reviews", description = "API for managing property reviews using MongoDB aggregation pipelines")
public class ReviewController {
    
    @Autowired
    private ReviewService reviewService;
    
    @GetMapping("/property/{propertyId}")
    @Operation(summary = "Get reviews for a property", 
               description = "Retrieve reviews for a specific property with pagination and sorting options")
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
    @Operation(summary = "Add a new review", 
               description = "Add a review with automatic validation")
    public ResponseEntity<Map<String, Object>> addReview(
            @RequestParam Integer propertyId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        Map<String, Object> result = reviewService.addReview(propertyId, cleanlinessRating, satisfactionRating, comment);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/add-with-rating-update")
    @Operation(summary = "Add review and trigger property rating update", 
               description = "Add a review and automatically trigger property rating recalculation")
    public ResponseEntity<Map<String, Object>> addReviewWithRatingUpdate(
            @RequestParam Integer propertyId,
            @RequestParam Integer cleanlinessRating,
            @RequestParam Integer satisfactionRating,
            @RequestParam String comment) {
        Map<String, Object> result = reviewService.addReviewWithRatingUpdate(propertyId, cleanlinessRating, satisfactionRating, comment);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all reviews with pagination", 
               description = "Retrieve all reviews with advanced filtering and sorting options")
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
               description = "Generate advanced analytics and trends for property reviews")
    public ResponseEntity<Map<String, Object>> getReviewAnalytics(@PathVariable Integer propertyId) {
        Map<String, Object> result = reviewService.getReviewAnalytics(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/summary")
    @Operation(summary = "Get review summary statistics", 
               description = "Generate system-wide review summary statistics")
    public ResponseEntity<Map<String, Object>> getReviewSummary() {
        Map<String, Object> result = reviewService.getReviewSummary();
        return ResponseEntity.ok(result);
    }
} 
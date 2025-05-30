package com.airbnb.backend.controller;

import com.airbnb.backend.service.PropertyRatingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/property-ratings")
@Tag(name = "Property Ratings", description = "API for managing property ratings using MongoDB aggregation pipelines")
public class PropertyRatingController {
    
    @Autowired
    private PropertyRatingService propertyRatingService;
    
    @PostMapping("/calculate/{propertyId}")
    @Operation(summary = "Calculate ratings for a property", 
               description = "Calculate and update average ratings for a specific property")
    public ResponseEntity<Map<String, Object>> calculatePropertyRating(@PathVariable Integer propertyId) {
        Map<String, Object> result = propertyRatingService.updatePropertyRatingAfterNewReview(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping("/recalculate-all")
    @Operation(summary = "Recalculate all property ratings", 
               description = "Batch recalculate ratings for all properties")
    public ResponseEntity<Map<String, Object>> recalculateAllRatings() {
        Map<String, Object> result = propertyRatingService.recalculateAllRatings();
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{propertyId}")
    @Operation(summary = "Get property rating", description = "Retrieve current ratings for a specific property")
    public ResponseEntity<Map<String, Object>> getPropertyRating(@PathVariable Integer propertyId) {
        Map<String, Object> result = propertyRatingService.getPropertyRating(propertyId);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{propertyId}/summary")
    @Operation(summary = "Get property rating summary", description = "Get a formatted text summary of property ratings")
    public ResponseEntity<String> getPropertyRatingSummary(@PathVariable Integer propertyId) {
        String summary = propertyRatingService.getRatingsSummary(propertyId);
        return ResponseEntity.ok(summary);
    }
    
    @GetMapping("/all")
    @Operation(summary = "Get all property ratings", description = "Retrieve all property ratings with pagination")
    public ResponseEntity<Map<String, Object>> getAllPropertyRatings(
            @RequestParam(defaultValue = "100") Integer limit,
            @RequestParam(defaultValue = "0") Integer skip) {
        Map<String, Object> result = propertyRatingService.getAllPropertyRatings(limit, skip);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/top-rated")
    @Operation(summary = "Get top rated properties", description = "Get properties with highest ratings")
    public ResponseEntity<Map<String, Object>> getTopRatedProperties(
            @RequestParam(defaultValue = "10") Integer limit,
            @RequestParam(defaultValue = "satisfaction") String ratingType) {
        Map<String, Object> result = propertyRatingService.getTopRatedProperties(limit, ratingType);
        return ResponseEntity.ok(result);
    }
    
    @GetMapping("/{propertyId}/exists")
    @Operation(summary = "Check if property has ratings", description = "Check if ratings have been calculated for a property")
    public ResponseEntity<Boolean> hasRatings(@PathVariable Integer propertyId) {
        boolean hasRatings = propertyRatingService.hasRatings(propertyId);
        return ResponseEntity.ok(hasRatings);
    }
} 
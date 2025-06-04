package com.airbnb.backend.service;

import com.airbnb.backend.repository.PropertyRatingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PropertyRatingService {
    
    @Autowired
    private PropertyRatingRepository propertyRatingRepository;
    
    /**
     * Update average ratings for a property when a new review is added
     */
    public Map<String, Object> updatePropertyRatingAfterNewReview(Integer propertyId) {
        return propertyRatingRepository.calculatePropertyRating(propertyId);
    }
    
    /**
     * Batch update all property ratings
     */
    public Map<String, Object> recalculateAllRatings() {
        return propertyRatingRepository.recalculateAllPropertyRatings();
    }
    
    /**
     * Get property rating by ID
     */
    public Map<String, Object> getPropertyRating(Integer propertyId) {
        return propertyRatingRepository.getPropertyRating(propertyId);
    }
    
    /**
     * Get all property ratings with pagination
     */
    public Map<String, Object> getAllPropertyRatings(Integer limit, Integer skip) {
        return propertyRatingRepository.getAllPropertyRatings(limit, skip);
    }
    
    /**
     * Get top rated properties
     */
    public Map<String, Object> getTopRatedProperties(Integer limit, String ratingType) {
        return propertyRatingRepository.getTopRatedProperties(limit, ratingType);
    }
    
    /**
     * Check if a property has ratings calculated
     */
    public boolean hasRatings(Integer propertyId) {
        Map<String, Object> result = getPropertyRating(propertyId);
        return result != null && 
               result.containsKey("success") && 
               Boolean.TRUE.equals(result.get("success"));
    }
    
    /**
     * Get rating statistics summary
     */
    public String getRatingsSummary(Integer propertyId) {
        Map<String, Object> result = getPropertyRating(propertyId);
        
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            return String.format(
                "Property %d: Cleanliness %.2f, Satisfaction %.2f (based on %d reviews)",
                propertyId,
                getNumberValue(result.get("avg_cleanliness_rating")).doubleValue(),
                getNumberValue(result.get("avg_satisfaction_rating")).doubleValue(),
                getNumberValue(result.get("total_reviews")).intValue()
            );
        }
        
        return result != null && result.containsKey("message") ? 
            result.get("message").toString() : 
            "Error retrieving ratings for property " + propertyId;
    }
    
    private Number getNumberValue(Object value) {
        if (value instanceof Number) {
            return (Number) value;
        } else if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return 0;
            }
        }
        return 0;
    }
}  
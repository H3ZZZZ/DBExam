package com.airbnb.backend.service;

import com.airbnb.backend.dto.PropertyDetailsDTO;
import com.airbnb.backend.dto.PropertyDTO;
import com.airbnb.backend.repository.PropertyRatingRepository;
import com.airbnb.backend.repository.ReviewRepository;
import com.airbnb.backend.service.PropertyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class PropertyDetailsService {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private PropertyRatingRepository propertyRatingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public PropertyDetailsDTO getCompletePropertyDetails(int propertyId) {
        PropertyDTO property = propertyService.getPropertyById(propertyId);
        Map<String, Object> ratingResult = propertyRatingRepository.getPropertyRating(propertyId);
        Map<String, Object> reviewsResult = reviewRepository.getPropertyReviews(propertyId, 10, 0, "created_at", "desc");

        PropertyDetailsDTO dto = new PropertyDetailsDTO();

        // Set SQL data
        dto.setId(property.getId());
        dto.setHostId(property.getHostId());
        dto.setPrice(property.getPrice());
        dto.setRoomType(property.getRoomType());
        dto.setPersonCapacity(property.getPersonCapacity());
        dto.setBedrooms(property.getBedrooms());
        dto.setCenterDistance(property.getCenterDistance());
        dto.setMetroDistance(property.getMetroDistance());
        dto.setCity(property.getCity());

        // Set rating data
        if (ratingResult != null && Boolean.TRUE.equals(ratingResult.get("success"))) {
            dto.setAvgCleanlinessRating(((Number) ratingResult.get("avg_cleanliness_rating")).doubleValue());
            dto.setAvgSatisfactionRating(((Number) ratingResult.get("avg_satisfaction_rating")).doubleValue());
            dto.setTotalReviews(((Number) ratingResult.get("total_reviews")).intValue());
        }

        // Set reviews
        if (reviewsResult != null && Boolean.TRUE.equals(reviewsResult.get("success"))) {
            dto.setReviews((List<Object>) reviewsResult.get("reviews"));
        }

        return dto;
    }
}

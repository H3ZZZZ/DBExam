package com.airbnb.backend.service;

import com.airbnb.backend.dto.UserDTO;
import com.airbnb.backend.repository.BookingRepository;
import com.airbnb.backend.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
public class HostDashboardService {

    @Autowired
    private UserService userService; // Used for host info

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Map<String, Object> getHostDashboard(int hostId) {
        Map<String, Object> response = new LinkedHashMap<>();

        // 1. Host Info (reuse UserService)
        UserDTO host = userService.getUserById(hostId);
        response.put("host", host);

        // 2. Properties owned by host
        List<Map<String, Object>> properties = propertyService.getPropertiesByHostId(hostId);
        response.put("properties", properties);

        // 3. Bookings across all properties
        List<Map<String, Object>> allBookings = new ArrayList<>();
        List<Integer> propertyIds = new ArrayList<>();

        for (Map<String, Object> property : properties) {
            Integer propertyId = (Integer) property.get("id");
            propertyIds.add(propertyId);
            List<Map<String, Object>> propertyBookings = bookingRepository.getBookingsByPropertyId(propertyId);
            allBookings.addAll(propertyBookings);
        }
        response.put("bookings", allBookings);

        // 4. Reviews across those properties
        List<Map<String, Object>> reviews = reviewRepository.getReviewsByPropertyIds(propertyIds);
        response.put("reviews", reviews);

        // 5. Aggregated stats
        BigDecimal totalEarnings = allBookings.stream()
                .map(b -> (BigDecimal) b.get("booking_price"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        response.put("total_earnings", totalEarnings);
        response.put("total_properties", properties.size());
        response.put("total_bookings", allBookings.size());
        response.put("total_reviews", reviews.size());

        return response;
    }
}

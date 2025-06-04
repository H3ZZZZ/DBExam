package com.airbnb.backend.service;

import com.airbnb.backend.dto.UserDTO;
import com.airbnb.backend.repository.BookingRepository;
import com.airbnb.backend.repository.ReviewRepository;
import com.airbnb.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserDashboardService {

    @Autowired
    private UserService userService;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    public Map<String, Object> getUserDashboard(int userId) {
        Map<String, Object> response = new HashMap<>();

        // 1. User Info
        UserDTO user = userService.getUserById(userId); // You might need to implement getUserById
        response.put("user", user);

        // 2. All bookings for this user
        List<Map<String, Object>> userBookings = bookingRepository.getBookingsByGuestId(userId); // New method needed
        response.put("bookings", userBookings);

        // 3. Extract booking IDs and fetch reviews
        List<Integer> bookingIds = userBookings.stream()
                .map(b -> (Integer) b.get("booking_id"))
                .toList();

        List<Map<String, Object>> reviews = reviewRepository.getReviewsByBookingIds(bookingIds); // You likely need to add this
        response.put("reviews", reviews);

        // 4. Aggregated stats
        BigDecimal totalSpent = userBookings.stream()
                .map(b -> (BigDecimal) b.get("booking_price"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.put("total_spent", totalSpent);
        response.put("total_bookings", userBookings.size());
        response.put("total_reviews", reviews.size());

        return response;
    }
}

package com.airbnb.backend.controller;

import com.airbnb.backend.dto.BookingDTO;
import com.airbnb.backend.service.BookingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> addBooking(@RequestBody BookingDTO booking) {
        bookingService.addBooking(
                booking.getPropertyId(),
                booking.getGuestId(),
                booking.getBookingStart(),
                booking.getBookingEnd()
        );
        return ResponseEntity.ok("Booking added successfully");
    }
}
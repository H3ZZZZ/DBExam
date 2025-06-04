package com.airbnb.backend.controller;

import com.airbnb.backend.dto.BookingCreateDTO;
import com.airbnb.backend.dto.BookingDTO;
import com.airbnb.backend.dto.BookingUpdateDTO;
import com.airbnb.backend.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@Tag(name = "Booking Controller", description = "API for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    public ResponseEntity<String> addBooking(@RequestBody BookingCreateDTO booking) {
        bookingService.addBooking(
                booking.getPropertyId(),
                booking.getGuestId(),
                booking.getBookingStart(),
                booking.getBookingEnd()
        );
        return ResponseEntity.ok("Booking added successfully");
    }

    @GetMapping
    public ResponseEntity<List<BookingDTO>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }


    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> getBookingById(@PathVariable int id) {
        BookingDTO booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(booking);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateBooking(@PathVariable int id, @RequestBody BookingUpdateDTO booking) {
        bookingService.updateBooking(id, booking);
        return ResponseEntity.ok("Booking updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteBooking(@PathVariable int id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok("Booking deleted successfully");
    }




}
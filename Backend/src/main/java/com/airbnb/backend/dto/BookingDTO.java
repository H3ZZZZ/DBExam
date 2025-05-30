package com.airbnb.backend.dto;
import java.time.LocalDate;

public class BookingDTO {
    private int propertyId;
    private int guestId;
    private LocalDate bookingStart;
    private LocalDate bookingEnd;

    public BookingDTO() {}

    public int getPropertyId() { return propertyId; }
    public void setPropertyId(int propertyId) { this.propertyId = propertyId; }

    public int getGuestId() { return guestId; }
    public void setGuestId(int guestId) { this.guestId = guestId; }

    public LocalDate getBookingStart() { return bookingStart; }
    public void setBookingStart(LocalDate bookingStart) { this.bookingStart = bookingStart; }

    public LocalDate getBookingEnd() { return bookingEnd; }
    public void setBookingEnd(LocalDate bookingEnd) { this.bookingEnd = bookingEnd; }
}
package com.airbnb.backend.dto;

import java.time.LocalDate;

public class BookingUpdateDTO {
    private LocalDate bookingStart;
    private LocalDate bookingEnd;

    public BookingUpdateDTO() {}

    public LocalDate getBookingStart() {
        return bookingStart;
    }

    public void setBookingStart(LocalDate bookingStart) {
        this.bookingStart = bookingStart;
    }

    public LocalDate getBookingEnd() {
        return bookingEnd;
    }

    public void setBookingEnd(LocalDate bookingEnd) {
        this.bookingEnd = bookingEnd;
    }
}

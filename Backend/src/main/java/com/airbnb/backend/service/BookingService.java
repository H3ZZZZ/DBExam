package com.airbnb.backend.service;

import com.airbnb.backend.dto.BookingDTO;
import com.airbnb.backend.dto.BookingUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.*;
import java.time.LocalDate;

@Service
public class BookingService {

    @Autowired
    private DataSource dataSource;

    public void addBooking(int propertyId, int guestId, java.time.LocalDate start, java.time.LocalDate end) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL AddBooking(?, ?, ?, ?)}")) {

            stmt.setInt(1, propertyId);
            stmt.setInt(2, guestId);
            stmt.setDate(3, Date.valueOf(start));
            stmt.setDate(4, Date.valueOf(end));

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure AddBooking", e);
        }
    }

    public BookingDTO getBookingById(int bookingId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL GetBooking(?)}")) {

            stmt.setInt(1, bookingId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    BookingDTO booking = new BookingDTO();
                    booking.setId(rs.getInt("ID"));
                    booking.setPropertyId(rs.getInt("Property_ID"));
                    booking.setGuestId(rs.getInt("Guest_ID"));
                    booking.setPrice(rs.getBigDecimal("Price"));
                    booking.setBookingStart(rs.getDate("Booking_start").toLocalDate());
                    booking.setBookingEnd(rs.getDate("Booking_end").toLocalDate());
                    return booking;
                } else {
                    throw new RuntimeException("Booking not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure GetBooking", e);
        }
    }

    public void updateBooking(int bookingId, BookingUpdateDTO booking) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL UpdateBooking(?, ?, ?)}")) {

            stmt.setInt(1, bookingId);

            if (booking.getBookingStart() != null) {
                stmt.setDate(2, Date.valueOf(booking.getBookingStart()));
            } else {
                stmt.setNull(2, Types.DATE);
            }

            if (booking.getBookingEnd() != null) {
                stmt.setDate(3, Date.valueOf(booking.getBookingEnd()));
            } else {
                stmt.setNull(3, Types.DATE);
            }

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure UpdateBooking", e);
        }
    }

    public void deleteBooking(int bookingId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL DeleteBooking(?)}")) {

            stmt.setInt(1, bookingId);
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure DeleteBooking", e);
        }
    }

}
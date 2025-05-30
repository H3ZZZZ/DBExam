package com.airbnb.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;

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
}
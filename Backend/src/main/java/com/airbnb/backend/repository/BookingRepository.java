package com.airbnb.backend.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.util.LinkedHashMap;
import java.util.Map;

@Repository
public class BookingRepository {
    
    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    /**
     * Get guest information from booking ID using stored procedure (demonstrates cross-database data transformation)
     */
    public Map<String, Object> getGuestInfoFromBooking(Integer bookingId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GetGuestInfoFromBooking")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_booking_id", Types.INTEGER)
                )
                .returningResultSet("booking_info", (rs, rowNum) -> {
                    Map<String, Object> result = new LinkedHashMap<>();
                    result.put("booking_id", rs.getInt("booking_id"));
                    result.put("property_id", rs.getInt("property_id"));
                    result.put("guest_id", rs.getInt("guest_id"));
                    result.put("guest_name", rs.getString("guest_name"));
                    result.put("guest_email", rs.getString("guest_email"));
                    result.put("booking_start", rs.getDate("Booking_start"));
                    result.put("booking_end", rs.getDate("Booking_end"));
                    result.put("booking_price", rs.getBigDecimal("booking_price"));
                    result.put("booking_status", rs.getString("booking_status"));
                    result.put("data_source", "MySQL stored procedure: GetGuestInfoFromBooking");
                    return result;
                });
            
            Map<String, Object> inParams = new LinkedHashMap<>();
            inParams.put("p_booking_id", bookingId);
            
            Map<String, Object> result = jdbcCall.execute(inParams);
            @SuppressWarnings("unchecked")
            var bookingList = (java.util.List<Map<String, Object>>) result.get("booking_info");
            
            if (bookingList != null && !bookingList.isEmpty()) {
                return bookingList.get(0);
            } else {
                return null;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error calling stored procedure GetGuestInfoFromBooking for booking " + bookingId + ": " + e.getMessage(), e);
        }
    }
    
    /**
     * Validate that a booking exists and matches the property using stored procedure
     */
    public boolean validateBookingExists(Integer bookingId, Integer propertyId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("ValidateBookingExists")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_booking_id", Types.INTEGER),
                    new SqlParameter("p_property_id", Types.INTEGER),
                    new SqlOutParameter("p_exists", Types.BOOLEAN)
                );
            
            Map<String, Object> inParams = new LinkedHashMap<>();
            inParams.put("p_booking_id", bookingId);
            inParams.put("p_property_id", propertyId);
            
            Map<String, Object> result = jdbcCall.execute(inParams);
            Object exists = result.get("p_exists");
            
            // Handle different possible return types for boolean
            if (exists instanceof Boolean) {
                return (Boolean) exists;
            } else if (exists instanceof Number) {
                return ((Number) exists).intValue() != 0;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error calling stored procedure ValidateBookingExists: " + e.getMessage(), e);
        }
    }
    
    /**
     * Check if booking is completed using stored procedure
     */
    public boolean isBookingCompleted(Integer bookingId) {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("IsBookingCompleted")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                    new SqlParameter("p_booking_id", Types.INTEGER),
                    new SqlOutParameter("p_completed", Types.BOOLEAN)
                );
            
            Map<String, Object> inParams = new LinkedHashMap<>();
            inParams.put("p_booking_id", bookingId);
            
            Map<String, Object> result = jdbcCall.execute(inParams);
            Object completed = result.get("p_completed");
            
            // Handle different possible return types for boolean
            if (completed instanceof Boolean) {
                return (Boolean) completed;
            } else if (completed instanceof Number) {
                return ((Number) completed).intValue() != 0;
            } else {
                return false;
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Error calling stored procedure IsBookingCompleted: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all bookings using stored procedure
     */
    public Map<String, Object> getAllBookings() {
        try {
            SimpleJdbcCall jdbcCall = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("GetAllBookings")
                .withoutProcedureColumnMetaDataAccess()
                .returningResultSet("bookings", (rs, rowNum) -> {
                    Map<String, Object> booking = new LinkedHashMap<>();
                    booking.put("booking_id", rs.getInt("booking_id"));
                    booking.put("property_id", rs.getInt("property_id"));
                    booking.put("guest_id", rs.getInt("guest_id"));
                    booking.put("guest_name", rs.getString("guest_name"));
                    booking.put("booking_start", rs.getDate("Booking_start"));
                    booking.put("booking_end", rs.getDate("Booking_end"));
                    booking.put("booking_price", rs.getBigDecimal("booking_price"));
                    booking.put("booking_status", rs.getString("booking_status"));
                    return booking;
                });
            
            Map<String, Object> result = jdbcCall.execute();
            @SuppressWarnings("unchecked")
            var bookings = (java.util.List<Map<String, Object>>) result.get("bookings");
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("bookings", bookings);
            response.put("total_returned", bookings != null ? bookings.size() : 0);
            response.put("data_source", "MySQL stored procedure: GetAllBookings");
            
            return response;
            
        } catch (Exception e) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", false);
            response.put("error", "Error calling stored procedure GetAllBookings: " + e.getMessage());
            return response;
        }
    }

    public List<Map<String, Object>> getBookingsByGuestId(int guestId) {
        List<Map<String, Object>> bookings = new ArrayList<>();

        try (var conn = jdbcTemplate.getDataSource().getConnection();
             var stmt = conn.prepareCall("{CALL GetBookingsByGuestId(?)}")) {

            stmt.setInt(1, guestId);

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("booking_id", rs.getInt("booking_id"));
                    booking.put("property_id", rs.getInt("property_id"));
                    booking.put("guest_id", rs.getInt("guest_id"));
                    booking.put("booking_start", rs.getDate("Booking_start").toLocalDate());
                    booking.put("booking_end", rs.getDate("Booking_end").toLocalDate());
                    booking.put("booking_price", rs.getBigDecimal("booking_price"));
                    bookings.add(booking);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException("Error calling stored procedure GetBookingsByGuestId", e);
        }

        return bookings;
    }

    public List<Map<String, Object>> getBookingsByPropertyId(int propertyId) {
        List<Map<String, Object>> bookings = new ArrayList<>();

        try (var conn = jdbcTemplate.getDataSource().getConnection();
             var stmt = conn.prepareCall("{CALL GetBookingsByPropertyId(?)}")) {

            stmt.setInt(1, propertyId);

            try (var rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> booking = new HashMap<>();
                    booking.put("booking_id", rs.getInt("ID"));
                    booking.put("property_id", rs.getInt("property_id"));
                    booking.put("guest_id", rs.getInt("guest_id"));
                    booking.put("booking_start", rs.getDate("Booking_start").toLocalDate());
                    booking.put("booking_end", rs.getDate("Booking_end").toLocalDate());
                    booking.put("booking_price", rs.getBigDecimal("Price"));
                    bookings.add(booking);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error calling stored procedure GetBookingsByPropertyId", e);
        }

        return bookings;
    }



} 
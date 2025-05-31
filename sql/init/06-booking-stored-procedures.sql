DELIMITER //

-- Get guest information from booking ID (for cross-database data transformation)
CREATE PROCEDURE GetGuestInfoFromBooking(
    IN p_booking_id INT
)
BEGIN
    SELECT 
        b.ID as booking_id,
        b.Property_ID as property_id,
        b.Guest_ID as guest_id,
        u.Name as guest_name,
        u.Email as guest_email,
        b.Booking_start,
        b.Booking_end,
        b.Price as booking_price,
        CASE 
            WHEN b.Booking_end < CURDATE() THEN 'completed'
            WHEN b.Booking_start <= CURDATE() AND b.Booking_end >= CURDATE() THEN 'active'
            ELSE 'upcoming'
        END as booking_status
    FROM Bookings b
    JOIN Users u ON b.Guest_ID = u.ID
    WHERE b.ID = p_booking_id;
END //

-- Validate that a booking exists and matches the property
CREATE PROCEDURE ValidateBookingExists(
    IN p_booking_id INT,
    IN p_property_id INT,
    OUT p_exists BOOLEAN
)
BEGIN
    DECLARE booking_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO booking_count
    FROM Bookings 
    WHERE ID = p_booking_id AND Property_ID = p_property_id;
    
    SET p_exists = (booking_count > 0);
END //

-- Check if booking is completed (end date has passed)
CREATE PROCEDURE IsBookingCompleted(
    IN p_booking_id INT,
    OUT p_completed BOOLEAN
)
BEGIN
    DECLARE booking_count INT DEFAULT 0;
    
    SELECT COUNT(*) INTO booking_count
    FROM Bookings 
    WHERE ID = p_booking_id AND Booking_end < CURDATE();
    
    SET p_completed = (booking_count > 0);
END //

-- Get all bookings for demonstration purposes  
CREATE PROCEDURE GetAllBookings()
BEGIN
    SELECT 
        b.ID as booking_id,
        b.Property_ID as property_id,
        b.Guest_ID as guest_id,
        u.Name as guest_name,
        b.Booking_start,
        b.Booking_end,
        b.Price as booking_price,
        CASE 
            WHEN b.Booking_end < CURDATE() THEN 'completed'
            WHEN b.Booking_start <= CURDATE() AND b.Booking_end >= CURDATE() THEN 'active'
            ELSE 'upcoming'
        END as booking_status
    FROM Bookings b
    JOIN Users u ON b.Guest_ID = u.ID
    ORDER BY b.Booking_end DESC
    LIMIT 20;
END //

DELIMITER ; 
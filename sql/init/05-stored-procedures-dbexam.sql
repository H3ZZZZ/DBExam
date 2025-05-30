DELIMITER //

CREATE PROCEDURE AddUser(
    IN p_name VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_mobile VARCHAR(20)
)
BEGIN
    INSERT INTO Users (Name, Email, Mobile)
    VALUES (p_name, p_email, p_mobile);
END //


CREATE PROCEDURE AddProperty(
    IN p_host_id INT,
    IN p_price DECIMAL(10,2),
    IN p_room_type VARCHAR(100),
    IN p_person_capacity INT,
    IN p_bedrooms INT,
    IN p_center_distance DECIMAL(5,2),
    IN p_metro_distance DECIMAL(5,2),
    IN p_city VARCHAR(100)
)
BEGIN
    INSERT INTO Properties (
        Host_ID, Price, Room_type, Person_capacity, Bedrooms,
        Center_distance, Metro_distance, City
    )
    VALUES (
        p_host_id, p_price, p_room_type, p_person_capacity,
        p_bedrooms, p_center_distance, p_metro_distance, p_city
    );
END //



CREATE PROCEDURE AddBooking(
    IN p_property_id INT,
    IN p_guest_id INT,
    IN p_booking_start DATE,
    IN p_booking_end DATE
)
BEGIN
    DECLARE nights INT;
    DECLARE price_per_night DECIMAL(10,2);
    DECLARE total_price DECIMAL(10,2);

    -- Calculate number of nights
    SET nights = DATEDIFF(p_booking_end, p_booking_start);

    -- Get nightly price from Properties
    SELECT Price INTO price_per_night
    FROM Properties
    WHERE ID = p_property_id;

    -- Calculate total price
    SET total_price = nights * price_per_night;

    -- Insert booking
    INSERT INTO Bookings (
        Property_ID, Guest_ID, Price,
        Booking_start, Booking_end
    )
    VALUES (
        p_property_id, p_guest_id, total_price,
        p_booking_start, p_booking_end
    );
END //

DELIMITER ;
-- CALL AddUser('Alice Johnson', 'alice@example.com', '123-456-7890');
-- CALL AddProperty(3,150.00,'Entire apartment',4,2,1.25,0.75,'Copenhagen');
-- CALL AddBooking(101, 3, '2025-06-01', '2025-06-05');





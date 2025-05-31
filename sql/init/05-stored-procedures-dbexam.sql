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

CREATE PROCEDURE GetUser(
    IN p_email VARCHAR(255)
)
BEGIN
    SELECT * FROM Users
    WHERE p_email = Email;
END //

CREATE PROCEDURE UpdateUser(
    IN p_user_id INT,
    IN p_name VARCHAR(255),
    IN p_email VARCHAR(255),
    IN p_mobile VARCHAR(20)
)
BEGIN
    UPDATE Users
    SET Name = COALESCE(p_name, Name),
        Email = COALESCE(p_email, Email),
        Mobile = COALESCE(p_mobile, Mobile)
    WHERE ID = p_user_id;
END //

CREATE PROCEDURE DeleteUser(
    IN p_user_id INT
)
BEGIN
    DELETE FROM Users WHERE ID = p_user_id;
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

CREATE PROCEDURE GetProperty(
    IN p_property_id INT
)
BEGIN
    SELECT * FROM Properties WHERE ID = p_property_id;
END //


CREATE PROCEDURE UpdateProperty(
    IN p_property_id INT,
    IN p_price DECIMAL(10,2),
    IN p_room_type VARCHAR(100),
    IN p_person_capacity INT,
    IN p_bedrooms INT,
    IN p_center_distance DECIMAL(5,2),
    IN p_metro_distance DECIMAL(5,2),
    IN p_city VARCHAR(100)
)
BEGIN
UPDATE Properties
SET Price = COALESCE(p_price, Price),
    Room_type = COALESCE(p_room_type, Room_type),
    Person_capacity = COALESCE(p_person_capacity, Person_capacity),
    Bedrooms = COALESCE(p_bedrooms, Bedrooms),
    Center_distance = COALESCE(p_center_distance, Center_distance),
    Metro_distance = COALESCE(p_metro_distance, Metro_distance),
    City = COALESCE(p_city, City)
WHERE ID = p_property_id;
END //

CREATE PROCEDURE DeleteProperty(
    IN p_property_id INT
)
BEGIN
DELETE FROM Properties WHERE ID = p_property_id;
END //


    CREATE PROCEDURE GetBooking(
    IN p_booking_id INT)

    BEGIN
    SELECT * FROM Bookings WHERE ID = p_booking_id;
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

CREATE PROCEDURE UpdateBooking(
    IN p_booking_id INT,
    IN p_property_id INT,
    IN p_guest_id INT,
    IN p_booking_start DATE,
    IN p_booking_end DATE
)
BEGIN
    DECLARE nights INT;
    DECLARE effective_property_id INT;
    DECLARE price_per_night DECIMAL(10,2);
    DECLARE total_price DECIMAL(10,2);

    -- Determine which property ID to use (new or existing)
SELECT COALESCE(p_property_id, Property_ID)
INTO effective_property_id
FROM Bookings
WHERE ID = p_booking_id;

-- Calculate number of nights
SET nights = DATEDIFF(COALESCE(p_booking_end, (
        SELECT Booking_end FROM Bookings WHERE ID = p_booking_id
    )), COALESCE(p_booking_start, (
        SELECT Booking_start FROM Bookings WHERE ID = p_booking_id
    )));

    -- Get nightly price from that property
SELECT Price INTO price_per_night
FROM Properties
WHERE ID = effective_property_id;

-- Calculate total price
SET total_price = nights * price_per_night;

    -- Perform update
UPDATE Bookings
SET Property_ID = COALESCE(p_property_id, Property_ID),
    Guest_ID = COALESCE(p_guest_id, Guest_ID),
    Price = total_price,
    Booking_start = COALESCE(p_booking_start, Booking_start),
    Booking_end = COALESCE(p_booking_end, Booking_end)
WHERE ID = p_booking_id;
END //



CREATE PROCEDURE DeleteBooking(
    IN p_booking_id INT
)
BEGIN
DELETE FROM Bookings WHERE ID = p_booking_id;
END //




DELIMITER ;
-- CALL AddUser('Alice Johnson', 'alice@example.com', '123-456-7890');
-- CALL GetUser('host1@email.com');
-- CALL UpdateUser(8,null,'updated@mail.com',null)
-- CALL DeleteUser(8)

-- CALL AddProperty(3,150.00,'Entire apartment',4,2,1.25,0.75,'Copenhagen');
-- CALL GetProperty(1)
-- CALL UpdateProperty(1, null, null, 4, null, null, null, null)
-- CALL DeleteProperty(3);

-- CALL AddBooking(101, 3, '2025-06-01', '2025-06-05');
-- CALL GetBooking(1);
-- CALL UpdateBooking(1, NULL, 5, '2024-10-30', '2024-11-07');
-- CALL DeleteBooking(1);





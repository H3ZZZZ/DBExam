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


CREATE PROCEDURE GetUserById(IN p_id INT)
BEGIN
SELECT ID, Name, Email, Mobile
FROM Users
WHERE ID = p_id;
END //

CREATE PROCEDURE GetAllUsers()
BEGIN
SELECT ID, Name, Email, Mobile FROM Users;
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
    DECLARE host_exists INT;

    START TRANSACTION;

    SELECT COUNT(*) INTO host_exists FROM Users WHERE ID = p_host_id;

    IF host_exists = 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No host found with that ID';
    END IF;

    INSERT INTO Properties (
        Host_ID, Price, Room_type, Person_capacity, Bedrooms,
        Center_distance, Metro_distance, City
    )
    VALUES (
        p_host_id, p_price, p_room_type, p_person_capacity,
        p_bedrooms, p_center_distance, p_metro_distance, p_city
    );
    COMMIT;
END //

CREATE PROCEDURE GetProperty(
    IN p_property_id INT
)

BEGIN
    SELECT * FROM Properties WHERE ID = p_property_id;
END //

CREATE PROCEDURE GetAllProperties()
BEGIN
SELECT ID, Host_ID, Price, Room_type, Person_capacity, Bedrooms,
       Center_distance, Metro_distance, City
FROM Properties;
END //

CREATE PROCEDURE GetPropertiesByHostId(IN p_host_id INT)
BEGIN
SELECT * FROM Properties WHERE Host_ID = p_host_id;
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
    DECLARE property_exists INT;

    START TRANSACTION;

    -- Check if the property exists
    SELECT COUNT(*) INTO property_exists FROM Properties WHERE ID = p_property_id;

    IF property_exists = 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No Property found with that ID';
    END IF;

    UPDATE Properties
    SET Price = COALESCE(p_price, Price),
        Room_type = COALESCE(p_room_type, Room_type),
        Person_capacity = COALESCE(p_person_capacity, Person_capacity),
        Bedrooms = COALESCE(p_bedrooms, Bedrooms),
        Center_distance = COALESCE(p_center_distance, Center_distance),
        Metro_distance = COALESCE(p_metro_distance, Metro_distance),
        City = COALESCE(p_city, City)
    WHERE ID = p_property_id;

    COMMIT;
END //

CREATE PROCEDURE GetFilteredProperties(
    IN p_city VARCHAR(50),
    IN p_price INT,
    IN p_capacity INT,
    IN p_city_dist FLOAT,
    in p_metro_dist FLOAT
)

BEGIN
	SELECT *
	FROM Properties
	WHERE City = p_city AND Price <= p_price AND Person_capacity >= p_capacity AND Center_distance <= p_city_dist AND Metro_distance <= p_metro_dist;
END //

CREATE PROCEDURE DeleteProperty(
    IN p_property_id INT
)

BEGIN
    DELETE FROM Properties WHERE ID = p_property_id;
END //

CREATE PROCEDURE GetBooking(
    IN p_booking_id INT
)

BEGIN
    SELECT * FROM Bookings WHERE ID = p_booking_id;
END //

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

CREATE PROCEDURE GetBookingsByPropertyId(IN p_property_id INT)
BEGIN
SELECT * FROM Bookings WHERE Property_ID = p_property_id;
END //

CREATE PROCEDURE GetBookingsByGuestId(IN p_guest_id INT)
BEGIN
SELECT
    ID AS booking_id,
    Property_ID AS property_id,
    Guest_ID AS guest_id,
    Booking_start,
    Booking_end,
    Price AS booking_price
FROM Bookings
WHERE Guest_ID = p_guest_id;
END //

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

-- Added from 06-booking-stored-procedures.sql
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

    START TRANSACTION;

    IF p_booking_start IS NULL OR p_booking_end IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start and end date must be provided';
    END IF;

    -- Calculate number of nights
    SET nights = DATEDIFF(p_booking_end, p_booking_start);
    IF nights <= 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'End date must be after start date';
    END IF;

    -- Get nightly price from Properties
    SELECT Price INTO price_per_night
    FROM Properties
    WHERE ID = p_property_id;

    -- If a property ID is not found price will be null and we rollback
    -- We can also provide a better error message this way
    IF price_per_night IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'No Property found with that ID';
    END IF;

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
    COMMIT;
END //

CREATE PROCEDURE UpdateBooking(
    IN p_booking_id INT,
    IN p_booking_start DATE,
    IN p_booking_end DATE
)

BEGIN
    DECLARE nights INT;
    DECLARE price_per_night DECIMAL(10,2);
    DECLARE total_price DECIMAL(10,2);
    DECLARE v_property_id INT;
    DECLARE booking_exists INT;

    START TRANSACTION;

    IF p_booking_start IS NULL OR p_booking_end IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Start and end date must be provided';
    END IF;

    SET nights = DATEDIFF(p_booking_end, p_booking_start);
    IF nights <= 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'End date must be after start date';
    END IF;

    SELECT COUNT(*) INTO booking_exists FROM Bookings WHERE ID = p_booking_id;
    IF booking_exists = 0 THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking not found';
    END IF;

    SELECT Property_ID INTO v_property_id
    FROM Bookings
    WHERE ID = p_booking_id;

    IF v_property_id IS NULL THEN
        ROLLBACK;
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Booking exists but Property_ID is NULL';
    END IF;

    SELECT Price INTO price_per_night
    FROM Properties
    WHERE ID = v_property_id;

    SET total_price = nights * price_per_night;

    UPDATE Bookings
    SET Booking_start = p_booking_start,
        Booking_end = p_booking_end,
        Price = total_price
    WHERE ID = p_booking_id;

    COMMIT;

END //

CREATE PROCEDURE DeleteBooking(
    IN p_booking_id INT
)

BEGIN
    DELETE FROM Bookings WHERE ID = p_booking_id;
END //

DELIMITER ;
-- Users procedures
-- CALL AddUser('Alice Johnson', 'alice@example.com', '123-456-7890');
-- CALL GetUser('host1@email.com');
-- CALL UpdateUser(8,null,'updated@mail.com',null);
-- CALL DeleteUser(8);

-- Properties procedures
-- CALL AddProperty(3,150.00,'Entire apartment',4,2,1.25,0.75,'Copenhagen');
-- CALL GetProperty(1);
-- CALL GetFilteredProperties('Amsterdam', 350, 3, 3.2, 2.8);
-- CALL UpdateProperty(1, null, null, 4, null, null, null, null);
-- CALL DeleteProperty(3);

-- Bookings procedures
-- CALL AddBooking(101, 3, '2025-06-01', '2025-06-05');
-- CALL GetBooking(1);
-- CALL UpdateBooking(1,'2024-10-30','2024-11-07');
-- CALL DeleteBooking(1);





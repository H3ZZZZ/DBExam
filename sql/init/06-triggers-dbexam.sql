DELIMITER //

CREATE TRIGGER prevent_overlapping_booking
BEFORE INSERT ON Bookings
FOR EACH ROW
BEGIN
    DECLARE overlap_count INT;

    SELECT COUNT(*) INTO overlap_count
    FROM Bookings
    WHERE Property_id = NEW.Property_id
      AND NOT (
          NEW.Booking_end <= Booking_start OR
          NEW.Booking_start >= Booking_end
      );

    IF overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
            SET MESSAGE_TEXT = 'Booking dates overlap with an existing booking.';
    END IF;
END;
//

DELIMITER ;

-- Create database schema for Airbnb analytics - English version
USE airbnb_analytics;

-- Users table
CREATE TABLE Users (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    Name VARCHAR(255) NOT NULL,
    Email VARCHAR(255) UNIQUE NOT NULL,
    Mobile VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (Email)
);

-- Properties (Listings) table
CREATE TABLE Properties (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    Host_ID INT NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    Room_type VARCHAR(100) NOT NULL,
    Person_capacity INT NOT NULL,
    Bedrooms INT,
    Center_distance DECIMAL(5,2),
    Metro_distance DECIMAL(5,2),
    City VARCHAR(100) NOT NULL,
    Room_shared BOOLEAN DEFAULT FALSE,
    Room_private BOOLEAN DEFAULT FALSE,
    Is_super_host BOOLEAN DEFAULT FALSE,
    Is_weekend BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (Host_ID) REFERENCES Users(ID) ON DELETE CASCADE,
    INDEX idx_price (Price),
    INDEX idx_room_type (Room_type),
    INDEX idx_city (City),
    INDEX idx_host (Host_ID),
    INDEX idx_super_host (Is_super_host),
    INDEX idx_weekend (Is_weekend)
);

-- Bookings table
CREATE TABLE Bookings (
    ID INT PRIMARY KEY AUTO_INCREMENT,
    Property_ID INT NOT NULL,
    Guest_ID INT NOT NULL,
    Price DECIMAL(10,2) NOT NULL,
    Booking_period VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (Property_ID) REFERENCES Properties(ID) ON DELETE CASCADE,
    FOREIGN KEY (Guest_ID) REFERENCES Users(ID) ON DELETE CASCADE,
    INDEX idx_property (Property_ID),
    INDEX idx_guest (Guest_ID),
    INDEX idx_period (Booking_period)
);

-- Create useful views for analytics
CREATE VIEW Property_Summary AS
SELECT 
    p.ID as property_id,
    p.Price,
    p.Room_type,
    p.Person_capacity,
    p.Bedrooms,
    p.City,
    p.Center_distance,
    p.Metro_distance,
    p.Room_shared,
    p.Room_private,
    p.Is_super_host,
    p.Is_weekend,
    u.Name as host_name,
    u.Email as host_email,
    COUNT(b.ID) as total_bookings
FROM Properties p
LEFT JOIN Users u ON p.Host_ID = u.ID
LEFT JOIN Bookings b ON p.ID = b.Property_ID
GROUP BY p.ID, p.Price, p.Room_type, p.Person_capacity, p.Bedrooms, p.City, p.Center_distance, p.Metro_distance, p.Room_shared, p.Room_private, p.Is_super_host, p.Is_weekend, u.Name, u.Email;

CREATE VIEW City_Analytics AS
SELECT 
    City,
    COUNT(*) as total_properties,
    AVG(Price) as avg_price,
    MIN(Price) as min_price,
    MAX(Price) as max_price,
    AVG(Center_distance) as avg_center_distance,
    AVG(Metro_distance) as avg_metro_distance,
    COUNT(CASE WHEN Is_super_host = TRUE THEN 1 END) as super_host_count,
    COUNT(CASE WHEN Room_shared = TRUE THEN 1 END) as shared_rooms,
    COUNT(CASE WHEN Room_private = TRUE THEN 1 END) as private_rooms
FROM Properties 
GROUP BY City;

CREATE VIEW Booking_Overview AS
SELECT 
    b.ID as booking_id,
    b.Property_ID,
    b.Guest_ID,
    b.Price as booking_price,
    b.Booking_period,
    p.Room_type,
    p.City,
    p.Is_super_host,
    guest.Name as guest_name,
    guest.Email as guest_email,
    host.Name as host_name,
    host.Email as host_email
FROM Bookings b
JOIN Properties p ON b.Property_ID = p.ID
JOIN Users guest ON b.Guest_ID = guest.ID
JOIN Users host ON p.Host_ID = host.ID;

-- Insert sample data for testing

-- Sample users (both renters and hosts)
INSERT INTO Users (Name, Email, Mobile) VALUES
('John Smith', 'john.smith@email.com', '+44 123 456 789'),
('Maria Garcia', 'maria.garcia@email.com', '+34 987 654 321'),
('Peter Johnson', 'peter.johnson@email.com', '+1 555 123 4567'),
('Anna Williams', 'anna.williams@email.com', '+49 162 345 6789'),
('Thomas Brown', 'thomas.brown@email.com', '+33 6 12 34 56 78'),
('Julie Davis', 'julie.davis@email.com', '+39 320 123 4567'),
('Michael Wilson', 'michael.wilson@email.com', '+31 6 12345678');

-- Sample properties
INSERT INTO Properties (Host_ID, Price, Room_type, Person_capacity, Bedrooms, Center_distance, Metro_distance, City, Room_shared, Room_private, Is_super_host, Is_weekend) VALUES
(1, 120.50, 'Entire home/apt', 4, 2, 5.2, 0.8, 'London', FALSE, FALSE, TRUE, FALSE),
(2, 85.00, 'Private room', 2, 1, 3.1, 1.2, 'Berlin', FALSE, TRUE, FALSE, FALSE),
(3, 200.00, 'Entire home/apt', 6, 3, 8.5, 0.5, 'Paris', FALSE, FALSE, TRUE, TRUE),
(1, 65.75, 'Private room', 2, 1, 12.3, 2.1, 'Amsterdam', FALSE, TRUE, TRUE, FALSE),
(4, 150.00, 'Entire home/apt', 4, 2, 4.7, 1.0, 'Barcelona', FALSE, FALSE, FALSE, TRUE),
(2, 55.50, 'Shared room', 1, 0, 5.5, 0.9, 'London', TRUE, FALSE, FALSE, FALSE),
(5, 110.00, 'Private room', 3, 1, 6.2, 1.5, 'Berlin', FALSE, TRUE, TRUE, FALSE);

-- Sample bookings
INSERT INTO Bookings (Property_ID, Guest_ID, Price, Booking_period) VALUES
(1, 6, 120.50, '2024-01-15 to 2024-01-17'),
(2, 7, 85.00, '2024-01-20 to 2024-01-22'),
(3, 6, 200.00, '2024-02-01 to 2024-02-03'),
(4, 7, 65.75, '2024-02-10 to 2024-02-12'),
(5, 6, 150.00, '2024-02-15 to 2024-02-17'),
(1, 7, 120.50, '2024-03-01 to 2024-03-03'),
(2, 6, 85.00, '2024-03-15 to 2024-03-17'),
(6, 7, 55.50, '2024-03-20 to 2024-03-22');

-- Grant permissions to airbnb_user
GRANT ALL PRIVILEGES ON airbnb_analytics.* TO 'airbnb_user'@'%';
FLUSH PRIVILEGES; 
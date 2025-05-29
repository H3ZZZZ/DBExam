-- Create database schema for Airbnb analytics - simplified version
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
                            ID INT PRIMARY KEY,
                            Host_ID INT NOT NULL,
                            Price DECIMAL(10,2) NOT NULL,
                            Room_type VARCHAR(100) NOT NULL,
                            Person_capacity INT NOT NULL,
                            Bedrooms INT,
                            Center_distance DECIMAL(5,2),
                            Metro_distance DECIMAL(5,2),
                            City VARCHAR(100) NOT NULL,
                            created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                            updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                            FOREIGN KEY (Host_ID) REFERENCES Users(ID) ON DELETE CASCADE,
                            INDEX idx_price (Price),
                            INDEX idx_city (City),
                            INDEX idx_host (Host_ID)
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
                          FOREIGN KEY (Guest_ID) REFERENCES Users(ID) ON DELETE CASCADE
);

-- Grant permissions to airbnb_user
GRANT ALL PRIVILEGES ON airbnb_analytics.* TO 'airbnb_user'@'%';
FLUSH PRIVILEGES;

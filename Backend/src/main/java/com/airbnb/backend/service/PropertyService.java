package com.airbnb.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;

@Service
public class PropertyService {

    @Autowired
    private DataSource dataSource;

    public void addProperty(int hostId, double price, String roomType, int personCapacity,
                            int bedrooms, double centerDistance, double metroDistance, String city) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL AddProperty(?, ?, ?, ?, ?, ?, ?, ?)}")) {

            stmt.setInt(1, hostId);
            stmt.setDouble(2, price);
            stmt.setString(3, roomType);
            stmt.setInt(4, personCapacity);
            stmt.setInt(5, bedrooms);
            stmt.setDouble(6, centerDistance);
            stmt.setDouble(7, metroDistance);
            stmt.setString(8, city);

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure AddProperty", e);
        }
    }
}
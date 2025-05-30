package com.airbnb.backend.service;

import com.airbnb.backend.dto.PropertyDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class PropertyService {

    @Autowired
    private DataSource dataSource;

    public void addProperty(Integer hostId, BigDecimal price, String roomType, Integer personCapacity,
                            Integer bedrooms, BigDecimal centerDistance, BigDecimal metroDistance, String city) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL AddProperty(?, ?, ?, ?, ?, ?, ?, ?)}")) {

            stmt.setInt(1, hostId);
            stmt.setBigDecimal(2, price);
            stmt.setString(3, roomType);
            stmt.setInt(4, personCapacity);
            stmt.setObject(5, bedrooms);
            stmt.setObject(6, centerDistance);
            stmt.setObject(7, metroDistance);
            stmt.setString(8, city);

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure AddProperty", e);
        }
    }

    public PropertyDTO getPropertyById(int propertyId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL GetProperty(?)}")) {

            stmt.setInt(1, propertyId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    PropertyDTO property = new PropertyDTO();
                    property.setId(rs.getInt("ID"));
                    property.setHostId(rs.getInt("Host_ID"));
                    property.setPrice(rs.getBigDecimal("Price"));
                    property.setRoomType(rs.getString("Room_type"));
                    property.setPersonCapacity(rs.getInt("Person_capacity"));
                    property.setBedrooms(rs.getObject("Bedrooms", Integer.class));
                    property.setCenterDistance(rs.getObject("Center_distance", BigDecimal.class));
                    property.setMetroDistance(rs.getObject("Metro_distance", BigDecimal.class));
                    property.setCity(rs.getString("City"));
                    return property;
                } else {
                    throw new RuntimeException("Property not found");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure GetProperty", e);
        }
    }

    public void updateProperty(int propertyId, PropertyDTO property) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL UpdateProperty(?, ?, ?, ?, ?, ?, ?, ?)}")) {

            stmt.setInt(1, propertyId);
            stmt.setObject(2, property.getPrice());
            stmt.setString(3, property.getRoomType());
            stmt.setObject(4, property.getPersonCapacity());
            stmt.setObject(5, property.getBedrooms());
            stmt.setObject(6, property.getCenterDistance());
            stmt.setObject(7, property.getMetroDistance());
            stmt.setString(8, property.getCity());

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure UpdateProperty", e);
        }
    }

}

package com.airbnb.backend.service;

import com.airbnb.backend.dto.UserDTO;
import com.airbnb.backend.dto.UserUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

@Service
public class UserService {

    @Autowired
    private DataSource dataSource;

    public void addUser(String name, String email, String mobile) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL AddUser(?, ?, ?)}")) {

            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setString(3, mobile);

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure AddUser", e);
        }
    }

    public UserDTO getUserByEmail(String email) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL GetUser(?)}")) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setId(rs.getInt("ID"));
                    user.setName(rs.getString("Name"));
                    user.setEmail(rs.getString("Email"));
                    user.setMobile(rs.getString("Mobile"));
                    return user;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure GetUser", e);
        }
    }

    public void updateUser(int userId, UserUpdateDTO user) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL UpdateUser(?, ?, ?, ?)}")) {

            stmt.setInt(1, userId);
            stmt.setString(2, user.getName());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getMobile());

            stmt.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure UpdateUser", e);
        }
    }


    public void deleteUser(int userId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL DeleteUser(?)}")) {

            stmt.setInt(1, userId);
            stmt.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure DeleteUser", e);
        }
    }

    public UserDTO getUserById(int userId) {
        try (Connection conn = dataSource.getConnection();
             CallableStatement stmt = conn.prepareCall("{CALL GetUserById(?)}")) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UserDTO user = new UserDTO();
                    user.setId(rs.getInt("ID"));
                    user.setName(rs.getString("Name"));
                    user.setEmail(rs.getString("Email"));
                    user.setMobile(rs.getString("Mobile"));
                    return user;
                } else {
                    return null;
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error calling stored procedure GetUserById", e);
        }
    }




}

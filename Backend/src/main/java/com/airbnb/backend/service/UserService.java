package com.airbnb.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;
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
}

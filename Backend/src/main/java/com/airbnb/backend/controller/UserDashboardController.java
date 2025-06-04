package com.airbnb.backend.controller;

import com.airbnb.backend.service.UserDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/user")
public class UserDashboardController {

    @Autowired
    private UserDashboardService userDashboardService;

    @GetMapping("/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable int userId) {
        return ResponseEntity.ok(userDashboardService.getUserDashboard(userId));
    }
}

package com.airbnb.backend.controller;

import com.airbnb.backend.service.HostDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard/host")
public class HostDashboardController {

    @Autowired
    private HostDashboardService hostDashboardService;

    @GetMapping("/{hostId}")
    public ResponseEntity<Map<String, Object>> getHostDashboard(@PathVariable int hostId) {
        return ResponseEntity.ok(hostDashboardService.getHostDashboard(hostId));
    }
}

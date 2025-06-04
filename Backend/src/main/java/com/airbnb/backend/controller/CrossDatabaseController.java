package com.airbnb.backend.controller;

import com.airbnb.backend.dto.PropertyDetailsDTO;
import com.airbnb.backend.service.HostDashboardService;
import com.airbnb.backend.service.PropertyDetailsService;
import com.airbnb.backend.service.UserDashboardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/crossdatabase")
@Tag(name = "Cross Database Controller", description = "API for accessing cross-database information")
public class CrossDatabaseController {

    @Autowired
    private PropertyDetailsService propertyDetailsService;

    @Autowired
    private UserDashboardService userDashboardService;

    @Autowired
    private HostDashboardService hostDashboardService;

    @GetMapping("/property-info/{propertyId}")
    public ResponseEntity<PropertyDetailsDTO> getFullPropertyInfo(@PathVariable int propertyId) {
        PropertyDetailsDTO result = propertyDetailsService.getCompletePropertyDetails(propertyId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/user-info/{userId}")
    public ResponseEntity<Map<String, Object>> getUserDashboard(@PathVariable int userId) {
        return ResponseEntity.ok(userDashboardService.getUserDashboard(userId));
    }

    @GetMapping("/host-info/{hostId}")
    public ResponseEntity<Map<String, Object>> getHostDashboard(@PathVariable int hostId) {
        return ResponseEntity.ok(hostDashboardService.getHostDashboard(hostId));
    }
}

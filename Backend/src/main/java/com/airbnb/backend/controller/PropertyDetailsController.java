package com.airbnb.backend.controller;

import com.airbnb.backend.dto.PropertyDetailsDTO;
import com.airbnb.backend.service.PropertyDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/property-details")
public class PropertyDetailsController {

    @Autowired
    private PropertyDetailsService propertyDetailsService;

    @GetMapping("/{propertyId}")
    public ResponseEntity<PropertyDetailsDTO> getFullPropertyInfo(@PathVariable int propertyId) {
        PropertyDetailsDTO result = propertyDetailsService.getCompletePropertyDetails(propertyId);
        return ResponseEntity.ok(result);
    }
}

package com.airbnb.backend.controller;

import com.airbnb.backend.dto.PropertyDTO;
import com.airbnb.backend.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<String> addProperty(@RequestBody PropertyDTO property) {
        propertyService.addProperty(
                property.getHostId(),
                property.getPrice(),
                property.getRoomType(),
                property.getPersonCapacity(),
                property.getBedrooms(),
                property.getCenterDistance(),
                property.getMetroDistance(),
                property.getCity()
        );
        return ResponseEntity.ok("Property added successfully");
    }
}
package com.airbnb.backend.controller;

import com.airbnb.backend.dto.PropertyCreateDTO;
import com.airbnb.backend.dto.PropertyDTO;
import com.airbnb.backend.dto.PropertyUpdateDTO;
import com.airbnb.backend.service.PropertyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @PostMapping
    public ResponseEntity<String> addProperty(@RequestBody PropertyCreateDTO property) {
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

    @GetMapping("/{id}")
    public ResponseEntity<PropertyDTO> getPropertyById(@PathVariable int id) {
        PropertyDTO property = propertyService.getPropertyById(id);
        return ResponseEntity.ok(property);
    }

    @GetMapping
    public ResponseEntity<List<PropertyDTO>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }


    @PutMapping("/{id}")
    public ResponseEntity<String> updateProperty(@PathVariable int id, @RequestBody PropertyUpdateDTO property) {
        propertyService.updateProperty(id, property);
        return ResponseEntity.ok("Property updated successfully");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProperty(@PathVariable int id) {
        propertyService.deleteProperty(id);
        return ResponseEntity.ok("Property deleted successfully");
    }

}
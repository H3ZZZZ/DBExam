package com.airbnb.backend.controller;

import com.airbnb.backend.dto.UserDTO;
import com.airbnb.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<String> addUser(@RequestBody UserDTO user) {
        userService.addUser(user.getName(), user.getEmail(), user.getMobile());
        return ResponseEntity.ok("User added successfully");
    }
}


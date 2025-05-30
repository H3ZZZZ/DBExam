package com.airbnb.backend.controller;

import com.airbnb.backend.dto.UserDTO;
import com.airbnb.backend.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/{email}")
    public ResponseEntity<?> getUserByEmail(@PathVariable String email) {
        UserDTO user = userService.getUserByEmail(email);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> updateUser(@PathVariable("id") int userId, @RequestBody UserDTO userDTO) {
        userService.updateUser(userId, userDTO.getName(), userDTO.getEmail(), userDTO.getMobile());
        return ResponseEntity.ok("User updated successfully");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return ResponseEntity.ok("User deleted successfully");
    }

}


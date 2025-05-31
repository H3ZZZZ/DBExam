package com.airbnb.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

public class UserDTO {
    private int id;
    private String name;
    private String email;
    private String mobile;


    // Constructors
    public UserDTO() {
    }

    public UserDTO(int id, String name, String email, String mobile) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobile() {
        return mobile;
    }

    public int getId() {
        return id;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public void setId(int id) {
        this.id = id;
    }
}

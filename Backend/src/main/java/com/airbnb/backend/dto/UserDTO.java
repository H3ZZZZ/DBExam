package com.airbnb.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDTO {
    private String name;
    private String email;
    private String mobile;

    // Constructors
    public UserDTO() {}

    public UserDTO(String name, String email, String mobile) {
        this.name = name;
        this.email = email;
        this.mobile = mobile;
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getMobile() { return mobile; }

    // Setters
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setMobile(String mobile) { this.mobile = mobile; }
}

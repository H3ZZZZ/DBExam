package com.airbnb.backend.dto;

public class UserUpdateDTO {
    private String name;
    private String email;
    private String mobile;

    public UserUpdateDTO() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }
}

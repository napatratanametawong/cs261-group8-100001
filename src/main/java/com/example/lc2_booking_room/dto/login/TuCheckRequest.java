package com.example.lc2_booking_room.dto.login;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class TuCheckRequest {
    @NotBlank
    private String userName;

    @NotBlank @Email
    private String email;

    public TuCheckRequest() {}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

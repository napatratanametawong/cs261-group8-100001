package com.example.lc2_booking_room.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RequestOtpDto {
    @NotBlank @Email
    private String email;

    public RequestOtpDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

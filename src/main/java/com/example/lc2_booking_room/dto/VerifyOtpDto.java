package com.example.lc2_booking_room.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyOtpDto {
    @NotBlank @Email
    private String email;

    @NotBlank
    private String otp;

    public VerifyOtpDto() {}

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getOtp() { return otp; }
    public void setOtp(String otp) { this.otp = otp; }
}


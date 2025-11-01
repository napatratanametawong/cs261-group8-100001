package com.example.lc2_booking_room.service.login;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class OtpService {
    private static final SecureRandom RNG = new SecureRandom();

    public String generateOtp(int digits) {
        int bound = (int) Math.pow(10, digits);
        int min = bound / 10;
        int code = RNG.nextInt(bound - min) + min;
        return String.valueOf(code);
    }
}
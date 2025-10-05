package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.RequestOtpDto;
import com.example.lc2_booking_room.dto.TokenResponse;
import com.example.lc2_booking_room.dto.VerifyOtpDto;
import com.example.lc2_booking_room.service.EmailService;
import com.example.lc2_booking_room.service.JwtService;
import com.example.lc2_booking_room.service.OtpService;
import com.example.lc2_booking_room.service.OtpStore;
import com.example.lc2_booking_room.service.TuDirectoryClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.time.Instant;
import java.util.Objects;
import java.util.regex.Pattern;

// ... package/import เดิม ...
@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final java.util.regex.Pattern DOME_EMAIL =
            java.util.regex.Pattern.compile("^[A-Za-z0-9._%+-]+@dome\\.tu\\.ac\\.th$", java.util.regex.Pattern.CASE_INSENSITIVE);

    private final EmailService emailService;
    private final OtpService otpService;
    private final OtpStore otpStore;
    private final JwtService jwtService;
    private final TuDirectoryClient tuDirectory;

    @Value("${app.otp.ttl-seconds}") private long otpTtl;
    @Value("${app.otp.request-cooldown}") private long cooldown;

    public AuthController(EmailService emailService, OtpService otpService, OtpStore otpStore,
                          JwtService jwtService, TuDirectoryClient tuDirectory) {
        this.emailService = emailService;
        this.otpService = otpService;
        this.otpStore = otpStore;
        this.jwtService = jwtService;
        this.tuDirectory = tuDirectory;
    }

    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@Valid @RequestBody RequestOtpDto req) {
        String userName = req.getUserName().trim();
        String emailInput = req.getEmail().trim();

        if (!DOME_EMAIL.matcher(emailInput).matches()) {
            return ResponseEntity.badRequest().body("อีเมลต้องเป็นโดเมน @dome.tu.ac.th เท่านั้น");
        }

        long now = java.time.Instant.now().getEpochSecond();
        if (!otpStore.canRequest(emailInput.toLowerCase(), now, cooldown)) {
            return ResponseEntity.status(429).body("ขอ OTP ถี่เกินไป กรุณารอสักครู่");
        }

        // 1) ดึงอีเมลทั้งหมดจาก TU
        java.util.Set<String> tuEmails = tuDirectory.findStudentEmails(userName);

        // 2) นอร์มัลไลซ์อีเมลทั้งสองฝั่ง (lower/trim) + rule สำหรับ dome (ไม่สนจุดใน local-part)
        String normalizedInput = normalizeEmail(emailInput);
        boolean match = tuEmails.stream()
                .filter(java.util.Objects::nonNull)
                .map(this::normalizeEmail)
                .anyMatch(e -> e.equals(normalizedInput));

        if (!match) {
            // ช่วยดีบัก: แสดงรายการอีเมลจาก TU (แนะนำเปิดเฉพาะ dev)
            // return ResponseEntity.badRequest().body("ไม่พบความสัมพันธ์... TU emails=" + tuEmails);
            return ResponseEntity.badRequest().body("ไม่พบความสัมพันธ์ระหว่างรหัสนักศึกษาและอีเมลมหาวิทยาลัย");
        }

        String otp = otpService.generateOtp(6);
        otpStore.save(emailInput.toLowerCase(), otp, otpTtl);
        emailService.sendOtp(emailInput, otp);
        return ResponseEntity.ok("ส่ง OTP ไปที่อีเมลแล้ว (อายุ " + (otpTtl / 60) + " นาที)");
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDto req) {
        String email = req.getEmail().trim().toLowerCase();
        boolean ok = otpStore.verifyAndConsume(email, req.getOtp());
        if (!ok) return ResponseEntity.badRequest().body("OTP ไม่ถูกต้องหรือหมดอายุ");
        String token = jwtService.issueToken(email, "UNKNOWN");
        return ResponseEntity.ok(new TokenResponse(token));
    }

    private String normalizeEmail(String raw) {
        String e = raw.trim().toLowerCase();
        int at = e.indexOf('@');
        if (at < 0) return e;
        String local = e.substring(0, at);
        String domain = e.substring(at + 1);
        // กฎพิเศษ: dome.tu.ac.th ไม่สน '.' ใน local-part (กันเคส tatchakritsta vs tatchakrit.sta)
        if (domain.equals("dome.tu.ac.th")) {
            local = local.replace(".", "");
        }
        return local + "@" + domain;
    }
}


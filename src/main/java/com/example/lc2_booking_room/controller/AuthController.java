package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.*;
import com.example.lc2_booking_room.service.*;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Pattern DOME_EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@dome\\.tu\\.ac\\.th$",
            Pattern.CASE_INSENSITIVE);

    private final EmailService emailService;
    private final OtpService otpService;
    private final OtpStore otpStore;
    private final JwtService jwtService;
    private final TuDirectoryClient tuDirectory;

    @Value("${app.otp.ttl-seconds}")
    private long otpTtl;

    @Value("${app.otp.request-cooldown}")
    private long cooldown;

    @Value("${app.admin.emails:}")
    private String adminEmailsCsv;

    public AuthController(EmailService emailService,
            OtpService otpService,
            OtpStore otpStore,
            JwtService jwtService,
            TuDirectoryClient tuDirectory) {
        this.emailService = emailService;
        this.otpService = otpService;
        this.otpStore = otpStore;
        this.jwtService = jwtService;
        this.tuDirectory = tuDirectory;
    }

    /** ขั้นที่ 1: ตรวจ userName ↔ email (นักศึกษาเช็คกับ TU; แอดมินข้าม) */
    @PostMapping("/tucheck")
    public ResponseEntity<?> tuCheck(@Valid @RequestBody TuCheckDto req) {
        String userName = req.getUserName().trim();
        String email = req.getEmail().trim().toLowerCase();

        // ✅ ถ้าเป็นอีเมล admin ข้าม TU check
        if (isAdminEmail(email)) {
            otpStore.markTuCheckPassed(email);
            otpStore.setUsernameFor(email, userName);
            return ResponseEntity.ok("ผ่านการตรวจสอบแล้ว (admin)");
        }

        // ✅ สำหรับนักศึกษา ต้องเช็คกับ TU API
        Set<String> tuEmails = tuDirectory.findStudentEmails(userName);
        if (tuEmails.isEmpty()) {
            return ResponseEntity.badRequest().body("ไม่พบข้อมูลจาก TU API");
        }

        String target = normalizeEmail(email);
        boolean matched = tuEmails.stream()
                .filter(Objects::nonNull)
                .map(this::normalizeEmail)
                .anyMatch(target::equals);

        if (!matched) {
            return ResponseEntity.badRequest().body("ไม่พบความสัมพันธ์ระหว่างรหัสนักศึกษาและอีเมล");
        }

        otpStore.markTuCheckPassed(email);
        otpStore.setUsernameFor(email, userName);

        return ResponseEntity.ok("ผ่านการตรวจสอบแล้ว");
    }

    /** ขั้นที่ 2: ขอ OTP */
    @PostMapping("/request-otp")
    public ResponseEntity<?> requestOtp(@Valid @RequestBody RequestOtpDto req) {
        String emailInput = req.getEmail().trim().toLowerCase();

        if (isAdminEmail(emailInput)) {
            otpStore.markTuCheckPassed(emailInput);
        }

        if (!otpStore.isTuCheckPassed(emailInput)) {
            if (!DOME_EMAIL.matcher(emailInput).matches()) {
                return ResponseEntity.badRequest().body("อีเมลต้องเป็นโดเมน @dome.tu.ac.th เท่านั้น");
            }
        }

        long now = Instant.now().getEpochSecond();
        if (!otpStore.canRequest(emailInput, now, cooldown)) {
            return ResponseEntity.status(429).body("ขอ OTP ถี่เกินไป กรุณารอสักครู่");
        }

        String otp = otpService.generateOtp(6);
        otpStore.save(emailInput, otp, otpTtl);
        emailService.sendOtp(emailInput, otp);

        return ResponseEntity.ok("ส่ง OTP ไปที่อีเมลแล้ว (อายุ " + (otpTtl / 60) + " นาที)");
    }

    /** ขั้นที่ 3: ยืนยัน OTP → ออก JWT + role + profile */
    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody VerifyOtpDto req) {
        String email = req.getEmail().trim().toLowerCase();

        boolean ok = otpStore.verifyAndConsume(email, req.getOtp());
        if (!ok) {
            return ResponseEntity.badRequest().body("OTP ไม่ถูกต้องหรือหมดอายุ");
        }

        String role = isAdminEmail(email) ? "BUILDING_ADMIN" : "USER";
        String username = otpStore.getUsernameFor(email);
        if (username == null)
            username = "";

        UserProfile profile = null;

        if ("USER".equals(role)) {
            // ✅ USER ต้องมี username (ได้จาก /auth/tucheck)
            if (username.isBlank()) {
                return ResponseEntity.badRequest()
                        .body("ไม่พบ username ที่ผูกไว้ กรุณาเรียก /auth/tucheck ก่อน แล้วจึงขอ/ยืนยัน OTP");
            }
            // ดึงโปรไฟล์จาก TU ด้วย "username"
            profile = tuDirectory.getStudentProfile(username);
            if (profile == null) {
                // กันเคส TU ล่ม/คีย์ไม่ตรง → ตอบแบบมีข้อมูลเท่าที่รู้
                profile = new UserProfile(username, null, email, null, null);
            } else if (profile.getEmail() == null || profile.getEmail().isBlank()) {
                profile.setEmail(email);
            }
        } else {
            // ✅ ADMIN: ใช้ username ที่กรอกไว้ตอน tucheck (ถ้าเว้นว่างได้ก็โอเค)
            profile = new UserProfile(
                    username,
                    null,
                    email,
                    "ฝ่ายดูแลอาคาร บร.2",
                    "คณะวิทยาศาสตร์และเทคโนโลยี");
        }

        String token = jwtService.issueToken(email, role, username);
        return ResponseEntity.ok(new TokenResponse(token, role, username, profile));
    }

    // ---------- Helpers ----------
    private boolean isAdminEmail(String email) {
        if (adminEmailsCsv == null || adminEmailsCsv.isBlank())
            return false;
        for (String s : adminEmailsCsv.split(",")) {
            if (email.equals(s.trim().toLowerCase()))
                return true;
        }
        return false;
    }

    private String normalizeEmail(String raw) {
        String e = raw == null ? "" : raw.trim().toLowerCase();
        int at = e.indexOf('@');
        if (at < 0)
            return e;
        String local = e.substring(0, at);
        String domain = e.substring(at + 1);
        if ("dome.tu.ac.th".equals(domain)) {
            local = local.replace(".", "");
        }
        return local + "@" + domain;
    }
}

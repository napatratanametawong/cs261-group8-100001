package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.PersonalBookingHistoryResponse;
import com.example.lc2_booking_room.service.login.JwtService;
import com.example.lc2_booking_room.service.reservation.PersonalBookingHistoryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/reservations")
public class PersonalHistoryController {

    private final JwtService jwtService;
    private final PersonalBookingHistoryService service;

    @GetMapping("/history")
    public Page<PersonalBookingHistoryResponse> myHistory(
            Authentication auth,
            @RequestParam(required = false) String roomCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = resolveEmail(auth);
        log.info("PersonalHistory email={}", email);

        // normalize optional filters so native SQL '... OR :param IS NULL' works
        roomCode = nullIfBlank(roomCode);
        status   = nullIfBlank(status);

        return service.getMyHistory(email, roomCode, fromDate, toDate, status, page, size);
    }

    // Quick check endpoint to see raw content for current user
    @GetMapping("/history/debug")
    public Object debug(Authentication auth) {
        String email = resolveEmail(auth);
        log.info("DEBUG PersonalHistory email={}", email);
        return service.getMyHistory(email, null, null, null, null, 0, 5).getContent();
    }

    // ===== helpers =====

    private String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }

    /** Convenience overload: pull HttpServletRequest if available and reuse main resolver */
    private String resolveEmail(Authentication auth) {
        var attrs = RequestContextHolder.getRequestAttributes();
        HttpServletRequest req = (attrs instanceof ServletRequestAttributes sra) ? sra.getRequest() : null;
        return resolveEmail(auth, req);
    }

    @SuppressWarnings("unchecked")
    private String resolveEmail(Authentication auth, @Nullable HttpServletRequest request) {
        if (auth != null) {
            // 1) details.email or details.profile.email (mirror your /auth/me payload)
            Object details = auth.getDetails();
            if (details instanceof Map<?, ?> m) {
                String direct = asEmail(m.get("email"));
                if (direct != null) return direct;

                Object prof = m.get("profile");
                if (prof instanceof Map<?, ?> p) {
                    String profEmail = asEmail(p.get("email"));
                    if (profEmail != null) return profEmail;
                }
            }
            // 2) principal -> UserDetails.username or String (if it looks like an email)
            Object principal = auth.getPrincipal();
            if (principal instanceof UserDetails ud) {
                String maybe = asEmail(ud.getUsername());
                if (maybe != null) return maybe;
            }
            if (principal instanceof String s) {
                String maybe = asEmail(s);
                if (maybe != null) return maybe;
            }
        }

        // 3) Fallback: read JWT subject (email) if request present
        if (request != null) {
            String bearer = request.getHeader("Authorization");
            if (bearer != null && bearer.startsWith("Bearer ")) {
                String token = bearer.substring(7);
                try {
                    String fromToken = asEmail(jwtService.extractEmail(token)); 
                    if (fromToken != null) return fromToken;
                } catch (Exception ignored) {}
            }
        }
        return null; 
    }

    private String asEmail(Object v) {
        if (!(v instanceof String s)) return null;
        s = s.trim();
        return s.contains("@") ? s : null;
    }
}

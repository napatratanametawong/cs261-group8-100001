package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.room.PersonalBookingHistoryResponse;
import com.example.lc2_booking_room.service.room.PersonalBookingHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/me/bookings")
public class PersonalHistoryController {

    private final PersonalBookingHistoryService service;

    @GetMapping("/history")
    public Page<PersonalBookingHistoryResponse> myHistory(
            Authentication auth,  // <-- provided by Spring Security
            @RequestParam(required = false) String roomCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) String status,   // PENDING / APPROVED / REJECTED / CANCELLED
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        String email = resolveEmail(auth);   // <-- same identity source as /auth/me
        return service.getMyHistory(email, roomCode, fromDate, toDate, status, page, size);
    }

    /** Extract email from your JwtAuthenticationFilter output without changing /auth/me */
    @SuppressWarnings("unchecked")
    private String resolveEmail(Authentication auth) {
        if (auth == null) return null;

        // 1) If your filter put claims into details as a Map (recommended)
        Object details = auth.getDetails();
        if (details instanceof Map<?,?> m) {
            Object v = firstNonBlank(
                    m.get("email"),
                    m.get("preferred_username"),
                    m.get("upn")
            );
            if (v instanceof String s && !s.isBlank()) return s;
        }

        // 2) Principal as UserDetails
        Object principal = auth.getPrincipal();
        if (principal instanceof UserDetails ud) return ud.getUsername(); // often the email

        // 3) Principal as String (UsernamePasswordAuthenticationToken)
        if (principal instanceof String s && !s.isBlank()) return s;

        // 4) Fallback to Authentication name
        return auth.getName();
    }

    private Object firstNonBlank(Object... vals) {
        for (Object v : vals) {
            if (v instanceof String s && !s.isBlank()) return s;
        }
        return null;
    }
}

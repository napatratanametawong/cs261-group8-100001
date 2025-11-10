package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.staff.StaffLogResponse;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.service.login.JwtService;
import com.example.lc2_booking_room.service.staff.StaffReservationLogService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/logs")
@RequiredArgsConstructor
public class StaffReservationLogController {

    private final StaffReservationLogService service;
    private final JwtService jwtService;

    // สร้าง log ใหม่
    @PostMapping("/{reservationId}")
    public ResponseEntity<StaffLogResponse> createLog(
            @PathVariable Long reservationId,
            @RequestParam StaffAction action,
            @RequestParam(required = false) String note,
            HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String staffEmail = jwtService.getEmail(token);

        StaffLogResponse log = service.createLog(reservationId, staffEmail, action, note);
        return ResponseEntity.ok(log);
    }

    /* ดึง log ทั้งหมดของ reservation หนึ่ง */
    @GetMapping("/{reservationId}")
    public ResponseEntity<List<StaffLogResponse>> getLogs(@PathVariable Long reservationId) {
        return ResponseEntity.ok(service.getLogsByReservation(reservationId));
    }

    /* ดึง log ทั้งหมดในระบบ */
    @GetMapping
    public ResponseEntity<List<StaffLogResponse>> getAllLogs() {
        return ResponseEntity.ok(service.getAllLogs());
    }
}

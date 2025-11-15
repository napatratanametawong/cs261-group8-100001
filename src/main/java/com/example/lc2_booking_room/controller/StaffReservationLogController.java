package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.staff.StaffLogResponse;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.service.staff.StaffReservationLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/logs")
@RequiredArgsConstructor
public class StaffReservationLogController {

    private final StaffReservationLogService service;

    /** ดึง email ของ staff ปัจจุบันจาก SecurityContext */
    private String getCurrentStaffEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        // ถ้าระบบคุณตั้งค่า principal เป็น email อยู่แล้ว getName() ก็จะเป็น email เลย
        return auth != null ? auth.getName() : null;
    }

    @PutMapping("/{reservationId}/reviewed")
    public ResponseEntity<StaffLogResponse> markReviewed(
            @PathVariable Long reservationId) {

        String staffEmail = getCurrentStaffEmail();
        StaffLogResponse res = service.createLog(
                reservationId,
                staffEmail,
                StaffAction.REVIEWED,
                null
        );
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{reservationId}/returned")
    public ResponseEntity<StaffLogResponse> markReturned(
            @PathVariable Long reservationId,
            @RequestBody String note) {

        String staffEmail = getCurrentStaffEmail();
        StaffLogResponse res = service.createLog(
                reservationId,
                staffEmail,
                StaffAction.RETURNED,
                note
        );
        return ResponseEntity.ok(res);
    }

    // generic create log (ใช้กรณีอื่น ๆ ถ้าจำเป็น)
    @PostMapping("/{reservationId}")
    public ResponseEntity<StaffLogResponse> createLog(
            @PathVariable Long reservationId,
            @RequestParam StaffAction action,
            @RequestParam(required = false) String note) {

        String staffEmail = getCurrentStaffEmail();
        StaffLogResponse log = service.createLog(
                reservationId,
                staffEmail,
                action,
                note
        );
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

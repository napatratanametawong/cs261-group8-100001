package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.reservation.ReservationStaffService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/reservations")
@RequiredArgsConstructor
public class ReservationStaffController {

    private final ReservationStaffService service;

    /* ดึงข้อมูลการจองทั้งหมด */
    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations() {
        return ResponseEntity.ok(service.getAllReservations());
    }

    /* Get reservation by ID (with slot details) */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.getReservationById(id));
    }
}

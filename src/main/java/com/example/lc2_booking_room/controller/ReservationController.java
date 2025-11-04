package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.ReservationServiceBySlots;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationServiceBySlots service;

    @PostMapping("/slots")
    public ResponseEntity<?> createBySlots(@Valid @RequestBody CreateReservationBySlotsRequest req) {
        try {
            ReservationResponse body = service.createReservationBySlots(req);
            return ResponseEntity.ok(body);
        } catch (IllegalStateException e) {
            // กันชนเวลา: บางช่วงเวลาถูกจองแล้ว
            return ResponseEntity.status(409).body(new ErrorMessage(e.getMessage()));
        } catch (IllegalArgumentException e) {
            // validation ไม่ผ่าน: ห้อง/สล็อตไม่ถูกต้อง ฯลฯ
            return ResponseEntity.badRequest().body(new ErrorMessage(e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}

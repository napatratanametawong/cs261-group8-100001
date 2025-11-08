package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.reservation.ReservationServiceBySlots;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationServiceBySlots reservationService;

    /** Create a reservation */
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationBySlotsRequest req) {
        return ResponseEntity.ok(reservationService.createReservationBySlots(req));
    }

    /** Get reservation by ID */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    /** Cancel reservation (Task01-US07) */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<ReservationResponse> cancelReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.cancelReservation(id));
    }
}

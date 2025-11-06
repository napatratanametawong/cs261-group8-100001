package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.reservation.ReservationServiceBySlots;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationServiceBySlots reservationService;

    /** Create a reservation (room + date + multiple slotCodes) */
    @PostMapping
    public ResponseEntity<ReservationResponse> create(@Valid @RequestBody CreateReservationBySlotsRequest req) {
        ReservationResponse res = reservationService.createReservationBySlots(req);
        return ResponseEntity.ok(res);
    }

<<<<<<< HEAD
    /** (Optional) Get one by id – useful for UI refresh after create */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        // implement service method if you want this endpoint
        throw new UnsupportedOperationException("Not yet implemented");
=======
    /** Get reservation by ID (with slot details) */
    @GetMapping("/{id}")
    public ResponseEntity<ReservationResponse> getById(@PathVariable Long id) {
        ReservationResponse res = reservationService.getById(id);
        return ResponseEntity.ok(res);
>>>>>>> 9c49952a6c11ce4411635eed43f97efbec377e74
    }
}
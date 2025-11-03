// controller/ReservationBySlotsController.java
package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.ReservationServiceBySlots;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reservations")
public class ReservationBySlotsController {

    private final ReservationServiceBySlots service;

    public ReservationBySlotsController(ReservationServiceBySlots service) {
        this.service = service;
    }

    @PostMapping("/slots")
    public ResponseEntity<?> createBySlots(@Valid @RequestBody CreateReservationBySlotsRequest req) {
        try {
            return ResponseEntity.ok(service.create(req));
        } catch (ReservationServiceBySlots.RoomBusyException ex) {
            return ResponseEntity.status(409).body(new ErrorMessage(ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorMessage(ex.getMessage()));
        }
    }

    record ErrorMessage(String message) {}
}

package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.reservation.CreateReservationRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.service.ReservationService;
import com.example.lc2_booking_room.service.ReservationService.RoomBusyException;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService service;
    private static final DateTimeFormatter TH_TIME = DateTimeFormatter.ofPattern("HH.mm");

    public ReservationController(ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateReservationRequest req) {
        try {
            ReservationResponse res = service.create(req);
            return ResponseEntity.ok(res);
        } catch (RoomBusyException ex) {
            String msg = String.format("ในเวลา %s - %s ถูกจองแล้ว",
                    TH_TIME.format(ex.getS()), TH_TIME.format(ex.getE()));
            return ResponseEntity.status(409).body(new ErrorMessage(msg));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(new ErrorMessage(ex.getMessage()));
        }
    }

    record ErrorMessage(String message) {}
}

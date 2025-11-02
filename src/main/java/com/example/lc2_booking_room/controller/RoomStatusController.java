package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.room.RoomWithSlotsDTO;
import com.example.lc2_booking_room.service.room.RoomStatusService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomStatusController {

    private final RoomStatusService service;

    public RoomStatusController(RoomStatusService service) {
        this.service = service;
    }

    @GetMapping("/status")
    public List<RoomWithSlotsDTO> getStatus(
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return service.getRoomsWithStatus(date);
    }
}

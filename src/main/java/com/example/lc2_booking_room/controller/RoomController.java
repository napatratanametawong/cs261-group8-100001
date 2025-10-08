package com.example.lc2_booking_room.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import com.example.lc2_booking_room.service.RoomService;
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping("/status")
    public List<Map<String, Object>> getRoomStatus(@RequestParam String date) {
        return roomService.getRoomStatus(date);
    }
}

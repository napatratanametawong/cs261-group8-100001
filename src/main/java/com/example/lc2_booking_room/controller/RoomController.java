package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.dto.room.CreateRoomRequest;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.service.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    // ✅ รวม endpoint เดิม
    @GetMapping
    public List<Room> list(@RequestParam(name = "active", required = false) Boolean active) {
        return roomService.listAll(active);
    }

    @PostMapping
    public ResponseEntity<Room> upsert(@RequestBody Room room) {
        var saved = roomService.upsert(room);
        return ResponseEntity.created(URI.create("/api/rooms/" + saved.getRoomId())).body(saved);
    }

    @PostMapping("/{code}/deactivate")
    public ResponseEntity<Void> deactivate(@PathVariable String code) {
        roomService.deactivate(code);
        return ResponseEntity.noContent().build();
    }

    // ✅ รวม endpoint จาก roomStatus-backend
    @GetMapping("/status")
    public List<Map<String, Object>> getRoomStatus(@RequestParam String date) {
        return roomService.getRoomStatus(date);
    }

    @PostMapping("/create")
    public Boolean createRoom(@RequestBody CreateRoomRequest request) {
        return roomService.createRoom(request);
    }

    @GetMapping("/test")
    public String test() {
        return "hello";
    }
}

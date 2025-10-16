package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.service.RoomService; 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
  private final RoomService service;

  public RoomController(RoomService service) { this.service = service; }

  @GetMapping
  public List<Room> list(@RequestParam(name = "active", required = false) Boolean active) {
    return service.listAll(active);
  }

  @PostMapping
  public ResponseEntity<Room> upsert(@RequestBody Room room) {
    var saved = service.upsert(room);
    return ResponseEntity.created(URI.create("/api/rooms/" + saved.getRoomId())).body(saved);
  }

  @PostMapping("/{code}/deactivate")
  public ResponseEntity<Void> deactivate(@PathVariable String code) {
    service.deactivate(code);
    return ResponseEntity.noContent().build();
  }
}

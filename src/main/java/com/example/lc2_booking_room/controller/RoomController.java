package com.example.lc2_booking_room.controller;

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

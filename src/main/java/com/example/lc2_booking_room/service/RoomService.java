package com.example.lc2_booking_room.service;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Map<String, Object>> getRoomStatus(String date) {
        return roomRepository.getRoomStatuses(date);
    }
}
package com.example.lc2_booking_room.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import com.example.lc2_booking_room.repository.RoomRepositoryCustom;

@Service
public class RoomService {

    @Autowired
    private RoomRepositoryCustom roomRepositoryCustom;

    public List<Map<String, Object>> getRoomStatus(String date) {
        return roomRepositoryCustom.getRoomStatuses(date);
    }
}
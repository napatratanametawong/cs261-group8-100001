package com.example.lc2_booking_room.repository;

import java.util.List;
import java.util.Map;

public interface RoomRepositoryCustom {
    List<Map<String, Object>> getRoomStatuses(String date);
}

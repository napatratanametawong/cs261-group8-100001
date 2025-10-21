package com.example.lc2_booking_room.dto.room;

public record RoomStatusDTO(
    String getRoomCode,
    String getRoomName,
    String getSlotCode,
    String getRoomStatus
) {}

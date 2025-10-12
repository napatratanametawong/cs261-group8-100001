package com.example.lc2_booking_room.dto.room;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateRoomRequest {

    @NotBlank(message = "Room name is required")
    private String roomName;

    @NotBlank(message = "Room type is required")
    private String roomType;

    @NotNull(message = "Capacity is required")
    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;
}

package com.example.lc2_booking_room.dto.room;

import lombok.Data;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class CreateRoomRequest {

    // @NotBlank(message = "Room name is required")
    // private String roomName;

    // @NotBlank(message = "Room type is required")
    // private String roomType;

    // @NotNull(message = "Capacity is required")
    // @Min(value = 1, message = "Capacity must be at least 1")
    // private Integer capacity;

    // public String getFeaturesJson() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getFeaturesJson'");
    // }

    // public Integer getMinCapacity() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getMinCapacity'");
    // }

    // public Integer getMaxCapacity() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'getMaxCapacity'");
    // }

    private String roomName;
    private String roomType;
    private int minCapacity;
    private int maxCapacity;
    private List<String> featuresJson;
}


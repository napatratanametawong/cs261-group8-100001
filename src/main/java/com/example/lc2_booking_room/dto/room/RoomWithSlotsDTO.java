package com.example.lc2_booking_room.dto.room;

import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RoomWithSlotsDTO {
    private String code;
    private String roomName;
    private String roomType;
    private Integer minCapacity;
    private Integer maxCapacity;
    private List<String> features;
    private Map<String, String> slots; 
    private String generatedAt;

    public RoomWithSlotsDTO() {}

    public RoomWithSlotsDTO(String code, String roomName, String roomType,
                            Integer minCapacity, Integer maxCapacity,
                            List<String> features, Map<String, String> slots,
                            String generatedAt) {
        this.code = code;
        this.roomName = roomName;
        this.roomType = roomType;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.features = features;
        this.slots = slots;
        this.generatedAt = generatedAt;
    }
}
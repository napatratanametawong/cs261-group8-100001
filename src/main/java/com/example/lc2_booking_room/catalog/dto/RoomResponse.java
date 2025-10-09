package com.example.lc2_booking_room.catalog.dto;

import java.util.List;

public class RoomResponse {
    private String code;
    private String name;
    private String type;
    private int minCapacity;
    private int maxCapacity;
    private List<String> features;
    private boolean active;

    public RoomResponse(String code, String name, String type, int minCapacity, int maxCapacity, List<String> features, boolean active) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.features = features;
        this.active = active;
    }

    // getters
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public int getMinCapacity() { return minCapacity; }
    public int getMaxCapacity() { return maxCapacity; }
    public List<String> getFeatures() { return features; }
    public boolean isActive() { return active; }
}

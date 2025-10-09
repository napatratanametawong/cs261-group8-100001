package com.example.lc2_booking_room.catalog.dto;

import jakarta.validation.constraints.*;
import java.util.List;

public class RoomRequest {
    @NotBlank @Size(max = 20)
    private String code;

    @NotBlank
    private String name;

    @NotBlank
    private String type;

    @Min(0)
    private int minCapacity;

    @Min(1)
    private int maxCapacity;

    private List<@NotBlank String> features;

    private boolean active;

    // getters/setters
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public int getMinCapacity() { return minCapacity; }
    public void setMinCapacity(int minCapacity) { this.minCapacity = minCapacity; }
    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }
    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    schema = "dbo",
    name = "rooms",
    indexes = @Index(name = "uk_rooms_code", columnList = "code", unique = true)
)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @Column(name = "room_name", nullable = false)
    private String roomName;

    @Column(name = "room_type", nullable = false)
    private String roomType;

    @Column(name = "min_capacity", nullable = false)
    private int minCapacity;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Convert(converter = FeaturesConverter.class)
    @Column(name = "features_json")
    private List<String> features;

    @Column(name = "active", nullable = false)
    private boolean active = true;

    // ----- Constructors -----
    public Room() {}

    public Room(String code, String roomName, String roomType,
                int minCapacity, int maxCapacity,
                List<String> features, boolean active) {
        this.code = code;
        this.roomName = roomName;
        this.roomType = roomType;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.features = features;
        this.active = active;
    }

    // ----- Getters/Setters -----
    public Long getRoomId() { return roomId; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getRoomName() { return roomName; }
    public void setRoomName(String roomName) { this.roomName = roomName; }

    public String getRoomType() { return roomType; }
    public void setRoomType(String roomType) { this.roomType = roomType; }

    public int getMinCapacity() { return minCapacity; }
    public void setMinCapacity(int minCapacity) { this.minCapacity = minCapacity; }

    public int getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(int maxCapacity) { this.maxCapacity = maxCapacity; }

    public List<String> getFeatures() { return features; }
    public void setFeatures(List<String> features) { this.features = features; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}

package com.example.lc2_booking_room.catalog.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(
    name = "rooms",
    indexes = @Index(name = "uk_rooms_code", columnList = "code", unique = true)
)
public class RoomCatalog {

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

    @Column(name = "min_capacity")
    private int minCapacity;

    @Column(name = "max_capacity")
    private int maxCapacity;

    @ElementCollection
    @CollectionTable(name = "room_features", joinColumns = @JoinColumn(name = "room_id"))
    @Column(name = "feature")
    private List<String> features;

    @Column(name = "active")
    private boolean active;

    public RoomCatalog() {}

    public RoomCatalog(String code, String roomName, String roomType, int minCapacity, int maxCapacity, List<String> features, boolean active) {
        this.code = code;
        this.roomName = roomName;
        this.roomType = roomType;
        this.minCapacity = minCapacity;
        this.maxCapacity = maxCapacity;
        this.features = features;
        this.active = active;
    }

    // Getters/Setters
    public Long getRoomId() { return roomId; }
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

package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(
    schema = "dbo",
    name = "rooms",
    indexes = @Index(name = "uk_rooms_code", columnList = "code", unique = true)
)
@Data
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
}

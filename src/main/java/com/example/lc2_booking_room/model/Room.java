package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
@Table(schema = "dbo", name = "rooms")
public class Room {

    @Id
    @Column(name = "code", length = 20, nullable = false)
    private String code;                 // ← ใช้ code เป็น @Id

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
}

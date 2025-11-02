package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.Nationalized;
import java.util.List;

@Getter
@Setter
@Entity
@Table(schema = "dbo", name = "rooms",
       indexes = @Index(name = "uk_rooms_code", columnList = "code", unique = true))
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "code", length = 20, nullable = false, unique = true)
    private String code;

    @Nationalized
    @Column(name = "room_name", nullable = false, length = 200)
    private String roomName;

    @Nationalized
    @Column(name = "room_type", nullable = false, length = 100)
    private String roomType;

    @Column(name = "min_capacity", nullable = false)
    private int minCapacity;

    @Column(name = "max_capacity", nullable = false)
    private int maxCapacity;

    @Convert(converter = FeaturesConverter.class)
    @Nationalized
    @Column(name = "features_json", columnDefinition = "NVARCHAR(MAX)")
    private List<String> features;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}

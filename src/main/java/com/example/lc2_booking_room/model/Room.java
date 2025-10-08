package com.example.lc2_booking_room.model;

@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    private String roomName;
    private String roomType;
    private int capacity;
}

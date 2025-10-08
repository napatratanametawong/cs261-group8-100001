package com.example.lc2_booking_room.model;

@Entity
@Table(name = "time_slots")
public class TimeSlot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long slotId;

    private String slotCode;
    private String startTime;
    private String endTime;
}

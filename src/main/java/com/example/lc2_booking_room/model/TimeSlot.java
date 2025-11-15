package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalTime;

@Getter
@Setter
@Entity
@Table(schema = "dbo", name = "time_slots")
public class TimeSlot {

    @Id
    @Column(name = "slot_code", length = 20, nullable = false)
    private String slotCode;    // ← ใช้ slot_code เป็น @Id

    @Column(name = "start_time")
    private java.time.LocalTime startTime;

    @Column(name = "end_time")
    private java.time.LocalTime endTime;

}

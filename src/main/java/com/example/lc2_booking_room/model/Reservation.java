package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(schema = "dbo", name = "reservations", uniqueConstraints = @UniqueConstraint(name = "uq_reservation", columnNames = {
        "room_id", "slot_id", "date" }), indexes = {
                @Index(name = "ix_reservations_date", columnList = "date"),
                @Index(name = "ix_reservations_room_slot", columnList = "room_id,slot_id")
        })
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long reservationId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id", nullable = false) 
    private TimeSlot slot;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "reserved_by", length = 200)
    private String reservedBy;

    @Column(name = "status", length = 50)
    private String status;
}

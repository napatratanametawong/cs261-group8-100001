package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservation_slots", uniqueConstraints = {
        @UniqueConstraint(name = "uk_reservation_room_slot", columnNames = { "reservation_id", "room_code",
                "slot_code" })
}, indexes = {
        @Index(name = "ix_res_slot_codes", columnList = "room_code,slot_code")
})
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_slot_id")
    private Long id;

    // FK → reservations.reservation_id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    // FK → rooms.code
    @Column(name = "room_code", length = 20, nullable = false)
    private String roomCode;

    // FK → time_slots.slot_code
    @Column(name = "slot_code", length = 20, nullable = false)
    private String slotCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_code", referencedColumnName = "slot_code", insertable = false, updatable = false)
    private TimeSlot timeSlot;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

}

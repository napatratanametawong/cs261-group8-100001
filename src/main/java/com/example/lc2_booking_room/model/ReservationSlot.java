package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.Getter; import lombok.Setter;
import java.time.LocalDate;

@Entity
@Table(name = "reservation_slots", schema = "dbo")
@Getter @Setter
public class ReservationSlot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_slot_id")
    private Long reservationSlotId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "slot_id", nullable = false)
    private Long slotId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}

package com.example.lc2_booking_room.model;

@Entity
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reservationId;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "slot_id")
    private TimeSlot slot;

    private LocalDate date;
    private String reservedBy;
    private String status;
}

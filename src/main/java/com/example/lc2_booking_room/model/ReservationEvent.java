package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Entity
@Table(name = "reservation_events", schema = "dbo")
public class ReservationEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "reservation_id", nullable = false)
    private Long reservationId;

    @Column(name = "room_code", nullable = false, length = 20)
    private String roomCode;

    @Column(name = "event_type", nullable = false, length = 30)
    private String eventType;

    @Column(name = "from_step", length = 30)
    private String fromStep;

    @Column(name = "to_step", length = 30)
    private String toStep;

    @Column(name = "from_final_status", length = 20)
    private String fromFinalStatus;

    @Column(name = "to_final_status", length = 20)
    private String toFinalStatus;

    @Column(name = "actor_email", length = 100)
    private String actorEmail;

    @Column(name = "notes")
    private String notes;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;
}

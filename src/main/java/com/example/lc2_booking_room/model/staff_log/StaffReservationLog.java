package com.example.lc2_booking_room.model.staff_log;

import com.example.lc2_booking_room.model.Reservation;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;


import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Entity
@Table(name = "staff_reservation_logs", schema = "dbo", indexes = {
        @Index(name = "idx_stafflog_reservation", columnList = "reservation_id"),
        @Index(name = "idx_stafflog_staff_email", columnList = "staff_email"),
        @Index(name = "idx_stafflog_changed_at", columnList = "changed_at")
})
public class StaffReservationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "staff_log_id")
    private Long staffLogId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "reservation_id")
    @JsonBackReference
    private Reservation reservation;

    @Column(name = "staff_email", length = 100, nullable = false)
    private String staffEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 50, nullable = false)
    private StaffAction action;

    @Lob
    @Column(name = "note")
    private String note;

    // เปลี่ยนเป็น OffsetDateTime + เก็บเป็นเวลาไทย
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    // ให้ entity เซ็ตเวลาเองตอน insert
    @PrePersist
    public void prePersist() {
        if (changedAt == null) {
            changedAt = OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
        }
    }
}

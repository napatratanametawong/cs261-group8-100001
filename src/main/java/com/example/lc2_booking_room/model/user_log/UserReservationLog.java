// src/main/java/com/example/lc2_booking_room/model/user_log/UserReservationLog.java
package com.example.lc2_booking_room.model.user_log;

import com.example.lc2_booking_room.model.Reservation;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Getter
@Setter
@Entity
@Table(name = "user_reservation_logs", schema = "dbo", indexes = {
        @Index(name = "idx_logs_reservation", columnList = "reservation_id"),
        @Index(name = "idx_logs_user_email", columnList = "user_email"),
        @Index(name = "idx_logs_changed_at", columnList = "changed_at")
})
public class UserReservationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_log_id")
    private Long userLogId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(name = "user_email", length = 100)
    private String userEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", length = 100, nullable = false)
    private LogAction action;

    // เก็บเวลาแบบ OffsetDateTime ให้ตรงกับ DATETIMEOFFSET(+7)
    @Column(name = "changed_at", nullable = false, updatable = false)
    private OffsetDateTime changedAt;

    @Lob
    @Column(name = "note")
    private String note;

    // ให้ entity เซ็ตเวลาเองตอน insert เป็นเวลาไทย
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
        }
    }
}

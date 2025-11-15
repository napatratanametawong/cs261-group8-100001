// src/main/java/com/example/lc2_booking_room/model/user_log/UserReservationLog.java
package com.example.lc2_booking_room.model.user_log;

import com.example.lc2_booking_room.model.Reservation;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    @Lob
    @Column(name = "note")
    private String note;

    // ✅ ให้ Hibernate เซ็ตเวลาลง changed_at อัตโนมัติ
    @PrePersist
    protected void onCreate() {
        if (changedAt == null) {
            changedAt = LocalDateTime.now();
        }
    }

    // getters/setters
    public Long getUserLogId() {
        return userLogId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public LogAction getAction() {
        return action;
    }

    public void setAction(LogAction action) {
        this.action = action;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

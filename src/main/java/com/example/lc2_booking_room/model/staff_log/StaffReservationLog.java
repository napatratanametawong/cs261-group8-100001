package com.example.lc2_booking_room.model.staff_log;

import com.example.lc2_booking_room.model.Reservation;
import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

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

    @CreationTimestamp
    @Column(name = "changed_at", nullable = false, updatable = false)
    private LocalDateTime changedAt;

    public Long getStaffLogId() {
        return staffLogId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        this.reservation = reservation;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }

    public StaffAction getAction() {
        return action;
    }

    public void setAction(StaffAction action) {
        this.action = action;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public LocalDateTime getChangedAt() {
        return changedAt;
    }
}

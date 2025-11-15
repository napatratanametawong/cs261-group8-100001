package com.example.lc2_booking_room.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import java.util.ArrayList;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "reservations", indexes = {
                @Index(name = "ix_res_room_date", columnList = "room_code,reservation_date"),
                @Index(name = "ix_res_created_at", columnList = "created_at")
})
public class Reservation {

        public enum BookingStep {
                SUBMITTED, STAFF_REVIEW, RETURNED_FOR_FIX, RESUBMITTED, HEAD_REVIEW, DECIDED
        }

        public enum FinalStatus {
                PENDING, APPROVED, REJECTED, CANCELLED
        }

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "reservation_id")
        private Long id;

        @Column(name = "room_code", length = 20, nullable = false)
        private String roomCode;

        @Column(name = "reservation_date", nullable = false)
        private LocalDate reservationDate;

        @Column(name = "reason", length = 255)
        private String reason;

        @Lob
        @Column(name = "file_attachment")
        private String fileAttachment;

        @Enumerated(EnumType.STRING)
        @Column(name = "step", length = 50)
        private BookingStep step;

        @Enumerated(EnumType.STRING)
        @Column(name = "final_status", length = 50)
        private FinalStatus finalStatus;

        @Column(name = "user_email", length = 100)
        private String userEmail;

        @Column(name = "user_name", length = 100)
        private String userName;

        @Column(name = "staff_reviewer_email", length = 100)
        private String staffReviewerEmail;

        @Column(name = "staff_reviewed_at")
        private OffsetDateTime staffReviewedAt;

        @Column(name = "head_approver_email", length = 100)
        private String headApproverEmail;

        @Column(name = "head_decided_at")
        private OffsetDateTime headDecidedAt;

        @Lob
        @Column(name = "return_reason")
        private String returnReason;

        @Lob
        @Column(name = "reject_reason")
        private String rejectReason;

        @Lob
        @Column(name = "cancel_reason")
        private String cancelReason;

        // children slots of this reservation
        @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
        @Builder.Default
        @JsonManagedReference
        private List<ReservationSlot> slots = new ArrayList<>();

        public void addSlot(ReservationSlot slot) {
                slots.add(slot);
                slot.setReservation(this);
        }

        public void removeSlot(ReservationSlot slot) {
                slots.remove(slot);
                slot.setReservation(null);
        }

        @Column(name = "approved_at")
        private OffsetDateTime approvedAt;

        @Column(name = "created_at")
        private OffsetDateTime createdAt;
}

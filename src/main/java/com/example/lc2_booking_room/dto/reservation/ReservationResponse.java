package com.example.lc2_booking_room.dto.reservation;

import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class ReservationResponse {

    private Long reservationId;

    private String roomCode;

    private LocalDate reservationDate;

    private String reason;

    private String fileAttachment;

    private String step;         // BookingStep (STRING)
    private String finalStatus;  // FinalStatus (STRING)

    private String userEmail;
    private String userName;

    private String staffReviewerEmail;
    private OffsetDateTime staffReviewedAt;
    private String headApproverEmail;
    private OffsetDateTime headDecidedAt;

    private String returnReason;
    private String rejectReason;
    private String cancelReason;

    private OffsetDateTime approvedAt;
    private OffsetDateTime createdAt;

    // สรุปรายการ slot ที่ถูกจองใน reservation นี้
    private List<SlotItem> slots;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class SlotItem {
        private String slotCode;   // FK → time_slots.slot_code
        private Boolean isActive;  // ตามคอลัมน์ reservation_slots.is_active
    }
}
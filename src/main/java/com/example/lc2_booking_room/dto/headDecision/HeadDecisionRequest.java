package com.example.lc2_booking_room.dto.headDecision;

import lombok.Data;

@Data
public class HeadDecisionRequest {

    public enum Decision {
        APPROVED,
        REJECTED
    }

    // APPROVED หรือ REJECTED
    private Decision decision;

    // ข้อความเหตุผล → จะไปลง Reservation.rejectReason (reject_reason)
    private String remark;
}
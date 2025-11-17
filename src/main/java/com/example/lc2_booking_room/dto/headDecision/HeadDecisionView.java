package com.example.lc2_booking_room.dto.headDecision;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HeadDecisionView {
    private Long id;                
    private String roomCode;
    private String roomType;
    private String reservationDate;
    private String userName;
    private String userEmail;
    private String reason;
    private String fileAttachment;

    private String step;
    private String finalStatus;

    private String staffReviewerEmail;
    private String headApproverEmail;
}

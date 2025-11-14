// src/main/java/com/example/lc2_booking_room/service/headDecision/HeadDecisionService.java
package com.example.lc2_booking_room.service.headDecision;

import com.example.lc2_booking_room.dto.headDecision.HeadDecisionRequest;
import com.example.lc2_booking_room.dto.headDecision.HeadDecisionView;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.Reservation.BookingStep;
import com.example.lc2_booking_room.model.Reservation.FinalStatus;
import com.example.lc2_booking_room.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class HeadDecisionService {

    private final ReservationRepository reservationRepository;

    
    @Value("${app.head-email}")
    private String headEmail;

    @Transactional(readOnly = true)
    public HeadDecisionView getReservationForHead(Long id) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));

        // ระบบนี้ทำงานเฉพาะตอน step = STAFF_REVIEW ตามที่คุณกำหนด
        if (r.getStep() != BookingStep.STAFF_REVIEW) {
            throw new IllegalStateException("Reservation is not in STAFF_REVIEW step");
        }

        return HeadDecisionView.builder()
                .id(r.getId())
                .roomCode(r.getRoomCode())
                .reservationDate(r.getReservationDate() != null ? r.getReservationDate().toString() : null)
                .userName(r.getUserName())
                .userEmail(r.getUserEmail())
                .reason(r.getReason())
                .fileAttachment(r.getFileAttachment())
                .step(r.getStep() != null ? r.getStep().name() : null)
                .finalStatus(r.getFinalStatus() != null ? r.getFinalStatus().name() : null)
                .staffReviewerEmail(r.getStaffReviewerEmail())
                .headApproverEmail(r.getHeadApproverEmail())
                .build();
    }

    @Transactional
    public void decide(Long id, HeadDecisionRequest req) {
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + id));

        if (r.getStep() != BookingStep.STAFF_REVIEW) {
            throw new IllegalStateException("Reservation is not in STAFF_REVIEW step");
        }

        if (r.getFinalStatus() != null && r.getFinalStatus() != FinalStatus.PENDING) {
            throw new IllegalStateException("Reservation already decided: " + r.getFinalStatus());
        }

        r.setHeadApproverEmail(headEmail);
        r.setHeadDecidedAt(OffsetDateTime.now());
        r.setStep(BookingStep.DECIDE);

        if (req.getDecision() == HeadDecisionRequest.Decision.APPROVED) {
            // ✅ เคสอนุมัติ: ไม่ใช้ reject_reason เลย → เคลียร์ให้ null
            r.setFinalStatus(FinalStatus.APPROVED);
            r.setApprovedAt(OffsetDateTime.now());
            r.setRejectReason(null);
        } else if (req.getDecision() == HeadDecisionRequest.Decision.REJECTED) {
            // ✅ เคสไม่อนุมัติ: remark → ลง field reject_reason
            r.setFinalStatus(FinalStatus.REJECTED);
            r.setRejectReason(req.getRemark());
            r.setApprovedAt(null);
        }

        // ❌ ไม่แตะ returnReason, cancelReason เพราะเป็น flow คนละแบบ
        reservationRepository.save(r);
    }
}

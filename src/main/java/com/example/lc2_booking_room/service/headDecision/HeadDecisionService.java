// src/main/java/com/example/lc2_booking_room/service/headDecision/HeadDecisionService.java
package com.example.lc2_booking_room.service.headDecision;

import com.example.lc2_booking_room.dto.headDecision.HeadDecisionRequest;
import com.example.lc2_booking_room.dto.headDecision.HeadDecisionView;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.Reservation.BookingStep;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.service.staff.StaffReservationLogService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class HeadDecisionService {

    private final ReservationRepository reservationRepository;
    private final StaffReservationLogService staffReservationLogService;
    

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

        // ทำงานได้เฉพาะตอน STAFF_REVIEW
        if (r.getStep() != Reservation.BookingStep.STAFF_REVIEW) {
            throw new IllegalStateException("Reservation is not in STAFF_REVIEW step");
        }

        // ถ้าเคยตัดสินไปแล้ว ไม่ให้ตัดสินซ้ำ
        if (r.getFinalStatus() != null && r.getFinalStatus() != Reservation.FinalStatus.PENDING) {
            throw new IllegalStateException("Reservation already decided: " + r.getFinalStatus());
        }

        // ใช้เวลาไทย Asia/Bangkok
        java.time.OffsetDateTime now = java.time.OffsetDateTime.now(java.time.ZoneId.of("Asia/Bangkok"));

        // ✅ บันทึกอีเมลหัวหน้าสาขา + เวลา
        r.setHeadApproverEmail(headEmail); // headEmail มาจาก @Value หรือ config ที่คุณตั้งไว้
        r.setHeadDecidedAt(now);
        r.setStep(Reservation.BookingStep.DECIDED);

        StaffAction action;
        if (req.getDecision() == HeadDecisionRequest.Decision.APPROVED) {
            r.setFinalStatus(Reservation.FinalStatus.APPROVED);
            r.setApprovedAt(now); // ใช้เวลาไทยเหมือนกัน
            r.setRejectReason(null); // เคลียร์เหตุผล reject ถ้ามีค้าง
            action = StaffAction.APPROVED;
        } else if (req.getDecision() == HeadDecisionRequest.Decision.REJECTED) {
            r.setFinalStatus(Reservation.FinalStatus.REJECTED);
            r.setRejectReason(req.getRemark());
            r.setApprovedAt(null);
            action = StaffAction.REJECTED;
        } else {
            throw new IllegalArgumentException("Unknown decision: " + req.getDecision());
        }
        
        staffReservationLogService.logHeadDecision(
                r,
                headEmail,
                action,
                req.getRemark()
        );

        reservationRepository.save(r);
    }
}

package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.ReservationSlot;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.ReservationSlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationStaffService {

    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    /* ดึงข้อมูลการจองทั้งหมด */
    public List<ReservationResponse> getAllReservations() {
        return reservationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* ดึงข้อมูลการจองตาม ID */
    public ReservationResponse getReservationById(Long id) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));
        return toResponse(reservation);
    }

    /* แปลง Reservation → ReservationResponse */
    private ReservationResponse toResponse(Reservation r) {
        // ดึง slots ของ reservation นี้
        List<ReservationSlot> slots = reservationSlotRepository.findByReservation_Id(r.getId());

        // แปลง slot เป็น DTO ย่อยใน ReservationResponse
        List<ReservationResponse.SlotItem> slotItems = slots.stream()
                .map(s -> ReservationResponse.SlotItem.builder()
                        .slotCode(s.getSlotCode())
                        .isActive(s.getIsActive())
                        .build())
                .collect(Collectors.toList());

        // แปลงข้อมูลหลัก Reservation → Response
        return ReservationResponse.builder()
                .reservationId(r.getId())
                .roomCode(r.getRoomCode())
                .reservationDate(r.getReservationDate())
                .reason(r.getReason())
                .fileAttachment(r.getFileAttachment())
                .step(r.getStep() != null ? r.getStep().name() : null)
                .finalStatus(r.getFinalStatus() != null ? r.getFinalStatus().name() : null)
                .userEmail(r.getUserEmail())
                .userName(r.getUserName())
                .staffReviewerEmail(r.getStaffReviewerEmail())
                .staffReviewedAt(r.getStaffReviewedAt())
                .headApproverEmail(r.getHeadApproverEmail())
                .headDecidedAt(r.getHeadDecidedAt())
                .returnReason(r.getReturnReason())
                .rejectReason(r.getRejectReason())
                .cancelReason(r.getCancelReason())
                .approvedAt(r.getApprovedAt())
                .createdAt(r.getCreatedAt())
                .slots(slotItems)
                .build();
    }
}

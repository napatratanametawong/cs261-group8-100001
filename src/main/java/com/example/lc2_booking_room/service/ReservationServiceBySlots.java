package com.example.lc2_booking_room.service;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.ReservationSlot;
import com.example.lc2_booking_room.model.TimeSlot;
import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.TimeSlotRepository;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.ReservationSlotRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceBySlots {

    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;

    /**
     * Flow ตาม ERD:
     * 1) validate room + slot
     * 2) กันชน (roomCode + reservationDate + slotCode && isActive)
     * 3) สร้าง Reservation
     * 4) สร้าง ReservationSlot หลายแถว
     * 5) map → ReservationResponse
     */
    @Transactional
    public ReservationResponse createReservationBySlots(CreateReservationBySlotsRequest req) {

        // ---- 0) sanitize ----
        final String roomCode = req.getRoomCode().trim();
        final LocalDate date = req.getReservationDate();
        final List<String> requestedSlotCodes = req.getSlotCodes().stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (requestedSlotCodes.isEmpty()) {
            throw new IllegalArgumentException("ต้องเลือกอย่างน้อย 1 slot");
        }

        // ---- 1) validate: room exists & active ----
        // ถ้าไม่มีเมธอด existsByCodeAndActiveTrue ใน RoomRepository ให้ใช้ findById แล้วเช็ค active แทน
        boolean roomOk = roomRepository.existsByCodeAndActiveTrue(roomCode);
        boolean roomExists = roomRepository.existsByCodeAndActiveTrue(roomCode);
        if (!roomExists) {
            throw new IllegalArgumentException("ไม่พบห้อง หรือห้องไม่เปิดใช้งาน: " + roomCode);
        }

        // ---- 1.1) validate: slots exist & active ----
        List<TimeSlot> activeSlots = timeSlotRepository.findBySlotCodeIn(requestedSlotCodes);
        Set<String> activeSlotCodes = activeSlots.stream().map(TimeSlot::getSlotCode).collect(Collectors.toSet());

        List<String> missingOrInactive = requestedSlotCodes.stream()
                .filter(c -> !activeSlotCodes.contains(c))
                .toList();
        if (!missingOrInactive.isEmpty()) {
            throw new IllegalArgumentException("slot ต่อไปนี้ไม่พบหรือไม่เปิดใช้งาน: " + String.join(", ", missingOrInactive));
        }

        // ---- 2) conflict check ----
        boolean hasConflict = reservationSlotRepository.anyActiveConflict(roomCode, date, requestedSlotCodes);
        if (hasConflict) {
            // แปลง slot เป็นเวลาเพื่อบอกผู้ใช้แบบอ่านง่าย
            /*
            String human = activeSlots.stream()
                    .sorted(Comparator.comparing(TimeSlot::getStartTime))
                    .map(ts -> ts.getStartTime() + "–" + ts.getEndTime())
                    .collect(Collectors.joining(", "));
            */
            String human = activeSlots.stream()
                    .sorted(Comparator.comparing(TimeSlot::getStartTime))
                    .map(ts -> ts.getStartTime() + " - " + ts.getEndTime())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("ช่วงเวลาบางส่วนถูกจองแล้ว (" + human + ")");
        }

        // ---- 3) persist Reservation ----
        Reservation reservation = Reservation.builder()
                .roomCode(roomCode)
                .reservationDate(date)
                .reason(req.getReason())
                .fileAttachment(req.getFileAttachment())
                .userEmail(req.getUserEmail())
                .userName(req.getUserName())
                .step(Reservation.BookingStep.SUBMITTED)   // ค่าเริ่มต้นตามที่ต้องการ
                .finalStatus(Reservation.FinalStatus.PENDING)
                .createdAt(java.time.OffsetDateTime.now())
                .build();

        Reservation toSave = Reservation.builder()
                .roomCode(roomCode)
                .reservationDate(date)
                .reason(req.getReason())
                .fileAttachment(req.getFileAttachment())
                .userEmail(req.getUserEmail())
                .userName(req.getUserName())
                .step(Reservation.BookingStep.SUBMITTED)
                .finalStatus(Reservation.FinalStatus.PENDING)
                .createdAt(java.time.OffsetDateTime.now())
                .build();

        final Reservation savedReservation = reservationRepository.save(toSave);

        // ---- 4) persist ReservationSlot (bulk) ----
        List<ReservationSlot> slots = requestedSlotCodes.stream()
                .map(code -> ReservationSlot.builder()
                        .reservation(reservation)
                        .roomCode(roomCode)
                        .slotCode(code)
                        .isActive(true)
                        .build())
                .toList();

        reservationSlotRepository.saveAll(slots);

        // ---- 5) map → DTO response ----
        return toResponse(reservation, slots, activeSlots);
    }

    private ReservationResponse toResponse(Reservation r, List<ReservationSlot> slotRows, List<TimeSlot> slotDefs) {
        // เตรียม map slotCode → TimeSlot (ไว้แปลงเวลา)
        Map<String, TimeSlot> slotMap = slotDefs.stream()
                .collect(Collectors.toMap(TimeSlot::getSlotCode, s -> s));

        List<ReservationResponse.SlotItem> slotItems = slotRows.stream()
                .sorted(Comparator.comparing(ReservationSlot::getSlotCode))
                .map(rs -> ReservationResponse.SlotItem.builder()
                        .slotCode(rs.getSlotCode())
                        .isActive(rs.getIsActive())
                        .build())
                .toList();

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

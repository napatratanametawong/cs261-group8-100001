package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.ReservationSlot;
import com.example.lc2_booking_room.model.TimeSlot;
import com.example.lc2_booking_room.model.user_log.UserReservationLog;
import com.example.lc2_booking_room.model.user_log.LogAction;
import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.TimeSlotRepository;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.ReservationSlotRepository;
import com.example.lc2_booking_room.repository.UserReservationLogRepository;
import org.springframework.transaction.annotation.Transactional;  
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationServiceBySlots {

    private final RoomRepository roomRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final UserReservationLogRepository userReservationLogRepository; // direct user log
    

     
    @Transactional(readOnly = true)
    public ReservationResponse getById(Long id) {
        // load parent; inside TX so lazy slots can initialize safely
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Reservation not found: " + id));

        // children
        List<ReservationSlot> slotRows = r.getSlots(); // uses your mapped relation

        // slot defs for mapping (uses only codes; safe if empty)
        List<String> codes = slotRows.stream().map(ReservationSlot::getSlotCode).toList();
        List<TimeSlot> slotDefs = codes.isEmpty()
                ? List.of()
                : timeSlotRepository.findBySlotCodeIn(codes);

        // reuse your existing mapper (keep your original comments)
        return toResponse(r, slotRows, slotDefs);
    }
    
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
                .filter(Objects::nonNull).map(String::trim)
                .filter(s -> !s.isEmpty()).distinct().toList();

        if (requestedSlotCodes.isEmpty()) {
            throw new IllegalArgumentException("ต้องเลือกอย่างน้อย 1 slot");
        }

        // ---- 1) validate: room exists & active ----
        if (!roomRepository.existsByCodeAndActiveTrue(roomCode)) {
            throw new IllegalArgumentException("ไม่พบห้อง หรือห้องไม่เปิดใช้งาน: " + roomCode);
        }

        // ---- 1.1) validate: slots exist & active ----
        List<TimeSlot> activeSlots = timeSlotRepository.findBySlotCodeIn(requestedSlotCodes);
        Set<String> activeSlotCodes = activeSlots.stream().map(TimeSlot::getSlotCode).collect(Collectors.toSet());
        List<String> missingOrInactive = requestedSlotCodes.stream().filter(c -> !activeSlotCodes.contains(c)).toList();
        if (!missingOrInactive.isEmpty()) {
            throw new IllegalArgumentException("slot ต่อไปนี้ไม่พบหรือไม่เปิดใช้งาน: " + String.join(", ", missingOrInactive));
        }

        // ---- 2) conflict check ----
        boolean hasConflict = reservationSlotRepository.anyActiveConflict(roomCode, date, requestedSlotCodes);
        if (hasConflict) {
            String human = activeSlots.stream()
                    .sorted(Comparator.comparing(TimeSlot::getStartTime))
                    .map(ts -> ts.getStartTime() + " - " + ts.getEndTime())
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException("ในเวลา " + human + " ถูกจองแล้ว");
        }

        // ---- 3) build parent (single instance) ----
        // IMPORTANT: we'll attach children to THIS instance and save THIS one.
        Reservation reservation = Reservation.builder()
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

        // ---- 4) build children and attach via helper (sets both sides) ----
        for (String code : requestedSlotCodes) {
            ReservationSlot slot = ReservationSlot.builder()
                    .roomCode(roomCode)
                    .slotCode(code)
                    .isActive(true)
                    .build();
            reservation.addSlot(slot); // <<< CRITICAL: sets slot.reservation = reservation
        }

        // ---- 5) save ONLY the parent (children cascade) ----
        Reservation saved = reservationRepository.save(reservation);

        // ---- 6) map → DTO response ----
        // use the saved graph (children available via saved.getSlots())
        ReservationResponse res = toResponse(saved, saved.getSlots(), activeSlots);

        // ---- 6.1) write user log after successful save (direct write in same TX) ----
        UserReservationLog log = new UserReservationLog();
        log.setReservation(saved);
        log.setUserEmail(res.getUserEmail());
        log.setAction(LogAction.CREATED);
        log.setNote("Reservation created");
        // uncomment IF your DB column 'changed_at' has NO default or @CreationTimestamp
        // log.setChangedAt(LocalDateTime.now());
        userReservationLogRepository.save(log);

        return res;
    }

    private ReservationResponse toResponse(
            Reservation r,
            List<ReservationSlot> slotRows,
            List<TimeSlot> slotDefs) {

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

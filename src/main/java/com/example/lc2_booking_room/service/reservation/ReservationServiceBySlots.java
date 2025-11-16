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
import com.example.lc2_booking_room.service.notification.StaffEmailNotificationService;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

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
        private final StaffEmailNotificationService notificationService; // for notifying staff

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

                // ✅ 0.5) resolve userEmail / userName ให้ไม่เป็น null
                // 1) ลองดึงจาก SecurityContext (คนที่ login อยู่)
                String authEmail = null;
                var auth = SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.isAuthenticated()) {
                        authEmail = auth.getName(); // ส่วนใหญ่ config ไว้ให้เป็น email อยู่แล้ว
                }

                // 2) รวมกับค่าที่มาจาก request (ถ้ามี)
                String userEmail = req.getUserEmail();
                String userName = req.getUserName();

                if (userEmail != null && !userEmail.isBlank()) {
                        userEmail = userEmail.trim();
                } else if (authEmail != null && !authEmail.isBlank()) {
                        userEmail = authEmail.trim();
                }

                if (userEmail == null || userEmail.isBlank()) {
                        // กันไม่ให้สร้าง reservation ถ้าไม่รู้ว่าเป็นของใคร
                        throw new IllegalStateException("ไม่พบอีเมลผู้ใช้ในระบบ ไม่สามารถสร้างคำร้องได้");
                }

                if (userName != null && !userName.isBlank()) {
                        userName = userName.trim();
                } else {
                        // ถ้าอยากบังคับให้ FE ส่งชื่อมาเสมอ → ใช้ throw แทน default
                        throw new IllegalArgumentException("ต้องระบุชื่อผู้ใช้ (user_name)");
                }

                // ---- 1) validate: room exists & active ----
                if (!roomRepository.existsByCodeAndActiveTrue(roomCode)) {
                        throw new IllegalArgumentException("ไม่พบห้อง หรือห้องไม่เปิดใช้งาน: " + roomCode);
                }

                // ---- 1.1) validate: slots exist & active ----
                List<TimeSlot> activeSlots = timeSlotRepository.findBySlotCodeIn(requestedSlotCodes);
                Set<String> activeSlotCodes = activeSlots.stream().map(TimeSlot::getSlotCode)
                                .collect(Collectors.toSet());
                List<String> missingOrInactive = requestedSlotCodes.stream().filter(c -> !activeSlotCodes.contains(c))
                                .toList();
                if (!missingOrInactive.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "slot ต่อไปนี้ไม่พบหรือไม่เปิดใช้งาน: " + String.join(", ", missingOrInactive));
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
                Reservation reservation = Reservation.builder()
                                .roomCode(roomCode)
                                .reservationDate(date)
                                .reason(req.getReason())
                                .fileAttachment(req.getFileAttachment())
                                .userEmail(userEmail) // ✅ ใช้ค่าที่เรา resolve แล้ว
                                .userName(userName) // ✅ ไม่ใช่ค่าดิบจาก req โดยตรง
                                .step(Reservation.BookingStep.SUBMITTED)
                                .finalStatus(Reservation.FinalStatus.PENDING)
                                .createdAt(OffsetDateTime.now(ZoneId.of("Asia/Bangkok")))
                                .build();

                // ---- 4) build children and attach via helper (sets both sides) ----
                for (String code : requestedSlotCodes) {
                        ReservationSlot slot = ReservationSlot.builder()
                                        .roomCode(roomCode)
                                        .slotCode(code)
                                        .isActive(true)
                                        .build();
                        reservation.addSlot(slot);
                }

                // ---- 5) save ONLY the parent (children cascade) ----
                Reservation saved = reservationRepository.save(reservation);

                // ---- 6) map → DTO response ----
                ReservationResponse res = toResponse(saved, saved.getSlots(), activeSlots);

                // ---- 6.1) write user log after successful save ----
                UserReservationLog log = new UserReservationLog();
                log.setReservation(saved);
                log.setUserEmail(res.getUserEmail());
                log.setAction(LogAction.CREATED);
                log.setNote("Reservation created");
                userReservationLogRepository.save(log);
                notificationService.notifyCreated(saved);
                return res;
        }

        private static OffsetDateTime toBkk(OffsetDateTime t) {
                return t == null ? null : t.withOffsetSameInstant(ZoneOffset.ofHours(7));
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
                                .staffReviewedAt(toBkk(r.getStaffReviewedAt()))
                                .headApproverEmail(r.getHeadApproverEmail())
                                .headDecidedAt(toBkk(r.getHeadDecidedAt()))
                                .returnReason(r.getReturnReason())
                                .rejectReason(r.getRejectReason())
                                .cancelReason(r.getCancelReason())
                                .approvedAt(toBkk(r.getApprovedAt()))
                                .createdAt(toBkk(r.getCreatedAt()))
                                .slots(slotItems)
                                .build();
        }

        /* Cancel Reservation */
        @Transactional
        public ReservationResponse cancelReservation(Long id) {
                Reservation reservation = reservationRepository.findById(id)
                                // Use NoSuchElementException for 404 Not Found, and match the spec's error
                                // message.
                                .orElseThrow(() -> new NoSuchElementException("Reservation not found"));

                // เงื่อนไขยกเลิก: ต้องเป็น SUBMITTED + PENDING เท่านั้น
                if (reservation.getStep() != Reservation.BookingStep.SUBMITTED
                                || reservation.getFinalStatus() != Reservation.FinalStatus.PENDING) { // PENDING is the
                                                                                                      // only
                                                                                                      // cancellable
                                                                                                      // status
                        // Use IllegalArgumentException for 400 Bad Request, as the client sent a
                        // request for an un-cancellable resource.
                        throw new IllegalArgumentException("Reservation cannot be canceled");
                }

                // เปลี่ยนสถานะ → CANCELLED (ไม่มีการเก็บเหตุผล)
                reservation.setFinalStatus(Reservation.FinalStatus.CANCELLED);
                reservation.setCancelReason("Cancelled by user"); // Optional: add a default reason

                // Update the state of the managed entities. JPA will handle the update.
                // This is more efficient than a separate repository call.
                reservation.getSlots().forEach(s -> s.setIsActive(false));

                Reservation saved = reservationRepository.save(reservation);

                UserReservationLog log = new UserReservationLog();
                log.setReservation(saved);
                String performedByEmail = SecurityContextHolder.getContext().getAuthentication().getName();
                log.setUserEmail(performedByEmail != null ? performedByEmail : saved.getUserEmail());
                log.setAction(LogAction.CANCELED);
                log.setNote("Reservation cancelled by user");
                userReservationLogRepository.save(log);
                notificationService.notifyCanceled(saved);
                return toResponse(saved, saved.getSlots(), List.of());
        }
}

package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.dto.staff.StaffLogResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.model.staff_log.StaffReservationLog;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.StaffReservationLogRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffReservationLogService {

    private final StaffReservationLogRepository logRepo;
    private final ReservationRepository reservationRepo;

    // APPROVED, REJECTED, REVIEWED, RETURNED, CANCELLED

    @Transactional
    public StaffLogResponse createLog(Long reservationId, String staffEmail, StaffAction action, String note) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // action
        switch (action) {

            case REVIEWED -> {
                reservation.setStep(Reservation.BookingStep.STAFF_REVIEW);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(OffsetDateTime.now());
            }
            case RETURNED -> {
                reservation.setStep(Reservation.BookingStep.RETURNED_FOR_FIX);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setReturnReason(note);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(OffsetDateTime.now());
            }

        }

        // save reservation
        reservationRepo.save(reservation);

        // save in StaffLog
        StaffReservationLog log = new StaffReservationLog();
        log.setReservation(reservation);
        log.setStaffEmail(staffEmail);
        log.setAction(action);
        log.setNote(note);

        StaffReservationLog saved = logRepo.save(log);

        // DTO
        return StaffLogResponse.builder()
                .staffLogId(saved.getStaffLogId())
                .reservationId(reservation.getId())
                .staffEmail(saved.getStaffEmail())
                .action(saved.getAction())
                .changedAt(saved.getChangedAt())
                .note(saved.getNote())
                .build();
    }

    // ดึง staffLog ของ reservation เดียว
    public List<StaffLogResponse> getLogsByReservation(Long reservationId) {
        return logRepo.findByReservation_Id(reservationId)
                .stream()
                .map(log -> StaffLogResponse.builder()
                        .staffLogId(log.getStaffLogId())
                        .reservationId(log.getReservation().getId())
                        .staffEmail(log.getStaffEmail())
                        .action(log.getAction())
                        .changedAt(log.getChangedAt())
                        .note(log.getNote())
                        .build())
                .toList();
    }

    // ดึง StaffLog ทั้งหมด
    public List<StaffLogResponse> getAllLogs() {
        return logRepo.findAll()
                .stream()
                .map(log -> StaffLogResponse.builder()
                        .staffLogId(log.getStaffLogId())
                        .reservationId(log.getReservation().getId())
                        .staffEmail(log.getStaffEmail())
                        .action(log.getAction())
                        .changedAt(log.getChangedAt())
                        .note(log.getNote())
                        .build())
                .toList();
    }
}

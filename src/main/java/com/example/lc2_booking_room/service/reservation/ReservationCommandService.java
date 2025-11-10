package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.service.staff.StaffReservationLogService;

import lombok.RequiredArgsConstructor;

import java.time.OffsetDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationLogService reservationLogService; // for user log
    private final StaffReservationLogService staffLogService; // for staff log

    /**
     * Create reservation inside one transaction and emit CREATED log
     */
    @Transactional
    public Reservation createReservation(CreateReservationRequest req, String actorEmail) {
        // map request -> entity (keep your existing mapping here)
        Reservation r = new Reservation();
        Reservation saved = reservationRepository.save(r);

        // write user log after successful save (listener persists AFTER_COMMIT)
        String emailForLog = (actorEmail != null && !actorEmail.isBlank())
                ? actorEmail
                : req.userEmail();
        reservationLogService.created(saved.getId(), emailForLog);

        return saved;
    }

    @Transactional
    public Reservation createReservation(CreateReservationRequest req) {
        return createReservation(req, /* actorEmail */ null);
    }

    // staff approved
    @Transactional
    public Reservation approveReservation(Long reservationId, String staffEmail, String note) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setFinalStatus(Reservation.FinalStatus.APPROVED);
        r.setStaffReviewerEmail(staffEmail);
        r.setStaffReviewedAt(OffsetDateTime.now());
        Reservation saved = reservationRepository.save(r);

        staffLogService.createLog(saved.getId(), staffEmail, StaffAction.APPROVED, note);
        return saved;
    }

    // staff rejected
    @Transactional
    public Reservation rejectReservation(Long reservationId, String staffEmail, String note) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setFinalStatus(Reservation.FinalStatus.REJECTED);
        r.setStaffReviewerEmail(staffEmail);
        r.setStaffReviewedAt(OffsetDateTime.now());
        r.setRejectReason(note);
        Reservation saved = reservationRepository.save(r);

        staffLogService.createLog(saved.getId(), staffEmail, StaffAction.REJECTED, note);
        return saved;
    }

    // staff reviewed
    @Transactional
    public Reservation reviewReservation(Long reservationId, String staffEmail, String note) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setStep(Reservation.BookingStep.STAFF_REVIEW);
        r.setStaffReviewerEmail(staffEmail);
        r.setStaffReviewedAt(OffsetDateTime.now());
        Reservation saved = reservationRepository.save(r);

        staffLogService.createLog(saved.getId(), staffEmail, StaffAction.REVIEWED, note);
        return saved;
    }

    // staff returned
    @Transactional
    public Reservation returnReservation(Long reservationId, String staffEmail, String note) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setStep(Reservation.BookingStep.RETURNED_FOR_FIX);
        r.setReturnReason(note);
        r.setStaffReviewerEmail(staffEmail);
        r.setStaffReviewedAt(OffsetDateTime.now());
        Reservation saved = reservationRepository.save(r);

        staffLogService.createLog(saved.getId(), staffEmail, StaffAction.RETURNED, note);
        return saved;
    }

    // staff cancelled
    @Transactional
    public Reservation cancelReservation(Long reservationId, String staffEmail, String note) {
        Reservation r = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        r.setFinalStatus(Reservation.FinalStatus.CANCELLED);
        r.setCancelReason(note);
        r.setHeadApproverEmail(staffEmail);
        r.setHeadDecidedAt(OffsetDateTime.now());
        Reservation saved = reservationRepository.save(r);

        staffLogService.createLog(saved.getId(), staffEmail, StaffAction.CANCELLED, note);
        return saved;
    }
}

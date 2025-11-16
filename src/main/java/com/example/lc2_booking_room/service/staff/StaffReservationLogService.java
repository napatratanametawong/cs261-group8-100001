package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.dto.staff.StaffLogResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.ReservationSlot;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.model.staff_log.StaffReservationLog;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.StaffReservationLogRepository;
import com.example.lc2_booking_room.service.notification.HeadEmailNotificationService;
import com.example.lc2_booking_room.service.notification.UserEmailNotificationService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StaffReservationLogService {

    private final StaffReservationLogRepository logRepo;
    private final ReservationRepository reservationRepo;

    // ส่งเมล + noti ให้ "หัวหน้า" ตอนที่ staff REVIEWED เสร็จ
    private final HeadEmailNotificationService headNotificationService;

    // ส่งเมล + noti ให้ "ผู้ใช้" (เจ้าของคำร้อง)
    private final UserEmailNotificationService userEmailNotificationService;

    private OffsetDateTime nowBkk() {
        return OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
    }

    /** สร้างข้อความช่วงเวลา เช่น "08:00 น.-09:30 น." เรียงจากเวลาต่ำไปสูง */
    private List<String> buildTimeRanges(Reservation reservation) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm น.");
        return reservation.getSlots().stream()
                .map(ReservationSlot::getTimeSlot)
                .filter(ts -> ts != null)
                .sorted(Comparator.comparing(ts -> ts.getStartTime()))
                .map(ts -> ts.getStartTime().format(fmt) + "-" + ts.getEndTime().format(fmt))
                .toList();
    }

    /**
     * ใช้โดย STAFF เท่านั้น
     * action ที่ถูกต้อง: REVIEWED, RETURNED
     */
    @Transactional
    public StaffLogResponse createLog(Long reservationId,
                                      String staffEmail,
                                      StaffAction action,
                                      String note) {

        // กันผิด: staff ห้ามใช้ APPROVED / REJECTED
        if (action == StaffAction.APPROVED || action == StaffAction.REJECTED) {
            throw new IllegalArgumentException("Staff cannot approve or reject reservations.");
        }

        // ----- หา reservation -----
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 🚫 กันไม่ให้ staff แก้ reservation ที่ถูกตัดสิน/ยกเลิกไปแล้ว
        if (reservation.getFinalStatus() != null
                && reservation.getFinalStatus() != Reservation.FinalStatus.PENDING) {
            throw new IllegalStateException("Reservation already finalized. Staff cannot review it again.");
        }

        // ----- อัปเดตสถานะตาม action -----
        switch (action) {
            case REVIEWED -> {
                reservation.setStep(Reservation.BookingStep.STAFF_REVIEW);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(nowBkk());

                // แจ้งหัวหน้าให้พิจารณา (ส่งเมล + บันทึก noti)
                headNotificationService.notifyHeadForReview(reservation);
            }
            case RETURNED -> {
                reservation.setStep(Reservation.BookingStep.RETURNED_FOR_FIX);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setReturnReason(note);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(nowBkk());
            }
            default -> {
                // ตอนนี้ STAFF ใช้แค่ REVIEWED / RETURNED
            }
        }

        // ----- เซฟ reservation -----
        reservationRepo.save(reservation);

        // ----- เซฟ StaffReservationLog -----
        StaffReservationLog log = new StaffReservationLog();
        log.setReservation(reservation);
        log.setStaffEmail(staffEmail);
        log.setAction(action);
        log.setNote(note);

        StaffReservationLog saved = logRepo.save(log);

        // ----- เตรียม timeRanges และแจ้ง "ผู้ใช้" -----
        List<String> timeRanges = buildTimeRanges(reservation);

        userEmailNotificationService.notifyStaffActionToUser(
                reservation,
                action,
                timeRanges,
                note
        );

        // ----- สร้าง DTO ตอบกลับ -----
        return StaffLogResponse.builder()
                .staffLogId(saved.getStaffLogId())
                .reservationId(reservation.getId())
                .staffEmail(saved.getStaffEmail())
                .action(saved.getAction())
                .changedAt(saved.getChangedAt())   // OffsetDateTime -> DTO OffsetDateTime
                .note(saved.getNote())
                .build();
    }

    // helper แปลง entity -> DTO
    private StaffLogResponse toDto(StaffReservationLog log) {
        return StaffLogResponse.builder()
                .staffLogId(log.getStaffLogId())
                .reservationId(log.getReservation().getId())
                .staffEmail(log.getStaffEmail())
                .action(log.getAction())
                .changedAt(log.getChangedAt())     // OffsetDateTime เช่นกัน
                .note(log.getNote())
                .build();
    }

    // ดึง staffLog ของ reservation เดียว
    public List<StaffLogResponse> getLogsByReservation(Long reservationId) {
        return logRepo.findByReservation_Id(reservationId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    // ดึง StaffLog ทั้งหมด
    public List<StaffLogResponse> getAllLogs() {
        return logRepo.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    /**
     * ใช้โดย HEAD เท่านั้น
     * action ที่ถูกต้อง: APPROVED, REJECTED
     */
    @Transactional
    public void logHeadDecision(Reservation reservation,
                                String headEmail,
                                StaffAction action,
                                String remark) {

        // กันผิด: head ต้องใช้เฉพาะ APPROVED / REJECTED เท่านั้น
        if (action != StaffAction.APPROVED && action != StaffAction.REJECTED) {
            throw new IllegalArgumentException("Head decision must be APPROVED or REJECTED.");
        }

        // ----- บันทึก log การตัดสินของหัวหน้า -----
        StaffReservationLog log = new StaffReservationLog();
        log.setReservation(reservation);
        log.setStaffEmail(headEmail); // ใช้ field staff_email เก็บอีเมลหัวหน้า
        log.setAction(action);
        log.setNote(remark);
        logRepo.save(log);

        // ----- เตรียม timeRanges แล้วแจ้ง "ผู้ใช้" ว่าหัวหน้าตัดสินแล้ว -----
        List<String> timeRanges = buildTimeRanges(reservation);

        userEmailNotificationService.notifyStaffActionToUser(
                reservation,
                action,   // APPROVED / REJECTED
                timeRanges,
                remark
        );
    }
}

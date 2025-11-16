package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.dto.staff.StaffLogResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.ReservationSlot;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.model.staff_log.StaffReservationLog;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.StaffReservationLogRepository;
import com.example.lc2_booking_room.service.login.EmailService;
import com.example.lc2_booking_room.model.notification.Notification;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffReservationLogService {

    private final StaffReservationLogRepository logRepo;
    private final ReservationRepository reservationRepo;
    private final EmailService emailService;
    private final com.example.lc2_booking_room.service.notification.UserInAppNotificationService userInAppNotificationService; // ✅
                                                                                                                               // เพิ่มตรงนี้

    // APPROVED, REJECTED, REVIEWED, RETURNED, CANCELLED

    @Transactional
    public StaffLogResponse createLog(Long reservationId, String staffEmail, StaffAction action, String note) {
        Reservation reservation = reservationRepo.findById(reservationId)
                .orElseThrow(() -> new RuntimeException("Reservation not found"));

        // 🚫 กันไม่ให้ staff แก้ reservation ที่ถูกตัดสิน/ยกเลิกไปแล้ว
        if (reservation.getFinalStatus() != null
                && reservation.getFinalStatus() != Reservation.FinalStatus.PENDING) {
            // APPROVED / REJECTED / CANCELLED ทั้งหมดห้าม REVIEW/RETURN แล้ว
            throw new IllegalStateException("Reservation already finalized. Staff cannot review it again.");
        }

        // action
        switch (action) {

            case REVIEWED -> {
                reservation.setStep(Reservation.BookingStep.STAFF_REVIEW);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(
                        OffsetDateTime.now(java.time.ZoneId.of("Asia/Bangkok")));
            }
            case RETURNED -> {
                reservation.setStep(Reservation.BookingStep.RETURNED_FOR_FIX);
                reservation.setFinalStatus(Reservation.FinalStatus.PENDING);
                reservation.setReturnReason(note);
                reservation.setStaffReviewerEmail(staffEmail);
                reservation.setStaffReviewedAt(
                        OffsetDateTime.now(java.time.ZoneId.of("Asia/Bangkok")));
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
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm น.");

        List<String> timeRanges = reservation.getSlots().stream()
                .map(ReservationSlot::getTimeSlot)
                .filter(ts -> ts != null)
                .map(ts -> {

                    var start = ts.getStartTime().atDate(reservation.getReservationDate())
                            .atZone(java.time.ZoneOffset.UTC)
                            .withZoneSameInstant(java.time.ZoneId.of("Asia/Bangkok"))
                            .toLocalTime();
                    var end = ts.getEndTime().atDate(reservation.getReservationDate())
                            .atZone(java.time.ZoneOffset.UTC)
                            .withZoneSameInstant(java.time.ZoneId.of("Asia/Bangkok"))
                            .toLocalTime();
                    return start.format(fmt) + "-" + end.format(fmt);
                })
                .collect(Collectors.toList());

        emailService.sendStaffActionNotice(
                reservation.getUserEmail(),
                action.name(),
                reservation.getRoomCode(),
                reservation.getReservationDate().toString(),
                timeRanges,
                note);

        // บันทึก In-App Notification ของ User (WEB)
        String title;
        String message;
        Notification.NotificationType notiType;

        switch (action) {
            case REVIEWED -> {
                title = "คำร้องของคุณได้รับการตรวจสอบแล้ว";
                message = "เจ้าหน้าที่ได้ตรวจสอบคำร้องของคุณแล้ว รอการอนุมัติจากหัวหน้าสาขา";
                notiType = Notification.NotificationType.STAFF_REVIEWED;
            }
            case RETURNED -> {
                title = "คำร้องของคุณถูกส่งกลับเพื่อแก้ไข";
                message = "เจ้าหน้าที่ส่งคำร้องกลับ โปรดตรวจสอบและแก้ไขข้อมูลก่อนส่งใหม่อีกครั้ง";
                notiType = Notification.NotificationType.STAFF_RETURNED;
            }
            default -> {
                title = "มีการอัปเดตคำร้องของคุณ";
                message = "เจ้าหน้าที่ได้เปลี่ยนสถานะคำร้องของคุณ";
                notiType = Notification.NotificationType.NEW_REQUEST;
            }
        }

        userInAppNotificationService.send(
                reservation.getUserEmail(),
                reservation.getId(),
                notiType,
                title,
                message);

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

package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.model.notification.Notification.SendStatus;
import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.example.lc2_booking_room.repository.NotificationRepository;
import com.example.lc2_booking_room.service.login.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserEmailNotificationService {

    private final EmailService emailService;
    private final UserInAppNotificationService inApp;
    private final NotificationRepository notificationRepository;

    private OffsetDateTime nowBkk() {
        return OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
    }

    /**
     * ใช้แจ้ง "ผู้ใช้" ทั้งเคสที่ staff ทำงาน (REVIEWED/RETURNED)
     * และเคสที่ head ตัดสิน (APPROVED/REJECTED)
     */
    public void notifyStaffActionToUser(
            Reservation r,
            StaffAction action,
            List<String> timeRanges,
            String note
    ) {

        // ------- รวมช่วงเวลาให้เหลืออันเดียว เช่น "08:00 น.-18:00 น." -------
        String mergedTimeRange = null;
        if (timeRanges != null && !timeRanges.isEmpty()) {
            String first = timeRanges.get(0);
            String last  = timeRanges.get(timeRanges.size() - 1);

            String start = first.split("-", 2)[0].trim(); // "08:00 น."
            String end   = last.split("-", 2)[1].trim();  // "18:00 น."

            mergedTimeRange = start + "-" + end;
        }

        List<String> timeRangesForEmail = new ArrayList<>();
        if (mergedTimeRange != null) {
            timeRangesForEmail.add(mergedTimeRange);
        }

        // -------- 1) ส่งอีเมลไปหา user พร้อมจับ status --------
        SendStatus status;
        String error = null;

        try {
            emailService.sendStaffActionNotice(
                    r.getUserEmail(),
                    action.name(),                       // REVIEWED / RETURNED / APPROVED / REJECTED
                    r.getRoomCode(),
                    r.getReservationDate().toString(),
                    timeRangesForEmail,
                    note
            );
            status = SendStatus.SUCCESS;
        } catch (Exception ex) {
            status = SendStatus.FAILED;
            error = ex.getMessage();
        }

        // -------- 2) เขียน log ลงตาราง notifications (EMAIL) --------
        String title;
        String message;
        Notification.NotificationType type;

        switch (action) {
            case REVIEWED -> {
                title = "คำร้องของคุณได้รับการตรวจสอบแล้ว";
                message = "เจ้าหน้าที่ได้ตรวจสอบคำร้องของคุณแล้ว รอการอนุมัติจากหัวหน้าสาขา";
                type = Notification.NotificationType.STAFF_REVIEWED;
            }
            case RETURNED -> {
                title = "คำร้องของคุณถูกส่งกลับเพื่อแก้ไข";
                message = "เจ้าหน้าที่ส่งคำร้องกลับ โปรดตรวจสอบและแก้ไขข้อมูลก่อนส่งใหม่อีกครั้ง";
                type = Notification.NotificationType.STAFF_RETURNED;
            }
            case APPROVED -> {
                title = "คำร้องของคุณได้รับการอนุมัติแล้ว";
                message = "หัวหน้าสาขาได้อนุมัติการจองห้องของคุณเรียบร้อยแล้ว";
                type = Notification.NotificationType.HEAD_APPROVED;
            }
            case REJECTED -> {
                title = "คำร้องของคุณไม่ได้รับการอนุมัติ";
                String extra = (note != null && !note.isBlank())
                        ? " เหตุผล: " + note
                        : "";
                message = "หัวหน้าสาขาไม่ได้อนุมัติคำร้องของคุณ." + extra;
                type = Notification.NotificationType.HEAD_REJECTED;
            }
            default -> {
                title = "มีการอัปเดตคำร้องของคุณ";
                message = "คำร้องหมายเลข #" + r.getId() + " มีการอัปเดตโดยเจ้าหน้าที่";
                type = Notification.NotificationType.NEW_REQUEST;
            }
        }

        Notification emailNoti = new Notification();
        emailNoti.setRecipientEmail(r.getUserEmail());
        emailNoti.setRecipientRole(Notification.RecipientRole.USER);
        emailNoti.setReservation(r);
        emailNoti.setNotificationType(type);
        emailNoti.setTitle(title);
        emailNoti.setMessage(message);
        emailNoti.setChannel(Notification.NotificationChannel.EMAIL);
        emailNoti.setRead(false);
        emailNoti.setDeleted(false);
        emailNoti.setCreatedAt(nowBkk());
        emailNoti.setSendStatus(status);

        if (status == SendStatus.SUCCESS) {
            emailNoti.setSentAt(nowBkk());
            emailNoti.setSendError(null);
        } else {
            emailNoti.setSentAt(null);
            emailNoti.setSendError(error);
        }

        notificationRepository.save(emailNoti);

        // -------- 3) In-App noti (WEB) ให้ user --------
        inApp.send(
                r.getUserEmail(),
                r.getId(),
                type,
                title,
                message
        );
    }
}

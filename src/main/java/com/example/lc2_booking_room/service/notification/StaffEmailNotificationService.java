package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.model.notification.Notification.SendStatus;
import com.example.lc2_booking_room.repository.NotificationRepository;
import com.example.lc2_booking_room.service.login.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StaffEmailNotificationService {

    private final EmailService emailService;
    private final StaffInAppNotificationService inApp;
    private final NotificationRepository notificationRepository;

    // อีเมลเจ้าหน้าที่ (ตอนนี้ฮาร์ดโค้ดไว้ก่อน)
    private static final String STAFF_EMAIL = "lc2.serviceadm@gmail.com";

    private OffsetDateTime nowBkk() {
        return OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
    }

    /** ===== เมื่อมีการสร้างคำขอใหม่ ===== */
    public void notifyCreated(Reservation r) {

        String subject = "มีคำขอจองห้องใหม่ NEW!!";
        String body = String.format("""
                มีคำขอจองห้องใหม่

                ผู้จอง : %s
                E-mail : %s
                Reservation ID : %s

                โปรดตรวจสอบในเว็บไซต์...
                """,
                r.getUserName(),
                r.getUserEmail(),
                r.getId()
        );

        // 1) พยายามส่งอีเมลพร้อม log send_status/sent_at
        SendStatus status;
        String error = null;
        try {
            emailService.sendStaffNotification(STAFF_EMAIL, subject, body);
            status = SendStatus.SUCCESS;
        } catch (Exception ex) {
            status = SendStatus.FAILED;
            error = ex.getMessage();
        }

        // 1.1) บันทึกแถว EMAIL ลงตาราง notifications
        Notification emailNoti = new Notification();
        emailNoti.setRecipientEmail(STAFF_EMAIL);
        emailNoti.setRecipientRole(Notification.RecipientRole.STAFF);
        emailNoti.setReservation(r);
        emailNoti.setNotificationType(Notification.NotificationType.NEW_REQUEST);
        emailNoti.setTitle("New Reservation Request");
        emailNoti.setMessage("New reservation #" + r.getId());
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

        // 2) สร้าง In-App Notification (WEB) เหมือนเดิม
        inApp.send(
                STAFF_EMAIL,
                r.getId(),
                Notification.NotificationType.NEW_REQUEST,
                "New Reservation Request",
                "New reservation #" + r.getId()
        );
    }

    /** ===== เมื่อมีการยกเลิกคำขอ ===== */
    public void notifyCanceled(Reservation r) {

        String subject = "มีคำขอยกเลิกการจองห้อง";
        String body = String.format("""
                คำร้องถูกยกเลิก

                ผู้ใช้ : %s
                E-mail : %s
                Reservation ID : %s

                โปรดตรวจสอบข้อมูลเพิ่มเติมในเว็บไซต์...
                """,
                r.getUserName(),
                r.getUserEmail(),
                r.getId()
        );

        // 1) ส่งอีเมล + log status
        SendStatus status;
        String error = null;
        try {
            emailService.sendStaffNotification(STAFF_EMAIL, subject, body);
            status = SendStatus.SUCCESS;
        } catch (Exception ex) {
            status = SendStatus.FAILED;
            error = ex.getMessage();
        }

        Notification emailNoti = new Notification();
        emailNoti.setRecipientEmail(STAFF_EMAIL);
        emailNoti.setRecipientRole(Notification.RecipientRole.STAFF);
        emailNoti.setReservation(r);
        emailNoti.setNotificationType(Notification.NotificationType.USER_CANCELLED);
        emailNoti.setTitle("Reservation Cancelled");
        emailNoti.setMessage("Reservation #" + r.getId() + " has been cancelled by user");
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

        // 2) In-App noti (WEB) เดิม
        inApp.send(
                STAFF_EMAIL,
                r.getId(),
                Notification.NotificationType.USER_CANCELLED,
                "Reservation Cancelled",
                "Reservation #" + r.getId() + " has been cancelled by user"
        );
    }
}

package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.service.login.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffEmailNotificationService {

    private final EmailService emailService;
    private final StaffInAppNotificationService inApp;

    private final String STAFF_EMAIL = "lc2.serviceadm@gmail.com"; // อีเมลเจ้าหน้าที่

    /** เมื่อมีการสร้างคำขอใหม่ */
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

        // 1) ส่ง email
        emailService.sendStaffNotification(STAFF_EMAIL, subject, body);

        // 2) สร้าง In-App Notification
        inApp.send(
                STAFF_EMAIL,
                r.getId(),
                Notification.NotificationType.NEW_REQUEST,
                "New Reservation Request",
                "New reservation #" + r.getId()
        );
    }

    /** เมื่อมีการยกเลิกคำขอ */
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

        // 1) ส่งอีเมล
        emailService.sendStaffNotification(STAFF_EMAIL, subject, body);

        // 2) ส่ง In-App Noti
        inApp.send(
                STAFF_EMAIL,
                r.getId(),
                Notification.NotificationType.USER_CANCELLED,
                "Reservation Cancelled",
                "Reservation #" + r.getId() + " has been cancelled by user"
        );
    }
}

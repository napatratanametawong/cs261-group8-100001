// src/main/java/com/example/lc2_booking_room/service/notification/HeadNotificationService.java
package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import com.example.lc2_booking_room.service.login.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class HeadNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${app.head-email}")
    private String headEmail;

    // เช่น http://localhost:8080/bookingRoom/head/head_decision.html
    @Value("${app.head-decision-url}")
    private String headDecisionUrl;

    /**
     * ใช้เรียกตอน staff กด REVIEWED สำเร็จ
     * - บันทึก notification (channel = EMAIL, recipientRole = HEAD)
     * - ส่งอีเมลให้หัวหน้าพร้อมลิงก์ไปหน้าอนุมัติ/ไม่อนุมัติ
     */
    public void notifyHeadForReview(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            return;
        }

        // ประกอบลิงก์หน้าเว็บของหัวหน้า
        String decisionLink = String.format("%s?id=%d", headDecisionUrl, reservation.getId());

        // ========== 1) บันทึก notification ลงตาราง notifications ==========
        Notification noti = new Notification();
        noti.setRecipientEmail(headEmail);
        noti.setRecipientRole(Notification.RecipientRole.HEAD);
        noti.setReservationId(reservation.getId());
        noti.setUserLogId(null); // ตอนนี้ยังไม่ใช้ user_log ก็ปล่อย null ได้
        noti.setNotificationType("HEAD_REVIEW_REQUEST");
        noti.setTitle("คำร้องใช้ห้องรอการพิจารณาจากหัวหน้าสาขา");

        String msg = String.format(
                "มีคำร้องใช้ห้องของ %s (%s)\n" +
                "ห้อง: %s\n" +
                "วันที่ใช้: %s\n\n" +
                "คลิกที่ลิงก์ด้านล่างเพื่อตรวจสอบและตัดสินคำร้อง:\n%s",
                safe(reservation.getUserName()),
                safe(reservation.getUserEmail()),
                safe(reservation.getRoomCode()),
                safe(reservation.getReservationDate()),
                decisionLink
        );
        noti.setMessage(msg);
        noti.setChannel(Notification.NotificationChannel.EMAIL);
        noti.setRead(false);
        noti.setDeleted(false);
        noti.setCreatedAt(OffsetDateTime.now(ZoneId.of("Asia/Bangkok")));
        noti.setReadAt(null);
        noti.setSentAt(null); // ถ้าอยาก update หลังส่งเมลสำเร็จ ค่อยมาเซ็ตเพิ่มทีหลัง

        notificationRepository.save(noti);

        // ========== 2) ส่งอีเมลจริงให้หัวหน้าสาขา ==========
        String subject = "[LC2 Booking] คำร้องใช้ห้องรอการพิจารณา";

        // 🔴 ถ้า EmailService ไม่มีเมธอดนี้ ให้เปลี่ยนชื่อเมธอดให้ตรงของจริง
        emailService.sendSimpleMail(headEmail, subject, msg);
    }

    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }
}

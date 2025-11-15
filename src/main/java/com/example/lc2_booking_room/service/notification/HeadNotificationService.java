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
import java.util.List;

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
     * เรียกตอน staff กด REVIEWED:
     * - บันทึกแถวใน notifications (recipientRole = HEAD, channel = EMAIL)
     * - ส่งอีเมลให้หัวหน้าพร้อมลิงก์ไปหน้าอนุมัติ
     */
    public void notifyHeadForReview(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) {
            return;
        }

        String decisionLink = String.format("%s?id=%d", headDecisionUrl, reservation.getId());

        Notification noti = new Notification();
        noti.setRecipientEmail(headEmail);
        noti.setRecipientRole(Notification.RecipientRole.HEAD);

        // ✅ ผูกกับ reservation ปัจจุบัน (column reservation_id)
        noti.setReservation(reservation);

        // ✅ ต้องเซ็ต ไม่งั้น NOT NULL constraint ระเบิด
        // ถ้าคุณมี enum อื่นเช่น HEAD_REVIEW_REQUEST ให้เปลี่ยนบรรทัดนี้ได้
        noti.setNotificationType(Notification.NotificationType.NEW_REQUEST);

        noti.setTitle("คำร้องใช้ห้องรอการพิจารณาจากหัวหน้าสาขา");

        String msg = String.format(
                "มีคำร้องใช้ห้องของ %s (%s)\n" +
                        "ห้อง: %s\n" +
                        "วันที่ใช้: %s\n\n" +
                        "คลิกลิงก์นี้เพื่อตรวจสอบและตัดสินคำร้อง:\n%s",
                safe(reservation.getUserName()),
                safe(reservation.getUserEmail()),
                safe(reservation.getRoomCode()),
                safe(reservation.getReservationDate()),
                decisionLink);
        noti.setMessage(msg);
        noti.setChannel(Notification.NotificationChannel.EMAIL);
        noti.setRead(false);
        noti.setDeleted(false);
        noti.setCreatedAt(OffsetDateTime.now(ZoneId.of("Asia/Bangkok")));
        noti.setReadAt(null);
        noti.setSentAt(null);

        notificationRepository.save(noti);

        // ส่งเมลให้หัวหน้าตามที่เคยเขียนไว้
        String dateStr = reservation.getReservationDate() != null
                ? reservation.getReservationDate().toString()
                : "-";

        List<String> timeRanges = List.of("ดูรายละเอียดคำร้องและตัดสินได้ที่:", decisionLink);

        emailService.sendStaffActionNotice(
                headEmail,
                "HEAD_REVIEW_REQUEST",
                safe(reservation.getRoomCode()),
                dateStr,
                timeRanges,
                msg);
    }

    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }
}

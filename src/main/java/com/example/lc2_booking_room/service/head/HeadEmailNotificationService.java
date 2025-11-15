package com.example.lc2_booking_room.service.head;

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
public class HeadEmailNotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Value("${app.head-email}")
    private String headEmail;

    @Value("${app.head-decision-url}")
    private String headDecisionUrl;

    public void notifyHeadForReview(Reservation reservation) {
        if (reservation == null || reservation.getId() == null) return;

        String decisionLink = String.format("%s?id=%d", headDecisionUrl, reservation.getId());

        Notification noti = new Notification();
        noti.setRecipientEmail(headEmail);
        noti.setRecipientRole(Notification.RecipientRole.HEAD);
        noti.setReservation(reservation);
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
                decisionLink
        );
        noti.setMessage(msg);
        noti.setChannel(Notification.NotificationChannel.EMAIL);
        noti.setRead(false);
        noti.setDeleted(false);
        noti.setCreatedAt(nowBkk());

        // ✅ เริ่มต้นเป็น PENDING (enum)
        noti.setSendStatus(Notification.SendStatus.PENDING);

        // save ก่อนส่งเมล
        noti = notificationRepository.save(noti);

        String dateStr = reservation.getReservationDate() != null
                ? reservation.getReservationDate().toString()
                : "-";
        List<String> timeRanges = List.of("ดูรายละเอียดคำร้องและตัดสินได้ที่:", decisionLink);

        try {
            emailService.sendStaffActionNotice(
                    headEmail,
                    "HEAD_REVIEW_REQUEST",
                    safe(reservation.getRoomCode()),
                    dateStr,
                    timeRanges,
                    msg
            );

            // ✅ ส่งสำเร็จ
            noti.setSendStatus(Notification.SendStatus.SUCCESS);
            noti.setSentAt(nowBkk());
            noti.setSendError(null);
        } catch (Exception ex) {
            // ❌ ส่งล้มเหลว
            noti.setSendStatus(Notification.SendStatus.FAILED);
            noti.setSendError(ex.getMessage());
        }

        notificationRepository.save(noti);
    }

    private OffsetDateTime nowBkk() {
        return OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
    }

    private String safe(Object o) {
        return o == null ? "-" : o.toString();
    }
}

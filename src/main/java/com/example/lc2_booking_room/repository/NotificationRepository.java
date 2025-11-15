package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ====== ใช้ฝั่งหน้าเว็บ (staff/user) ดึงกล่องแจ้งเตือน ======

    // noti ตามอีเมล + role + ช่องทาง (WEB/EMAIL) ที่ยังไม่ถูกลบ
    List<Notification> findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
            String recipientEmail,
            Notification.RecipientRole recipientRole,
            Notification.NotificationChannel channel
    );

    // เวอร์ชันเฉพาะ "ยังไม่อ่าน"
    List<Notification> findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseAndReadFalseOrderByCreatedAtDesc(
            String recipientEmail,
            Notification.RecipientRole recipientRole,
            Notification.NotificationChannel channel
    );

    // ====== ใช้ตอนจะอัปเดต send_status / sent_at ของ noti เดิม ======
    // ดึง noti ล่าสุดของ STAFF สำหรับ reservationId นั้น + ประเภทแจ้งเตือนที่ต้องการ + channel
    Optional<Notification> findTopByRecipientRoleAndReservation_IdAndNotificationTypeAndChannelOrderByCreatedAtDesc(
            Notification.RecipientRole recipientRole,
            Long reservationId,
            Notification.NotificationType notificationType,
            Notification.NotificationChannel channel
    );
}

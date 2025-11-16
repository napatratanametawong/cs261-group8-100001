package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import com.example.lc2_booking_room.service.login.EmailService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final EmailService emailService;

    /** ดึง noti ทั้งหมดของ staff ที่ล็อกอินอยู่ (เฉพาะ WEB, ไม่ถูกลบ) */
    public List<Notification> getStaffInbox(String email) {
        return repo.findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
                email,
                Notification.RecipientRole.STAFF,
                Notification.NotificationChannel.WEB);
    }

    /** กดอ่าน noti */
    public Notification markRead(Long id, String email) {
        Notification n = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        // กันเผื่ออนาคต: เช็ค owner ด้วย
        if (!n.getRecipientEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("You are not owner of this notification");
        }

        n.setRead(true);
        n.setReadAt(OffsetDateTime.now());
        return repo.save(n);
    }

    public void createUserNotification(
            String toEmail,
            Notification.NotificationType type,
            String title,
            String message,
            String action,
            String roomCode,
            String date,
            List<String> timeRanges,
            String note,
            Reservation reservation) {
        // 1️⃣ บันทึก Notification สำหรับ WEB
        Notification webNoti = Notification.builder()
                .recipientEmail(toEmail)
                .recipientRole(Notification.RecipientRole.USER)
                .notificationType(type)
                .title(title)
                .message(message)
                .channel(Notification.NotificationChannel.WEB)
                .read(false)
                .deleted(false)
                .createdAt(OffsetDateTime.now())
                .reservation(reservation)
                .build();
        repo.save(webNoti);

        // 2️⃣ ส่งอีเมลด้วย EmailService เดิม
        try {
            emailService.sendStaffActionNotice(toEmail, action, roomCode, date, timeRanges, note);

            // 3️⃣ บันทึก Notification สำหรับ EMAIL (ส่งสำเร็จ)
            Notification emailNoti = Notification.builder()
                    .recipientEmail(toEmail)
                    .recipientRole(Notification.RecipientRole.USER)
                    .notificationType(type)
                    .title(title)
                    .message(message)
                    .channel(Notification.NotificationChannel.EMAIL)
                    .read(false)
                    .deleted(false)
                    .createdAt(OffsetDateTime.now())
                    .sendStatus(Notification.SendStatus.SUCCESS)
                    .sentAt(OffsetDateTime.now())
                    .reservation(reservation)
                    .build();
            repo.save(emailNoti);
        } catch (Exception e) {
            // ถ้าส่งอีเมลล้มเหลว
            Notification failedEmail = Notification.builder()
                    .recipientEmail(toEmail)
                    .recipientRole(Notification.RecipientRole.USER)
                    .notificationType(type)
                    .title(title)
                    .message(message)
                    .channel(Notification.NotificationChannel.EMAIL)
                    .read(false)
                    .deleted(false)
                    .createdAt(OffsetDateTime.now())
                    .sendStatus(Notification.SendStatus.FAILED)
                    .sendError(e.getMessage())
                    .reservation(reservation)
                    .build();
            repo.save(failedEmail);
        }
    }

    public List<Notification> getUserInbox(String email) {
        return repo.findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
                email,
                Notification.RecipientRole.USER,
                Notification.NotificationChannel.WEB);
    }

}

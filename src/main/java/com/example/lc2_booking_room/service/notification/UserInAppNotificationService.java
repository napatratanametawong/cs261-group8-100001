package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserInAppNotificationService {

    private final NotificationRepository repo;

    // เวลาโซน Bangkok
    private OffsetDateTime nowBkk() {
        return OffsetDateTime.now(ZoneId.of("Asia/Bangkok"));
    }

    // ====== กล่องแจ้งเตือนของ USER ======
    public List<Notification> getUserInbox(String email) {
        if (email == null) {
            return Collections.emptyList();
        }

        return repo.findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
                email,
                Notification.RecipientRole.USER,
                Notification.NotificationChannel.WEB
        );
    }

    // USER กดอ่าน noti
    public Notification markRead(Long id, String email) {
        Notification noti = repo.findByIdAndRecipientEmailAndRecipientRole(
                        id,
                        email,
                        Notification.RecipientRole.USER
                )
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        if (!noti.isRead()) {
            noti.setRead(true);
            noti.setReadAt(nowBkk());
            repo.save(noti);
        }
        return noti;
    }

    // ====== ส่ง in-app noti ให้ USER ======
    public void send(
            String userEmail,
            Long reservationId,
            Notification.NotificationType type,
            String title,
            String message
    ) {
        Notification n = Notification.builder()
                .recipientEmail(userEmail)
                .recipientRole(Notification.RecipientRole.USER)
                .reservation(Reservation.builder().id(reservationId).build())
                .notificationType(type)
                .title(title)
                .message(message)
                .channel(Notification.NotificationChannel.WEB)
                .read(false)
                .deleted(false)
                .createdAt(nowBkk())
                .sendStatus(Notification.SendStatus.SUCCESS)
                .sentAt(nowBkk())
                .build();

        repo.save(n);
    }
}

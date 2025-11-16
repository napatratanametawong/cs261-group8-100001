package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserInAppNotificationService {

    private final NotificationRepository repo;

    /**
     * สร้าง notification สำหรับผู้ใช้ (USER)
     * channel = WEB, recipientRole = USER
     */
    public void send(
            String userEmail,
            Long reservationId,
            Notification.NotificationType type,
            String title,
            String message) {
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
                .createdAt(OffsetDateTime.now())
                .build();

        repo.save(n);
    }
}

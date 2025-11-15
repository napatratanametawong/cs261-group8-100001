package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class StaffInAppNotificationService {

    private final NotificationRepository repo;

    public void send(
            String staffEmail,
            Long reservationId,
            Notification.NotificationType type,
            String title,
            String message
    ) {
        Notification n = Notification.builder()
                .recipientEmail(staffEmail)
                .recipientRole(Notification.RecipientRole.STAFF)
                .reservation(Reservation.builder().id(reservationId).build())
                .notificationType(type)
                .title(title)
                .message(message)
                .channel(Notification.NotificationChannel.WEB)
                .read(false)
                .createdAt(OffsetDateTime.now())
                .build();

        repo.save(n);
    }
}

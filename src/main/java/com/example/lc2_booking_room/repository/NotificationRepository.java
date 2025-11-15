package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    
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
}

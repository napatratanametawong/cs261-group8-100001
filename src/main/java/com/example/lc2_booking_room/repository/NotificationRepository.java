package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // inbox staff: email + role + channel = WEB + is_deleted = 0, sort by created_at desc
    List<Notification> findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
            String email,
            Notification.RecipientRole recipientRole,
            Notification.NotificationChannel channel
    );
}

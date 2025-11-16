package com.example.lc2_booking_room.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.lc2_booking_room.model.notification.Notification;

import java.util.List;

public interface StaffNotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientEmailOrderByCreatedAtDesc(String email);

}

package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.staff_log.StaffNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffNotificationRepository extends JpaRepository<StaffNotification, Long> {

    List<StaffNotification> findByStaffEmailOrderByCreatedAtDesc(String email);
}


// src/main/java/com/example/lc2_booking_room/repository/UserReservationLogRepository.java
package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.user_log.UserReservationLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserReservationLogRepository extends JpaRepository<UserReservationLog, Long> {
    Page<UserReservationLog> findByReservation_IdOrderByChangedAtDesc(Long id, Pageable pageable);
    Page<UserReservationLog> findByUserEmailOrderByChangedAtDesc(String userEmail, Pageable pageable);
}
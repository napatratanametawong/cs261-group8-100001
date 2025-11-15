package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.staff_log.StaffReservationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StaffReservationLogRepository extends JpaRepository<StaffReservationLog, Long> {
    List<StaffReservationLog> findByReservation_Id(Long reservationId);

}

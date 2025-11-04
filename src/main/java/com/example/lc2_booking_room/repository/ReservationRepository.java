// repository/ReservationRepository.java
package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ดึงใบจองของห้องในวันหนึ่ง ๆ
    Optional<Reservation> findByRoomCodeAndReservationDate(String roomCode, LocalDate reservationDate);

    boolean existsByRoomCodeAndReservationDate(String roomCode, LocalDate reservationDate);
}

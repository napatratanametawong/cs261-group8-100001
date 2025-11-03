// repository/ReservationRepository.java
package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // ดึง slot_id ที่ถูกจองแล้วในวัน/ห้องนั้น
    @Query("""
       select r.slot.slotCode
       from Reservation r
       where r.room.code = :roomCode
         and r.date = :date
         and r.slot.slotCode in :slotCodes
         and (r.status is null or r.status <> 'CANCELLED')
    """)
    List<String> findConflictedSlotIds(String roomCode, LocalDate date, List<String> slotCodes);
}

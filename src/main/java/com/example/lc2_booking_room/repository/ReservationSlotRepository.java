package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.ReservationSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {
    @Query("""
    SELECT rs.slotId FROM ReservationSlot rs
    WHERE rs.roomId = :roomId AND rs.date = :date AND rs.isActive = true
        AND rs.slotId IN (:slotIds)
    """)
    List<Long> findConflictedSlotIds(Long roomId, java.time.LocalDate date, java.util.List<Long> slotIds);
}

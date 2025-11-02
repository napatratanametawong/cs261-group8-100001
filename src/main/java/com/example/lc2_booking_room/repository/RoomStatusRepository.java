package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Room;  
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomStatusRepository extends JpaRepository<Room, Long> {
    @Query(value = """
        SELECT 
          r.room_id,
          r.code,
          r.room_name,
          r.room_type,
          r.min_capacity,
          r.max_capacity,
          r.features_json,
          t.slot_code,
          CASE WHEN res.reservation_id IS NULL THEN 'Available' ELSE 'Booked' END AS room_status
        FROM rooms r
        CROSS JOIN time_slots t
        LEFT JOIN reservations res
          ON res.room_id = r.room_id
         AND res.slot_id = t.slot_id
         AND res.date = :date
        WHERE r.active = 1
        ORDER BY r.code, t.start_time
        """, nativeQuery = true)
    List<Object[]> findRoomSlotStatuses(@Param("date") LocalDate date);
}

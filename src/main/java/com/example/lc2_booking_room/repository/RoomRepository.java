package com.example.lc2_booking_room.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

import com.example.lc2_booking_room.model.Room;
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {

    @Query(value = """
        SELECT r.room_name, t.slot_code,
               CASE WHEN res.reservation_id IS NULL THEN 'Available'
                    ELSE 'Booked'
               END AS room_status
        FROM rooms r
        CROSS JOIN time_slots t
        LEFT JOIN reservations res 
            ON r.room_id = res.room_id 
            AND t.slot_id = res.slot_id 
            AND res.date = :date
        ORDER BY r.room_name, t.start_time
        """, nativeQuery = true)
    List<Map<String, Object>> getRoomStatuses(@Param("date") String date);
}
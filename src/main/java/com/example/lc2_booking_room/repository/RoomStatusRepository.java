package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomStatusRepository extends JpaRepository<Room, String> {

    // ห้องที่ active ทั้งหมด
    @Query("SELECT r FROM Room r WHERE r.active = true ORDER BY r.code")
    List<Room> findActiveRooms();

    // slot ทั้งหมด (เรียงโดยเวลา)
    @Query("SELECT t FROM TimeSlot t ORDER BY t.startTime ASC, t.endTime ASC")
    List<TimeSlot> findAllOrderedSlots();

    // คู่ (room_code, slot_code) ที่ถูกจองในวันนั้น (ไม่เอา CANCELLED และเฉพาะ rs.is_active=1)
    @Query(value = """
        SELECT rs.room_code, rs.slot_code
        FROM dbo.reservations r
        JOIN dbo.reservation_slots rs
          ON rs.reservation_id = r.reservation_id
         AND rs.is_active = 1
        WHERE r.reservation_date = :d
          AND (r.final_status IS NULL OR r.final_status <> 'CANCELLED')
        """, nativeQuery = true)
    List<Object[]> findBookedPairs(@Param("d") LocalDate date);
}

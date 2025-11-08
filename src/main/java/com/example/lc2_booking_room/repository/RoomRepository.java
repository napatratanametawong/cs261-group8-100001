package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface RoomRepository extends JpaRepository<Room, String> { // <-- PK เป็น String

    Optional<Room> findByCode(String code);
    boolean existsByCode(String code);

    // ถ้ามีฟิลด์ active (ตาม ERD)
    List<Room> findByActiveTrue();
    boolean existsByCodeAndActiveTrue(String code);
}

package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {
  Optional<Room> findByCode(String code);
  boolean existsByCode(String code);
}

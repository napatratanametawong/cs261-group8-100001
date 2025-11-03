// repository/RoomRepository.java
package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, String> {}

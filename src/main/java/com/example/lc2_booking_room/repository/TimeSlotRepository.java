// repository/TimeSlotRepository.java
package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.TimeSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TimeSlotRepository extends JpaRepository<TimeSlot, String> {
    List<TimeSlot> findBySlotCodeIn(List<String> slotCodes);
}


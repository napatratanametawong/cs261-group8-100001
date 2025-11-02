package com.example.lc2_booking_room.service.room;

import com.example.lc2_booking_room.dto.room.RoomWithSlotsDTO;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.model.TimeSlot;
import com.example.lc2_booking_room.repository.RoomStatusRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.*;

@Service
public class RoomStatusService {

    private final RoomStatusRepository repo;

    public RoomStatusService(RoomStatusRepository repo) {
        this.repo = repo;
    }

    public List<RoomWithSlotsDTO> getStatusFor(LocalDate date) {
        List<Room> rooms = repo.findActiveRooms();
        List<TimeSlot> slots = repo.findAllOrderedSlots();

        List<Object[]> pairs = repo.findBookedPairs(date);
        Map<String, Set<String>> bookedByRoom = new HashMap<>();
        for (Object[] row : pairs) {
            String roomCode = Objects.toString(row[0], "");
            String slotCode = Objects.toString(row[1], "");
            if (!roomCode.isEmpty() && !slotCode.isEmpty()) {
                bookedByRoom.computeIfAbsent(roomCode, k -> new HashSet<>()).add(slotCode);
            }
        }

        OffsetDateTime now = OffsetDateTime.now();
        List<RoomWithSlotsDTO> out = new ArrayList<>(rooms.size());

        for (Room r : rooms) {
            Map<String, String> slotStatus = new LinkedHashMap<>();
            Set<String> bookedForThisRoom = bookedByRoom.getOrDefault(r.getCode(), Collections.emptySet());

            for (TimeSlot s : slots) {
                boolean isBooked = bookedForThisRoom.contains(s.getSlotCode());
                slotStatus.put(s.getSlotCode(), isBooked ? "booked" : "available");
            }

            // ใช้ค่าที่ converter แปลงให้แล้ว
            List<String> features = r.getFeatures() != null ? r.getFeatures() : Collections.emptyList();

            RoomWithSlotsDTO dto = new RoomWithSlotsDTO(
                    r.getCode(),
                    r.getRoomName(),
                    r.getRoomType(),
                    r.getMinCapacity(),
                    r.getMaxCapacity(),
                    features,
                    slotStatus,
                    now
            );
            out.add(dto);
        }

        return out;
    }
}

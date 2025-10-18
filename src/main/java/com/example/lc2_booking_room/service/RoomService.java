package com.example.lc2_booking_room.service;

import com.example.lc2_booking_room.dto.room.CreateRoomRequest;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.RoomRepositoryCustom;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class RoomService {

    private final RoomRepository repo;

    @Autowired
    private RoomRepositoryCustom roomRepositoryCustom;

    @Autowired
    public RoomService(RoomRepository repo) {
        this.repo = repo;
    }

    // ✅ list rooms (optionally filter by active)
    public List<Room> listAll(Boolean onlyActive) {
        var rooms = repo.findAll();
        if (onlyActive == null) return rooms;
        return rooms.stream()
                .filter(r -> r.isActive() == onlyActive)
                .toList();
    }

    // ✅ create/update room
    @Transactional
    public Room upsert(Room incoming) {
        var existing = repo.findByCode(incoming.getCode()).orElse(null);
        if (existing == null) {
            return repo.save(incoming);
        }

        existing.setRoomName(incoming.getRoomName());
        existing.setRoomType(incoming.getRoomType());
        existing.setMinCapacity(incoming.getMinCapacity());
        existing.setMaxCapacity(incoming.getMaxCapacity());
        existing.setFeatures(incoming.getFeatures());
        existing.setActive(incoming.isActive());
        return repo.save(existing);
    }

    // ✅ deactivate
    @Transactional
    public void deactivate(String code) {
        var r = repo.findByCode(code).orElseThrow();
        r.setActive(false);
        repo.save(r);
    }

    // ✅ get room status (new feature)
    public List<Map<String, Object>> getRoomStatus(String date) {
        return roomRepositoryCustom.getRoomStatus(date);
    }

    // ✅ create room from request DTO
    public Boolean createRoom(CreateRoomRequest request) {
        Room newRoom = new Room();
        newRoom.setCode("TEMP-" + System.currentTimeMillis()); // assign temporary unique code
        newRoom.setRoomName(request.getRoomName());
        newRoom.setRoomType(request.getRoomType());
        newRoom.setMinCapacity(request.getMinCapacity());
        newRoom.setMaxCapacity(request.getMaxCapacity());
        newRoom.setFeatures(request.getFeatures());
        newRoom.setActive(true);
        repo.save(newRoom);
        return true;
    }
}

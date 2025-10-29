package com.example.lc2_booking_room.service.room;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.RoomRepositoryCustom;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.dto.room.CreateRoomRequest;

@Service
public class RoomService {
    private final RoomRepositoryCustom roomRepositoryCustom;
    private final RoomRepository roomRepository;

    public RoomService(
        @Qualifier("roomRepositoryCustomImpl") RoomRepositoryCustom roomRepositoryCustom,
        RoomRepository roomRepository
    ) {
        this.roomRepositoryCustom = roomRepositoryCustom;
        this.roomRepository = roomRepository;
    }

    public List<Map<String, Object>> getRoomStatus(String date) {
        return roomRepositoryCustom.getRoomStatuses(date);
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return rooms;
    }

   public Boolean createRoom(CreateRoomRequest request) {
    try {
        Room newRoom = new Room();
        newRoom.setRoomName(request.getRoomName());
        newRoom.setRoomType(request.getRoomType());
        newRoom.setMinCapacity(request.getMinCapacity());
        newRoom.setMaxCapacity(request.getMaxCapacity());

        newRoom.setActive(true);
        roomRepository.save(newRoom);
        return true;

    } catch (Exception e) {
        e.printStackTrace();
        return false;
        }
    }
}
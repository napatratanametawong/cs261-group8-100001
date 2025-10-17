package com.example.lc2_booking_room.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.RoomRepositoryCustom;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.dto.room.CreateRoomRequest;

@Service
public class RoomService {

    @Autowired
    private RoomRepositoryCustom roomRepositoryCustom;
    @Autowired
    private RoomRepository roomRepository;

    public List<Map<String, Object>> getRoomStatus(String date) {
        return roomRepositoryCustom.getRoomStatuses(date);
    }

    public List<Room> getAllRooms() {
        List<Room> rooms = roomRepository.findAll();
        return rooms;
    }

    public Boolean createRoom(CreateRoomRequest request) {
        Room newRoom = new Room();

    newRoom.setRoomName(request.getRoomName());
    newRoom.setRoomType(request.getRoomType());
    newRoom.setMinCapacity(request.getMinCapacity());
    newRoom.setMaxCapacity(request.getMaxCapacity());
    request.getFeaturesJson();


        newRoom.setActive(true);

        roomRepository.save(newRoom);
        return true;
    }

}
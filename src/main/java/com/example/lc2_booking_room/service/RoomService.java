package com.example.lc2_booking_room.service;

import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.repository.RoomRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomService {
  private final RoomRepository repo;

  public RoomService(RoomRepository repo) { this.repo = repo; }

  public List<Room> listAll(Boolean onlyActive) {
    var rooms = repo.findAll();
    if (onlyActive == null) return rooms;
    return rooms.stream()
        .filter(r -> Boolean.TRUE.equals(r.isActive()) == Boolean.TRUE.equals(onlyActive))
        .toList();
  }

  @Transactional
  public Room upsert(Room incoming) {
    var existing = repo.findByCode(incoming.getCode()).orElse(null);
    if (existing == null) {
      // create new
      return repo.save(incoming);
    }

    // update only fields that exist in your model
    existing.setRoomName(incoming.getRoomName());
    existing.setRoomType(incoming.getRoomType());
    existing.setMinCapacity(incoming.getMinCapacity());
    existing.setMaxCapacity(incoming.getMaxCapacity());
    existing.setFeatures(incoming.getFeatures()); // List<String> via FeaturesConverter
    if (incoming.isActive() != existing.isActive()) {
      existing.setActive(incoming.isActive());
    }

    return repo.save(existing);
  }

  @Transactional
  public void deactivate(String code) {
    var r = repo.findByCode(code).orElseThrow();
    r.setActive(false);
    repo.save(r);
  }
}

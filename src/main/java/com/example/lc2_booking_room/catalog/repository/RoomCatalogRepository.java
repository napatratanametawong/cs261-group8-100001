package com.example.lc2_booking_room.catalog.repository;

import com.example.lc2_booking_room.catalog.model.RoomCatalog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomCatalogRepository extends JpaRepository<RoomCatalog, Long> {
    Optional<RoomCatalog> findByCode(String code);
    boolean existsByCode(String code);
    void deleteByCode(String code);

    List<RoomCatalog> findByActiveTrue();
    List<RoomCatalog> findByRoomTypeIgnoreCase(String roomType);
}

package com.example.lc2_booking_room.catalog.mapper;

import com.example.lc2_booking_room.catalog.dto.RoomRequest;
import com.example.lc2_booking_room.catalog.dto.RoomResponse;
import com.example.lc2_booking_room.catalog.model.RoomCatalog;

public class RoomCatalogMapper {

    public static RoomCatalog toEntity(RoomRequest req) {
        return new RoomCatalog(
            req.getCode(),
            req.getName(),
            req.getType(),
            req.getMinCapacity(),
            req.getMaxCapacity(),
            req.getFeatures(),
            req.isActive()
        );
    }

    public static void updateEntity(RoomCatalog entity, RoomRequest req) {
        entity.setCode(req.getCode());
        entity.setRoomName(req.getName());
        entity.setRoomType(req.getType());
        entity.setMinCapacity(req.getMinCapacity());
        entity.setMaxCapacity(req.getMaxCapacity());
        entity.setFeatures(req.getFeatures());
        entity.setActive(req.isActive());
    }

    public static RoomResponse toResponse(RoomCatalog r) {
        return new RoomResponse(
            r.getCode(),
            r.getRoomName(),
            r.getRoomType(),
            r.getMinCapacity(),
            r.getMaxCapacity(),
            r.getFeatures(),
            r.isActive()
        );
    }
}

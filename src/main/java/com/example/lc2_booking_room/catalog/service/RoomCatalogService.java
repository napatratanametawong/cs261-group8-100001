package com.example.lc2_booking_room.catalog.service;

import com.example.lc2_booking_room.catalog.dto.RoomRequest;
import com.example.lc2_booking_room.catalog.dto.RoomResponse;
import com.example.lc2_booking_room.catalog.mapper.RoomCatalogMapper;
import com.example.lc2_booking_room.catalog.model.RoomCatalog;
import com.example.lc2_booking_room.catalog.repository.RoomCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class RoomCatalogService {

    private final RoomCatalogRepository repo;

    public RoomCatalogService(RoomCatalogRepository repo) {
        this.repo = repo;
    }

    @Transactional
    public RoomResponse create(RoomRequest req) {
        if (repo.existsByCode(req.getCode())) {
            throw new IllegalArgumentException("Room code already exists: " + req.getCode());
        }
        if (req.getMinCapacity() > req.getMaxCapacity()) {
            throw new IllegalArgumentException("minCapacity must be <= maxCapacity");
        }
        RoomCatalog saved = repo.save(RoomCatalogMapper.toEntity(req));
        return RoomCatalogMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public RoomResponse getByCode(String code) {
        RoomCatalog r = repo.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Room not found: " + code));
        return RoomCatalogMapper.toResponse(r);
    }

    @Transactional(readOnly = true)
    public List<RoomResponse> list(Boolean active) {
        List<RoomCatalog> rooms = (active != null && active) ? repo.findByActiveTrue() : repo.findAll();
        return rooms.stream().map(RoomCatalogMapper::toResponse).toList();
    }

    @Transactional
    public RoomResponse update(String code, RoomRequest req) {
        RoomCatalog r = repo.findByCode(code).orElseThrow(() -> new IllegalArgumentException("Room not found: " + code));
        if (req.getMinCapacity() > req.getMaxCapacity()) {
            throw new IllegalArgumentException("minCapacity must be <= maxCapacity");
        }
        RoomCatalogMapper.updateEntity(r, req);
        return RoomCatalogMapper.toResponse(repo.save(r));
    }

    @Transactional
    public void delete(String code) {
        if (!repo.existsByCode(code)) throw new IllegalArgumentException("Room not found: " + code);
        repo.deleteByCode(code);
    }
}

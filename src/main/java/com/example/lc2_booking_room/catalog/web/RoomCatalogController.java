package com.example.lc2_booking_room.catalog.web;

import com.example.lc2_booking_room.catalog.dto.RoomRequest;
import com.example.lc2_booking_room.catalog.dto.RoomResponse;
import com.example.lc2_booking_room.catalog.service.RoomCatalogService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/room-catalog") // NEW base path so no conflict with /api/rooms/status
public class RoomCatalogController {

    private final RoomCatalogService service;
    public RoomCatalogController(RoomCatalogService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest req) {
        return service.create(req);
    }

    @GetMapping("/{code}")
    public RoomResponse get(@PathVariable String code) {
        return service.getByCode(code);
    }

    @GetMapping
    public List<RoomResponse> list(@RequestParam(required = false) Boolean active) {
        return service.list(active);
    }

    @PutMapping("/{code}")
    public RoomResponse update(@PathVariable String code, @Valid @RequestBody RoomRequest req) {
        req.setCode(code);
        return service.update(code, req);
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String code) {
        service.delete(code);
    }
}

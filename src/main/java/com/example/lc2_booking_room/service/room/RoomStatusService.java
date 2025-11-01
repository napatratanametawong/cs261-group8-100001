package com.example.lc2_booking_room.service.room;

import com.example.lc2_booking_room.dto.room.RoomWithSlotsDTO;
import com.example.lc2_booking_room.repository.RoomStatusRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class RoomStatusService {

    private final RoomStatusRepository repo;
    private final ObjectMapper objectMapper;

    private static final ZoneId BANGKOK = ZoneId.of("Asia/Bangkok");
    private static final DateTimeFormatter ISO_OFFSET = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public RoomStatusService(RoomStatusRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper != null ? objectMapper : new ObjectMapper();
    }
    public List<RoomWithSlotsDTO> getRoomsWithStatus(LocalDate date) {
        List<Object[]> rows = repo.findRoomSlotStatuses(date);

        // Keep insertion order (rooms sorted by code in query)
        Map<Long, RoomWithSlotsDTO> map = new LinkedHashMap<>();

        // One timestamp per response, Bangkok time, ISO with offset
        String generatedAt = ZonedDateTime.now(BANGKOK).format(ISO_OFFSET);

        for (Object[] r : rows) {
            Long roomId       = toLong(r[0]);
            String code       = str(r[1]);
            String roomName   = str(r[2]);
            String roomType   = str(r[3]);
            Integer minCap    = toInt(r[4]);
            Integer maxCap    = toInt(r[5]);
            String featuresJs = str(r[6]);
            String slotCode   = str(r[7]);
            String status     = str(r[8]);

            RoomWithSlotsDTO dto = map.get(roomId);
            if (dto == null) {
                List<String> features = parseFeatures(featuresJs);
                dto = new RoomWithSlotsDTO(
                        code,
                        roomName,
                        roomType,
                        minCap,
                        maxCap,
                        features,
                        new LinkedHashMap<>(), // slotCode -> status
                        generatedAt
                );
                map.put(roomId, dto);
            }

            if (slotCode != null && !slotCode.isBlank()) {
                dto.getSlots().put(slotCode, status);
            }
        }

        return new ArrayList<>(map.values());
    }

    // ---------- helpers ----------

    private static String str(Object o) {
        return (o == null) ? null : String.valueOf(o);
    }

    private static Integer toInt(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.intValue();
        try { return Integer.parseInt(o.toString()); } catch (Exception e) { return null; }
    }

    private static Long toLong(Object o) {
        if (o == null) return null;
        if (o instanceof Number n) return n.longValue();
        try { return Long.parseLong(o.toString()); } catch (Exception e) { return null; }
    }

    private List<String> parseFeatures(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            return List.of(); // fail-soft
        }
    }
}

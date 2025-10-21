package com.example.lc2_booking_room.repository;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

@Repository("roomRepositoryCustomImpl")
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    private final NamedParameterJdbcTemplate npJdbc;

    public RoomRepositoryCustomImpl(NamedParameterJdbcTemplate npJdbc) {
        this.npJdbc = npJdbc;
    }

    private static final String SQL = """
            SELECT
                r.code       AS roomCode,
                r.room_name  AS roomName,
                ts.slot_code AS slotCode,
                CASE WHEN b.reservation_id IS NOT NULL THEN 'Booked' ELSE 'Available' END AS roomStatus
            FROM dbo.rooms r
            CROSS JOIN dbo.time_slots ts
            LEFT JOIN dbo.reservations b
                   ON b.room_id = r.room_id
                  AND b.slot_id = ts.slot_id
                  AND CAST(b.[date] AS date) = CAST(:date AS date)
            WHERE r.active = 1
            ORDER BY r.code, ts.start_time
            """;
    @Override
    public List<Map<String, Object>> getRoomStatuses(String dateIso) {
        var effective = (dateIso == null || dateIso.isBlank())
            ? LocalDate.now(ZoneId.of("Asia/Bangkok"))
            : LocalDate.parse(dateIso);
        return npJdbc.queryForList(SQL, new MapSqlParameterSource().addValue("date", effective));
    }
}
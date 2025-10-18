package com.example.lc2_booking_room.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Map<String, Object>> getRoomStatuses(String date) {
        try {
            String sql = new ClassPathResource("sql/room_status.sql")
                    .getContentAsString(StandardCharsets.UTF_8);
            Query query = entityManager.createNativeQuery(sql);
            query.setParameter("date", date);
            query.unwrap(org.hibernate.query.NativeQuery.class)
                 .setTupleTransformer((tuple, aliases) -> {
                     Map<String, Object> map = new HashMap<>();
                     for (int i = 0; i < aliases.length; i++) {
                         map.put(aliases[i], tuple[i]);
                     }
                     return map;
                 });
            return query.getResultList();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load SQL resource room_status.sql: " + e.getMessage());
        }
    }

    @Override
    public String getRoomStatus(String roomCode) {
        try {
            return (String) entityManager.createQuery(
                "SELECT CASE WHEN r.active = true THEN 'ACTIVE' ELSE 'INACTIVE' END FROM Room r WHERE r.code = :code"
            )
            .setParameter("code", roomCode)
            .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }
}


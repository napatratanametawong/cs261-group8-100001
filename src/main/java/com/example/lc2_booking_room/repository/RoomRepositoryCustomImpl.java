package com.example.lc2_booking_room.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Repository("roomRepositoryCustomImpl")
public class RoomRepositoryCustomImpl implements RoomRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Map<String, Object>> getRoomStatuses(String date) {
        try {
            String sql = new String(Files.readAllBytes(Paths.get("src/main/resources/sql/room_status.sql")));
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
            throw new RuntimeException("อ่านไฟล์ SQL ไม่ได้: " + e.getMessage());
        }
    }
}

package com.example.lc2_booking_room.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Map;


@Repository
public class RoomRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> getRoomStatuses(String date) {
        try {
            // โหลด SQL จากไฟล์
            String sql = Files.readString(Path.of("src/main/resources/sql/room_status.sql"));
            Query query = entityManager.createNativeQuery(sql);

            // ใส่ parameter ชื่อ date
            query.setParameter("date", date);

            // แปลงผลลัพธ์เป็น List<Map<String, Object>>
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


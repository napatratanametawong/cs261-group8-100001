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

@Repository
public class RoomRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Map<String, Object>> getRoomStatuses(String date) {
        try {
            // โหลด query จากไฟล์ SQL
            String sql = Files.readString(Path.of("src/main/resources/sql/room_status.sql"));
            Query query = entityManager.createNativeQuery(sql);

            // ตั้งค่า parameter
            query.setParameter("date", date);

            // แปลงผลลัพธ์เป็น List<Map<String,Object>>
            query.unwrap(org.hibernate.query.NativeQuery.class)
                 .setResultTransformer(org.hibernate.transform.AliasToEntityMapResultTransformer.INSTANCE);

            return query.getResultList();

        } catch (IOException e) {
            throw new RuntimeException("❌ ไม่สามารถโหลดไฟล์ room_status.sql ได้", e);
        }
    }
}

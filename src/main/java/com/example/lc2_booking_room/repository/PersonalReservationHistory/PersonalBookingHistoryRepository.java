package com.example.lc2_booking_room.repository.PersonalReservationHistory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

public interface PersonalBookingHistoryRepository
    extends JpaRepository<com.example.lc2_booking_room.model.ReservationEvent, Long> {
  @Query(value = """
        SELECT
          r.reservation_id      AS reservationId,
          r.room_code           AS roomCode,
          r.reservation_date    AS reservationDate,

          STRING_AGG(rs.slot_code, ', ')
            WITHIN GROUP (ORDER BY t.start_time) AS slotCodes,

          r.step                AS step,
          r.final_status        AS finalStatus,
          r.user_name           AS userName,
          r.user_email          AS userEmail,

          -- คืนค่าเป็น ISO-8601 string พร้อม offset (+07:00 ฯลฯ)
          FORMAT(
            COALESCE(
              CASE WHEN r.final_status = 'APPROVED'              THEN r.approved_at END,
              CASE WHEN r.final_status IN ('REJECTED','CANCELLED') THEN r.head_decided_at END,
              r.head_decided_at,
              r.staff_reviewed_at,
              r.created_at
            ),
            'yyyy-MM-ddTHH:mm:sszzz'
          ) AS lastStatusAtIso

        FROM dbo.reservations r
        LEFT JOIN dbo.reservation_slots rs
               ON rs.reservation_id = r.reservation_id
              AND rs.is_active = 1
        LEFT JOIN dbo.time_slots t
               ON t.slot_code = rs.slot_code
        WHERE r.user_email = :email
          AND (:roomCode IS NULL OR r.room_code = :roomCode)
          AND (:fromDate IS NULL OR r.reservation_date >= :fromDate)
          AND (:toDate   IS NULL OR r.reservation_date <= :toDate)
          AND (:status   IS NULL OR r.final_status = :status)
        GROUP BY
          r.reservation_id, r.room_code, r.reservation_date,
          r.step, r.final_status, r.user_name, r.user_email,
          r.approved_at, r.created_at, r.head_decided_at, r.staff_reviewed_at
        ORDER BY
          -- เรียงใน SQL ได้ยากเมื่อเป็น string; คุณยังคง ORDER BY ตามคอลัมน์ดิบเดิมควบคู่ไปด้วย:
          COALESCE(
            CASE WHEN r.final_status = 'APPROVED'              THEN r.approved_at END,
            CASE WHEN r.final_status IN ('REJECTED','CANCELLED') THEN r.head_decided_at END,
            r.head_decided_at,
            r.staff_reviewed_at,
            r.created_at
          ) DESC,
          r.reservation_date DESC,
          r.reservation_id DESC
      """, countQuery = """
        SELECT COUNT(*) FROM (
          SELECT r.reservation_id
          FROM dbo.reservations r
          WHERE r.user_email = :email
            AND (:roomCode IS NULL OR r.room_code = :roomCode)
            AND (:fromDate IS NULL OR r.reservation_date >= :fromDate)
            AND (:toDate   IS NULL OR r.reservation_date <= :toDate)
            AND (:status   IS NULL OR r.final_status = :status)
          GROUP BY r.reservation_id
        ) x
      """, nativeQuery = true)
  Page<PersonalBookingHistoryView> findPersonalHistory(
      @Param("email") String email,
      @Param("roomCode") String roomCode,
      @Param("fromDate") java.sql.Date fromDate,
      @Param("toDate") java.sql.Date toDate,
      @Param("status") String status,
      Pageable pageable);
}

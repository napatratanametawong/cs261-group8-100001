package com.example.lc2_booking_room.repository;

import com.example.lc2_booking_room.model.ReservationSlot;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlot, Long> {

    // ดึงทุกรายการ slot ของ reservation หนึ่ง
    List<ReservationSlot> findByReservation_Id(Long reservationId);

    // ป้องกันซ้ำภายใน reservation เดียวกัน
    boolean existsByReservation_IdAndSlotCode(Long reservationId, String slotCode);

    // ✅ เมธอดเช็ค "ชน" ตาม ERD: วันเดียวกัน-ห้องเดียวกัน-ช่วงเวลาเดียวกัน
    @Query("""
           select count(rs) > 0
           from ReservationSlot rs
           join rs.reservation r
           where r.reservationDate = :reservationDate
             and rs.roomCode = :roomCode
             and rs.slotCode in :slotCodes
             and rs.isActive = true
           """)
    boolean anyActiveConflict(
            @Param("roomCode") String roomCode,
            @Param("reservationDate") LocalDate reservationDate,
            @Param("slotCodes") Collection<String> slotCodes
    );

    // ใช้ตอน soft-delete (ปิดการใช้งาน slot)
    @Modifying
    @Query("""
           update ReservationSlot rs
           set rs.isActive = false
           where rs.reservation.id = :reservationId
           """)
    int deactivateAllByReservation(@Param("reservationId") Long reservationId);
}

// dto/reservation/CreateReservationBySlotsRequest.java
package com.example.lc2_booking_room.dto.reservation;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record CreateReservationBySlotsRequest(
        @NotNull Long roomId,
        @NotNull LocalDate date,
        @NotEmpty List<@NotNull Long> slotIds,   // slot_id ที่ผู้ใช้เลือก
        @NotBlank String reservedBy               // ชื่อ/อีเมลผู้จอง → ไปลง reserved_by
) {}

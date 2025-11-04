// dto/reservation/CreateReservationBySlotsRequest.java
package com.example.lc2_booking_room.dto.reservation;

import lombok.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CreateReservationBySlotsRequest {

    @NotBlank
    private String roomCode;         // FK → rooms.code

    @NotNull
    private LocalDate reservationDate;  // อยู่ฝั่ง reservations เท่านั้น

    @Size(min = 1)
    @NotNull
    private List<@NotBlank String> slotCodes; // รายการ slot_code ที่ต้องการจอง

    @Size(max = 255)
    private String reason;           // ออปชัน ตาม ERD

    private String fileAttachment;   // ออปชัน (text/URL/base64 ตามที่คุณใช้)
    
    // ข้อมูลผู้จอง (เลือกระบุเท่าที่ต้องใช้ในระบบ auth ของคุณ)
    @Email @Size(max = 100)
    private String userEmail;

    @Size(max = 100)
    private String userName;
}
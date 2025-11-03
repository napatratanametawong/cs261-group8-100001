// src/main/java/com/example/lc2_booking_room/dto/PersonalBookingHistoryResponse.java
package com.example.lc2_booking_room.dto.room;

import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class PersonalBookingHistoryResponse {
    private Long reservationId;
    private String roomCode;
    private LocalDate reservationDate;
    private String slotCodes;     
    private String timeRanges;    
    private String step;
    private String finalStatus;
    private String userName;
    private String userEmail;
    private LocalDateTime lastStatusAt;
}

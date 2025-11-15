package com.example.lc2_booking_room.dto.reservation;

import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PersonalBookingHistoryResponse {
    private Long reservationId;
    private String roomCode;
    private LocalDate reservationDate;
    private String slotCodes;
    private String step;
    private String finalStatus;
    private String userName;
    private String userEmail;
    private OffsetDateTime lastStatusAt;
}
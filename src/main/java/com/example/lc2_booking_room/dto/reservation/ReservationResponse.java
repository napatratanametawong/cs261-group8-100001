package com.example.lc2_booking_room.dto.reservation;

import java.time.LocalDateTime;

public record ReservationResponse(
        Long reservationId,
        Long roomId,
        LocalDateTime startDateTime,
        LocalDateTime endDateTime,
        String status
) {}
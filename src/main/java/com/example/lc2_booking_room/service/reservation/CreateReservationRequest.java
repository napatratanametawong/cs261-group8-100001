package com.example.lc2_booking_room.service.reservation;

import java.time.LocalDate;
import java.util.List;

public record CreateReservationRequest(
        String roomCode,
        LocalDate reservationDate,
        List<String> slotCodes,
        String reason,
        String fileAttachment,
        String userEmail,
        String userName
) {}
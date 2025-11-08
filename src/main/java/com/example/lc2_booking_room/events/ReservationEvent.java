// src/main/java/com/example/lc2_booking_room/events/ReservationEvent.java
package com.example.lc2_booking_room.events;

import com.example.lc2_booking_room.model.user_log.LogAction;

public record ReservationEvent(
        Long reservationId,
        String userEmail,
        LogAction action,
        String note
) {}

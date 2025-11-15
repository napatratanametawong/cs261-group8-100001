// src/main/java/com/example/lc2_booking_room/service/reservation/ReservationLogService.java
package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.events.ReservationEvent;
import com.example.lc2_booking_room.model.user_log.LogAction;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
public class ReservationLogService {

    private final ApplicationEventPublisher events;

    public ReservationLogService(ApplicationEventPublisher events) {
        this.events = events;
    }

    public void created(Long reservationId, String actorEmail) {
        publish(reservationId, actorEmail, LogAction.CREATED, "Reservation created");
    }

    private void publish(Long reservationId, String actorEmail, LogAction action, String note) {
        events.publishEvent(new ReservationEvent(reservationId, actorEmail, action, note));
    }
}

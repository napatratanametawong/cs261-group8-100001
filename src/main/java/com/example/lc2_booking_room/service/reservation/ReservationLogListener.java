// src/main/java/com/example/lc2_booking_room/service/reservation/ReservationLogListener.java
package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.events.ReservationEvent;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.user_log.UserReservationLog;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.UserReservationLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

@Component
public class ReservationLogListener {

    private final ReservationRepository reservationRepository;
    private final UserReservationLogRepository logRepository;

    public ReservationLogListener(ReservationRepository reservationRepository,
                                  UserReservationLogRepository logRepository) {
        this.reservationRepository = reservationRepository;
        this.logRepository = logRepository;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onReservationEvent(ReservationEvent e) {
        try {
            Reservation reservationRef = reservationRepository.getReferenceById(e.reservationId());

            UserReservationLog log = new UserReservationLog();
            log.setReservation(reservationRef);
            log.setUserEmail(e.userEmail());
            log.setAction(e.action());
            log.setNote(e.note());

            logRepository.save(log);
        } catch (EntityNotFoundException ignore) {
        }
    }
}

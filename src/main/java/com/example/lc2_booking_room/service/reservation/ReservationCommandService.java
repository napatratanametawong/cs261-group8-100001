package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationCommandService {

    private final ReservationRepository reservationRepository;
    private final ReservationLogService reservationLogService; // for user log

    /**
     * Create reservation inside one transaction and emit CREATED log
     */
    @Transactional
    public Reservation createReservation(CreateReservationRequest req, String actorEmail) {
        // map request -> entity (keep your existing mapping here)
        Reservation r = new Reservation();
        Reservation saved = reservationRepository.save(r);

        // write user log after successful save (listener persists AFTER_COMMIT)
        String emailForLog = (actorEmail != null && !actorEmail.isBlank())
                ? actorEmail
                : req.userEmail();
        reservationLogService.created(saved.getId(), emailForLog);

        return saved;
    }
    
    @Transactional
    public Reservation createReservation(CreateReservationRequest req) {
        return createReservation(req, /* actorEmail */ null);
    }
}

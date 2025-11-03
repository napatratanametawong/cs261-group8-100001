// service/ReservationServiceBySlots.java
package com.example.lc2_booking_room.service;

import com.example.lc2_booking_room.dto.reservation.CreateReservationBySlotsRequest;
import com.example.lc2_booking_room.dto.reservation.ReservationResponse;
import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.model.Room;
import com.example.lc2_booking_room.model.TimeSlot;
import com.example.lc2_booking_room.repository.ReservationRepository;
import com.example.lc2_booking_room.repository.RoomRepository;
import com.example.lc2_booking_room.repository.TimeSlotRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ReservationServiceBySlots {

    private final ReservationRepository reservationRepo;
    private final TimeSlotRepository timeSlotRepo;
    private final RoomRepository roomRepo;

    private static final DateTimeFormatter TH_TIME = DateTimeFormatter.ofPattern("HH.mm");

    public ReservationServiceBySlots(
            ReservationRepository reservationRepo,
            TimeSlotRepository timeSlotRepo,
            RoomRepository roomRepo
    ) {
        this.reservationRepo = reservationRepo;
        this.timeSlotRepo = timeSlotRepo;
        this.roomRepo = roomRepo;
    }

    @Transactional
    public ReservationResponse create(CreateReservationBySlotsRequest req) {
        if (req.slotIds() == null || req.slotIds().isEmpty()) {
            throw new IllegalArgumentException("Please select at least 1 time slot.");
        }

        // 1) Check conflicts (convert numeric ids to string codes)
        List<String> conflicted = reservationRepo
                .findConflictedSlotIds(String.valueOf(req.roomId()), req.date(),
                        req.slotIds().stream().map(String::valueOf).toList());
        if (!conflicted.isEmpty()) {
            // Load first conflicted slot for a friendly message
            var conflictSlots = timeSlotRepo.findBySlotCodeIn(conflicted);
            var t = conflictSlots.get(0);

            String msg = "Time %s - %s is already booked"
                    .formatted(
                            t.getStartTime().format(TH_TIME),
                            t.getEndTime().format(TH_TIME)
                    );

            throw new RoomBusyException(msg);
        }

        // 2) Load chosen TimeSlots
        List<String> chosenIds = req.slotIds().stream().map(String::valueOf).toList();
        List<TimeSlot> chosen = timeSlotRepo.findBySlotCodeIn(chosenIds);

        // 3) Create one Reservation per slot
        List<Reservation> toSave = new ArrayList<>();
        Room roomRef = roomRepo.findById(String.valueOf(req.roomId()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid room"));
        for (TimeSlot slot : chosen) {
            Reservation r = new Reservation();
            r.setRoom(roomRef);
            r.setSlot(slot);
            r.setDate(req.date());
            r.setReservedBy(req.reservedBy());
            r.setStatus("CONFIRMED");
            toSave.add(r);
        }
        List<Reservation> saved = reservationRepo.saveAll(toSave);

        // 4) Build response summary across selected slots
        var minStart = chosen.stream().map(TimeSlot::getStartTime).min(java.time.LocalTime::compareTo).orElseThrow();
        var maxEnd = chosen.stream().map(TimeSlot::getEndTime).max(java.time.LocalTime::compareTo).orElseThrow();
        var startDateTime = LocalDateTime.of(req.date(), minStart);
        var endDateTime = LocalDateTime.of(req.date(), maxEnd);

        return new ReservationResponse(
                saved.get(0).getReservationId(),
                req.roomId(),
                startDateTime,
                endDateTime,
                "CONFIRMED"
        );
    }

    public static class RoomBusyException extends RuntimeException {
        public RoomBusyException(String message) { super(message); }
    }
}


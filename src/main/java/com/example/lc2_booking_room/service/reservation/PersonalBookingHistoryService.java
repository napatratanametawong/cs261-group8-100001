package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.dto.reservation.PersonalBookingHistoryResponse;
import com.example.lc2_booking_room.repository.PersonalReservationHistory.PersonalBookingHistoryRepository;
import com.example.lc2_booking_room.repository.PersonalReservationHistory.PersonalBookingHistoryView;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class PersonalBookingHistoryService {

    private final PersonalBookingHistoryRepository repo;

    // SQL uses FORMAT(..., 'yyyy-MM-ddTHH:mm:sszzz') -> matches Java "XXX"
    private static final DateTimeFormatter ISO_WITH_OFFSET = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX");

    public Page<PersonalBookingHistoryResponse> getMyHistory(
            String email,
            String roomCode,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size) {
        Pageable pageable = PageRequest.of(page, size);
        var pageView = repo.findPersonalHistory(
                email,
                roomCode,
                fromDate == null ? null : java.sql.Date.valueOf(fromDate),
                toDate == null ? null : java.sql.Date.valueOf(toDate),
                status,
                pageable);

        return pageView.map(this::mapToDto);
    }

    private PersonalBookingHistoryResponse mapToDto(PersonalBookingHistoryView v) {
        // Parse ISO string with offset to OffsetDateTime
        OffsetDateTime last = null;
        String iso = v.getLastStatusAtIso(); // <-- comes from projection
        if (iso != null && !iso.isBlank()) {
            last = OffsetDateTime.parse(iso, ISO_WITH_OFFSET);
        }

        return PersonalBookingHistoryResponse.builder()
                .reservationId(v.getReservationId())
                .roomCode(v.getRoomCode())
                .reservationDate(v.getReservationDate())
                .slotCodes(v.getSlotCodes())
                .step(v.getStep())
                .finalStatus(v.getFinalStatus())
                .userName(v.getUserName())
                .userEmail(v.getUserEmail())
                .lastStatusAt(last) // OffsetDateTime in DTO
                .build();
    }
}

package com.example.lc2_booking_room.service.reservation;

import com.example.lc2_booking_room.dto.reservation.PersonalBookingHistoryResponse;
import com.example.lc2_booking_room.repository.PersonalReservationHistory.PersonalBookingHistoryRepository;
import com.example.lc2_booking_room.repository.PersonalReservationHistory.PersonalBookingHistoryView;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class PersonalBookingHistoryService {

    private final PersonalBookingHistoryRepository repo;

    public Page<PersonalBookingHistoryResponse> getMyHistory(
            String email,
            String roomCode,
            LocalDate fromDate,
            LocalDate toDate,
            String status,
            int page,
            int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        var pageView = repo.findPersonalHistory(
                email,
                roomCode,
                fromDate == null ? null : java.sql.Date.valueOf(fromDate),
                toDate   == null ? null : java.sql.Date.valueOf(toDate),
                status,
                pageable
        );

        return pageView.map(this::mapToDto);
    }

    private PersonalBookingHistoryResponse mapToDto(PersonalBookingHistoryView v) {
        return PersonalBookingHistoryResponse.builder()
                .reservationId(v.getReservationId())
                .roomCode(v.getRoomCode())
                .reservationDate(v.getReservationDate())
                .slotCodes(v.getSlotCodes())
                .step(v.getStep())
                .finalStatus(v.getFinalStatus())
                .userName(v.getUserName())
                .userEmail(v.getUserEmail())
                .lastStatusAt(v.getLastStatusAt())
                .build();
    }
}

package com.example.lc2_booking_room.repository.PersonalReservationHistory;

import java.time.LocalDate;

public interface PersonalBookingHistoryView {
    Long getReservationId();

    String getRoomCode();

    LocalDate getReservationDate();

    String getSlotCodes();

    String getStep();

    String getFinalStatus();

    String getUserName();

    String getUserEmail();

    String getLastStatusAtIso();
}

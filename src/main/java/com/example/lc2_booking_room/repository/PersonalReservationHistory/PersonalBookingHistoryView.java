package com.example.lc2_booking_room.repository.PersonalReservationHistory;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface PersonalBookingHistoryView {
    Long getReservationId();
    String getRoomCode();
    LocalDate getReservationDate();
    String getSlotCodes();
    String getTimeRanges();
    String getStep();
    String getFinalStatus();
    String getUserName();
    String getUserEmail();
    LocalDateTime getLastStatusAt();
}
package com.example.lc2_booking_room.dto.staff;

import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffLogResponse {
    private Long staffLogId;
    private Long reservationId;
    private String staffEmail;
    private StaffAction action;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime changedAt;
    private String note;
}

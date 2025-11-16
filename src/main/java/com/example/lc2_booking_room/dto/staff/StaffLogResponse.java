package com.example.lc2_booking_room.dto.staff;

import com.example.lc2_booking_room.model.staff_log.StaffAction;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StaffLogResponse {
    private Long staffLogId;
    private Long reservationId;
    private String staffEmail;
    private StaffAction action;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX")
    private OffsetDateTime changedAt;

    private String note;
}

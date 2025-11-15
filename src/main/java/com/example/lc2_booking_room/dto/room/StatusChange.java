package com.example.lc2_booking_room.dto.room;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatusChange {
    private String fromStep;
    private String toStep;
    private String fromFinalStatus;
    private String toFinalStatus;

    public boolean hasAnyChange() {
        return notEquals(fromStep, toStep) || notEquals(fromFinalStatus, toFinalStatus);
    }

    private boolean notEquals(String a, String b) {
        return (a == null && b != null) || (a != null && !a.equals(b));
    }
}
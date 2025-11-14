package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.model.staff_log.StaffNotification;
import com.example.lc2_booking_room.repository.StaffNotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class StaffInAppNotificationService {

    private final StaffNotificationRepository repo;

    public void send(String staffEmail, Long reservationId, String message) {

        StaffNotification n = StaffNotification.builder()
                .reservationId(reservationId)
                .staffEmail(staffEmail)
                .message(message)
                .createdAt(OffsetDateTime.now())
                .isRead(false)  // ✔ แก้ตรงนี้
                .build();

        repo.save(n);
    }
}

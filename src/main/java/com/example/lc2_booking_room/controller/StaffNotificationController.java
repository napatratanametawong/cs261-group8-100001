package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.service.notification.StaffNotificationService; 

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/staff/notifications")
@RequiredArgsConstructor
public class StaffNotificationController {

    private final StaffNotificationService notificationService;  

    /** ดึง email staff จาก JWT */
    private String getCurrentStaffEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("No authenticated staff user");
        }
        return auth.getName();
    }

    /** ✔ ดึงกล่องแจ้งเตือนของ staff ที่กำลังล็อกอิน */
    @GetMapping
    public ResponseEntity<List<Notification>> getInbox() {
        String email = getCurrentStaffEmail();
        return ResponseEntity.ok(notificationService.getStaffInbox(email));
    }

    /** ✔ อ่านการแจ้งเตือน */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        String email = getCurrentStaffEmail();
        Notification updated = notificationService.markRead(id, email);     
        return ResponseEntity.ok(updated);
    }

}

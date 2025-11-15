package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.service.notification.NotificationService;

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

    private final NotificationService service;

    /** ดึง email staff จาก JWT */
    private String getCurrentStaffEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /** ✔ ดึงกล่องแจ้งเตือนของ staff ที่กำลังล็อกอิน */
    @GetMapping
    public ResponseEntity<List<Notification>> getInbox() {
        String email = getCurrentStaffEmail();
        return ResponseEntity.ok(service.getStaffInbox(email));
    }

    /** ✔ อ่านการแจ้งเตือน */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        String email = getCurrentStaffEmail();
        Notification updated = service.markRead(id, email);
        return ResponseEntity.ok(updated);
    }

}

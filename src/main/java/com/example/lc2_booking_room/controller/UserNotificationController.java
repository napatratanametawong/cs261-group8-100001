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
@RequestMapping("/api/user/notifications")
@RequiredArgsConstructor
public class UserNotificationController {

    private final NotificationService service;

    // email user จาก JWT
    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    // ดึงกล่องแจ้งเตือนของ user (เฉพาะ WEB, ไม่ถูกลบ)
    @GetMapping
    public ResponseEntity<List<Notification>> getInbox() {
        String email = getCurrentUserEmail();
        List<Notification> notifications = service.getUserInbox(email);
        return ResponseEntity.ok(notifications);
    }

    // อ่านการแจ้งเตือน
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        String email = getCurrentUserEmail();
        Notification updated = service.markRead(id, email);
        return ResponseEntity.ok(updated);
    }
}

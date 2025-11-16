package com.example.lc2_booking_room.controller;

import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.service.notification.UserInAppNotificationService;  
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

    private final UserInAppNotificationService service;   // 👈 เปลี่ยน type ให้ตรงกับ import

    private String getCurrentUserEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getInbox() {
        String email = getCurrentUserEmail();
        List<Notification> notifications = service.getUserInbox(email);  // ต้องมี method นี้ใน service
        return ResponseEntity.ok(notifications);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markRead(@PathVariable Long id) {
        String email = getCurrentUserEmail();
        Notification updated = service.markRead(id, email);               // และ method นี้ด้วย
        return ResponseEntity.ok(updated);
    }
}

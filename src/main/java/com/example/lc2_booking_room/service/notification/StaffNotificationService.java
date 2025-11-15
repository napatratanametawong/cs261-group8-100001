package com.example.lc2_booking_room.service.notification;

import com.example.lc2_booking_room.model.notification.Notification;
import com.example.lc2_booking_room.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class StaffNotificationService {   

    private final NotificationRepository repo;

    /** ดึง noti ทั้งหมดของ staff ที่ล็อกอินอยู่ (เฉพาะ WEB, ไม่ถูกลบ) */
    public List<Notification> getStaffInbox(String email) {
        if (email == null || email.isBlank()) {
            // กันเคส auth เพี้ยน / ยังไม่ได้ล็อกอิน
            return List.of();
        }

        return repo.findByRecipientEmailAndRecipientRoleAndChannelAndDeletedFalseOrderByCreatedAtDesc(
                email,
                Notification.RecipientRole.STAFF,
                Notification.NotificationChannel.WEB
        );
    }

    /** กดอ่าน noti */
    public Notification markRead(Long id, String email) {
        Notification n = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Notification not found"));

        // กันเผื่ออนาคต: เช็ค owner ด้วย
        if (email == null || !n.getRecipientEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("You are not owner of this notification");
        }

        n.setRead(true);
        // ✅ ใช้เวลาโซนไทยให้ตรงกับคอลัมน์อื่น ๆ
        n.setReadAt(OffsetDateTime.now(ZoneId.of("Asia/Bangkok")));
        return repo.save(n);
    }
}

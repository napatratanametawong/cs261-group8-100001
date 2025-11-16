package com.example.lc2_booking_room.model.notification;

import com.example.lc2_booking_room.model.Reservation;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    public enum RecipientRole {
        USER, STAFF, HEAD, ADMIN
    }

    public enum NotificationChannel {
        WEB, EMAIL
    }

    public enum NotificationType {
        NEW_REQUEST,
        STAFF_REVIEWED,
        STAFF_RETURNED,
        HEAD_APPROVED,
        HEAD_REJECTED,
        USER_CANCELLED
    }

    public enum SendStatus {
        PENDING, SUCCESS, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "recipient_email", nullable = false, length = 100)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", nullable = false, length = 30)
    private RecipientRole recipientRole;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id")
    @JsonIgnore
    private Reservation reservation;

    @Column(name = "user_log_id")
    private Long userLogId;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 50)
    private NotificationType notificationType;

    @Column(name = "title", length = 200)
    private String title;

    @Column(name = "message", nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel; // WEB, EMAIL

    @Column(name = "is_read", nullable = false)
    private boolean read;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt; // map กับ DATETIMEOFFSET(0)

    @Column(name = "read_at")
    private OffsetDateTime readAt;

    @Column(name = "sent_at")
    private OffsetDateTime sentAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "send_status", length = 20)
    private SendStatus sendStatus;

    @Column(name = "send_error", length = 500)
    private String sendError;

    @Column(name = "is_deleted", nullable = false)
    private boolean deleted;

    @PrePersist
    protected void onCreate() {
        // กันลืม channel (optional)
        if (channel == null) {
            channel = NotificationChannel.WEB;
        }

        // default sendStatus สำหรับ EMAIL
        if (channel == NotificationChannel.EMAIL && sendStatus == null) {
            sendStatus = SendStatus.PENDING;
        }
    }
}
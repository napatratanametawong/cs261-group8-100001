package com.example.lc2_booking_room.model.staff_log;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "staff_notifications")
public class StaffNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long id;

    @Column(name = "reservation_id")
    private Long reservationId;

    @Column(name = "staff_email")
    private String staffEmail;

    @Column(name = "message")
    private String message;

    @Column(name = "created_at")
    private OffsetDateTime createdAt;

    @Column(name = "is_read")
    private boolean isRead;
}
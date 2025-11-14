package com.example.lc2_booking_room.service.staff;

import com.example.lc2_booking_room.model.Reservation;
import com.example.lc2_booking_room.service.login.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StaffEmailNotificationService {
private final EmailService emailService;

    private final StaffInAppNotificationService inApp;
    private final String STAFF_EMAIL = "lc2.serviceadm@gmail.com";  // อีเมลเจ้าหน้าที่

    /** เมื่อมีคำขอจองใหม่ */
    public void notifyCreated(Reservation r) {

        String subject = "มีคำขอร้องจองห้องใหม่ NEW!!";

        String body = String.format("""
                มีคำขอร้องจองห้องใหม่ NEW!!

                ผู้จอง : %s
                E-mail : %s
                Reservation ID : %s

                โปรดตรวจสอบและอนุมัติการจองในเว็บไซต์...
                """,
                r.getUserName(),
                r.getUserEmail(),
                r.getId()
        );

        emailService.sendStaffNotification(STAFF_EMAIL, subject, body);
        inApp.send(STAFF_EMAIL, r.getId(), "New reservation #" + r.getId());

    }

    /** เมื่อมีการยกเลิก */
    public void notifyCanceled(Reservation r) {

        String subject = "มีคำขอยกเลิกการจองห้อง";

        String body = String.format("""
                คำร้องถูกยกเลิก

                ผู้ใช้ : %s
                E-mail : %s
                Reservation ID : %s

                โปรดตรวจสอบข้อมูลเพิ่มเติมในเว็บไซต์...
                """,
                r.getUserName(),
                r.getUserEmail(),
                r.getId()
        );

        emailService.sendStaffNotification(STAFF_EMAIL, subject, body);
        inApp.send(STAFF_EMAIL, r.getId(),body);

    }
}

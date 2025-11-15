package com.example.lc2_booking_room.service.login;

import java.util.List;
import java.util.StringJoiner;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mail;

    public EmailService(JavaMailSender mail) {
        this.mail = mail;
    }

    /** ส่ง OTP แบบข้อความล้วน */
    public void sendOtp(String toEmail, String otp) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);

        // สำคัญ: From ต้องตรงกับ spring.mail.username (Gmail เดิม)
        msg.setFrom("booking.lc2@gmail.com");

        // ทางเลือก: อยากให้ตอบกลับไปที่อีเมล TU
        // msg.setReplyTo("tatchakrit.sta@dome.tu.ac.th");

        msg.setSubject("LC2 Booking - OTP ของคุณ");
        msg.setText("""
                สวัสดีค่ะ/ครับ,

                รหัส OTP ของคุณคือ: %s
                รหัสมีอายุ 5 นาที

                ถ้าไม่ได้ร้องขอ กรุณาเพิกเฉยอีเมลนี้
                """.formatted(otp));

        mail.send(msg);
    }

    /** ส่งอีเมลแจ้งเตือนผู้ใช้ เมื่อ staff หรือหัวหน้าสาขาดำเนินการกับคำร้อง */
    public void sendStaffActionNotice(
            String toEmail,
            String action,
            String roomCode,
            String date,
            List<String> timeRanges,
            String note) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(toEmail);
        msg.setFrom("booking.lc2@gmail.com");
        msg.setSubject("LC2 Booking - การอัปเดตคำร้องของคุณ");

        StringJoiner joiner = new StringJoiner(", ");
        if (timeRanges != null && !timeRanges.isEmpty()) {
            timeRanges.forEach(joiner::add);
        }
        String timeText = joiner.toString().isEmpty() ? "-" : joiner.toString();

        String body = switch (action) {
            case "RETURNED" -> """
                    สวัสดีค่ะ,

                    คำร้องจองห้องของคุณ (ห้อง %s วันที่ %s เวลา %s)
                    ถูกตีกลับโดยเจ้าหน้าที่ เนื่องจาก:

                    %s

                    กรุณาแก้ไขข้อมูลและส่งคำร้องใหม่อีกครั้งค่ะ
                    """.formatted(roomCode, date, timeText, note == null ? "-" : note);

            case "REVIEWED" -> """
                    สวัสดีค่ะ,

                    คำร้องจองห้องของคุณ (ห้อง %s วันที่ %s เวลา %s)
                    ได้รับการตรวจสอบโดยเจ้าหน้าที่เรียบร้อยแล้ว
                    รอการอนุมัติจากหัวหน้าสาขาในขั้นตอนถัดไปค่ะ
                    """.formatted(roomCode, date, timeText);

            case "REJECTED" -> """
                    สวัสดีค่ะ,

                    คำร้องจองห้องของคุณ (ห้อง %s วันที่ %s เวลา %s)
                    ถูกปฏิเสธ เนื่องจาก:

                    %s
                    """.formatted(roomCode, date, timeText, note == null ? "-" : note);

            case "APPROVED" -> """
                    สวัสดีค่ะ,

                    คำร้องจองห้องของคุณ (ห้อง %s วันที่ %s เวลา %s)
                    ได้รับการอนุมัติจากหัวหน้าสาขาแล้วค่ะ

                    คุณสามารถใช้ห้องได้ตามวันที่และเวลาที่ระบุไว้
                    ขอให้การใช้งานเป็นไปอย่างราบรื่นค่ะ
                    """.formatted(roomCode, date, timeText);

            default -> """
                    สวัสดีค่ะ,

                    คำร้องจองห้องของคุณมีการอัปเดตสถานะ: %s
                    ห้อง: %s วันที่: %s เวลา: %s
                    """.formatted(action, roomCode, date, timeText);
        };

        msg.setText(body);
        mail.send(msg);
    }
}

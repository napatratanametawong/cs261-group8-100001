package com.example.lc2_booking_room.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mail;
    @Value("${app.mail.enabled:true}")
    private boolean mailEnabled;

    public EmailService(JavaMailSender mail) {
        this.mail = mail;
    }

    /** ส่ง OTP แบบข้อความล้วน */
    public void sendOtp(String toEmail, String otp) {
        if (!mailEnabled) {
            System.out.println("[DEV] OTP for " + toEmail + ": " + otp);
            return;
        }
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
}

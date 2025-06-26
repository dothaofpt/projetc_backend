package org.example.projetc_backend.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String toEmail, String otp) {
        if (toEmail == null || toEmail.trim().isEmpty() || otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("Email và OTP không được để trống.");
        }
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Mã OTP để đặt lại mật khẩu của bạn");
        message.setText("Mã OTP của bạn là: " + otp + ". Mã này có hiệu lực trong 10 phút.");
        message.setFrom("noreply@yourdomain.com"); // Đặt email gửi đi (thường cấu hình trong application.properties)
        mailSender.send(message);
    }
}
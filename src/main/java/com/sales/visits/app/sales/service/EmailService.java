package com.sales.visits.app.sales.service;

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
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("PT Sales - Password Reset Code");
        message.setText("You requested to reset your password.\n\n" +
                        "Your verification code is: " + otp + "\n\n" +
                        "This code expires in 2 minutes.");
        mailSender.send(message);
    }
}

package com.example.auth.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String to, String activationLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        String htmlMsg = "<h3> Thank you for registering!</h3>"
                + "<p> Please click the link below to activate you account:</p>"
                + "<a href=\"" + activationLink + "\"> Activate now </a>";

        helper.setText(htmlMsg, true);
        helper.setTo(to);
        helper.setSubject("Account Activation");
        helper.setFrom("noreply@example.com");
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
        String htmlMsg = "<h3> Password Reset </h3>"
                + "<p> Click the link below to reset your password. The link expires in 15 minutes: </p>"
                + "<a href=\"" + resetLink + "\"> Reset Password </a>";

        helper.setText(htmlMsg, true);
        helper.setTo(to);
        helper.setSubject("Password Reset Request");
        helper.setFrom("noreply@example.com");
        mailSender.send(message);
    }
}

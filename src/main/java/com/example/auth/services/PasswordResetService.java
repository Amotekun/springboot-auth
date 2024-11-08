package com.example.auth.services;

import com.example.auth.entities.PasswordReset;
import com.example.auth.entities.User;
import com.example.auth.repositories.PasswordResetRepository;
import com.example.auth.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;

    @Value("${application.url}")
    private String appUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetRepository passwordResetRepository,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
    }

    public void createPasswordResetToken(String email) throws MessagingException {
        Optional<User> isUserAvailable = userRepository.findByEmail(email);
        if (isUserAvailable.isEmpty()) {
            throw new IllegalStateException("No user associated with this email.");
        }

        User user = isUserAvailable.get();
        String userId = user.getId();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordReset passwordReset = new PasswordReset(userId, token, expiresAt);
        passwordResetRepository.save(passwordReset);

        String resetLink = appUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }

}
package com.example.auth.services;

import com.example.auth.entities.PasswordResetEntity;
import com.example.auth.entities.UserEntity;
import com.example.auth.repositories.PasswordResetRepository;
import com.example.auth.repositories.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    private final UserRepository userRepository;
    private final PasswordResetRepository passwordResetRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.url}")
    private String appUrl;

    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetRepository passwordResetRepository,
            EmailService emailService,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordResetRepository = passwordResetRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 8) {
            return false;
        }

        boolean hasUppercase = password.chars().anyMatch(Character::isUpperCase);
        boolean hasLowercase = password.chars().anyMatch(Character::isLowerCase);
        boolean hasDigits = password.chars().anyMatch(Character::isDigit);
        boolean hasSpecialCharacter = password.chars().anyMatch(ch -> !Character.isLetterOrDigit(ch));

        return hasUppercase && hasLowercase && hasDigits && hasSpecialCharacter;
    }

    public String requestPasswordReset(Map<String, String> credentials) throws MessagingException {
        String email = credentials.get("email");
        Optional<UserEntity> isUserAvailable = userRepository.findByEmail(email);
        if (isUserAvailable.isEmpty()) {
            throw new IllegalStateException("No user associated with this email.");
        }

        UserEntity userEntity = isUserAvailable.get();
        String userId = userEntity.getId();
        String token = UUID.randomUUID().toString();
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        PasswordResetEntity passwordResetEntity = new PasswordResetEntity(userId, token, expiresAt);
        passwordResetRepository.save(passwordResetEntity);

        String resetLink = appUrl + "/reset-password?token=" + token;
        emailService.sendPasswordResetEmail(userEntity.getEmail(), resetLink);

        return "Password reset email sent.";
    }

    public String resetPassword(String token, Map<String, String> credentials) {
        String newPassword = credentials.get("newPassword");

        if (!isPasswordValid( newPassword)) {
            throw new IllegalArgumentException("Password does not meet security requirements");
        }

        PasswordResetEntity resetToken = passwordResetRepository.findByToken(token)
                .filter(rt -> rt.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new IllegalStateException("Invalid or expired reset token"));

        UserEntity userEntity = userRepository.findById(resetToken.getUserId())
                .orElseThrow(() -> new IllegalStateException("User not found"));

        String userId = userEntity.getId();
        String hashedPassword = passwordEncoder.encode(newPassword);
        userEntity.setPassword(hashedPassword);

        userRepository.updatePassword(userId, hashedPassword);
        passwordResetRepository.deleteByUserId(userId);

        return "Password reset successfully";
    }
}
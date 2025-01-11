package com.example.auth.util;

import com.example.auth.entities.TwoFactorTokenEntity;
import com.example.auth.repositories.TwoFactorTokenRepository;
import com.example.auth.repositories.UserContactInfoRepository;
import com.example.auth.services.SMSService;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;
import java.util.UUID;

@Component
public class TokenUtility {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserContactInfoRepository userContactInfoRepository;
    private final TwoFactorTokenRepository twoFactorTokenRepository;
    private final SMSService smsService;

    public TokenUtility(UserContactInfoRepository userContactInfoRepository, TwoFactorTokenRepository twoFactorTokenRepository, SMSService smsService) {
        this.userContactInfoRepository = userContactInfoRepository;
        this.twoFactorTokenRepository = twoFactorTokenRepository;
        this.smsService = smsService;
    }

    public String generateToken() {
        return UUID.randomUUID().toString();
    }

    public String generateOtp() {
        int otp = 100000 + RANDOM.nextInt(900000);
        return String.valueOf(otp);
    }

    public String hashOtp(String otp) throws NoSuchAlgorithmException {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(otp.getBytes());
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("SHA-256 algorithim not available", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public void generateAndSendOtp(String userId) {
        String phoneNumber = userContactInfoRepository.getPhoneNumberByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No verified phone number found for the user"));

        String otp = generateOtp();
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        TwoFactorTokenEntity tokenEntity = new TwoFactorTokenEntity(userId, otp, expiresAt);
        twoFactorTokenRepository.save(tokenEntity);

        smsService.sendOtpSMS(phoneNumber, otp);
    }
}

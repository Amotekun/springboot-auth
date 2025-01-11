package com.example.auth.services;
import com.example.auth.dto.Enable2FARequest;
import com.example.auth.entities.TwoFactorConfirmationEntity;
import com.example.auth.entities.TwoFactorTokenEntity;
import com.example.auth.entities.UserContactInfoEntity;
import com.example.auth.repositories.TwoFactorConfirmationRepository;
import com.example.auth.repositories.TwoFactorTokenRepository;
import com.example.auth.repositories.UserContactInfoRepository;
import com.example.auth.repositories.UserRepository;
import com.example.auth.util.CookieUtility;
import com.example.auth.util.JwtTokenUtility;
import com.example.auth.util.TokenUtility;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
public class TwoFactorService {

    private static final Logger logger = LoggerFactory.getLogger(TwoFactorService.class);

    private final UserRepository userRepository;
    private final TwoFactorTokenRepository twoFactorTokenRepository;
    private final TwoFactorConfirmationRepository twoFactorConfirmationRepository;
    private final UserContactInfoRepository userContactInfoRepository;
    private final SMSService smsService;
    private final JwtTokenUtility jwtTokenUtility;
    private final TokenService tokenService;
    private final TokenUtility tokenUtility;
    private final CookieUtility cookieUtility;

    public TwoFactorService(
            UserRepository userRepository,
            TwoFactorTokenRepository twoFactorTokenRepository,
            TwoFactorConfirmationRepository twoFactorConfirmationRepository,
            UserContactInfoRepository userContactInfoRepository,
            SMSService smsService,
            JwtTokenUtility jwtTokenUtility,
            TokenService tokenService,
            TokenUtility tokenUtility,
            CookieUtility cookieUtility
    ) {
        this.userRepository = userRepository;
        this.twoFactorTokenRepository = twoFactorTokenRepository;
        this.twoFactorConfirmationRepository = twoFactorConfirmationRepository;
        this.userContactInfoRepository = userContactInfoRepository;
        this.smsService = smsService;
        this.jwtTokenUtility = jwtTokenUtility;
        this.tokenService = tokenService;
        this.tokenUtility = tokenUtility;
        this.cookieUtility = cookieUtility;
    }

    public void resendOtp(Map<String, String> request) {
        String userId = request.get("userId");

        int currentAttemptCount = twoFactorTokenRepository.getAttemptCount(userId);
        if (currentAttemptCount >= 3) {
            logger.error("");
            throw new IllegalStateException("Maximum OTP resend attempts reached.");
        }

        generateAndSendOtp(userId);
    }

    public void generateAndSendOtp(String userId) {

        String phoneNumber = userContactInfoRepository.getPhoneNumberByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("No verified phone number found for the user"));

        String otp = tokenUtility.generateOtp();
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        TwoFactorTokenEntity tokenEntity = new TwoFactorTokenEntity(userId, otp, expiresAt);
        twoFactorTokenRepository.save(tokenEntity);

        smsService.sendOtpSMS(phoneNumber, otp);
    }

    private void saveOrUpdateUserContactInfo(String userId, String phoneNumber) {
        UserContactInfoEntity contactInfo = userContactInfoRepository.findById(userId)
                .orElse(new UserContactInfoEntity(userId, phoneNumber));

        contactInfo.setPhoneVerified(false);
        userContactInfoRepository.saveOrUpdate(contactInfo);
    }

    private void saveTwoFactorTokenConfirmation(String userId, String hashedOtp, Instant expiresAt) {
        TwoFactorTokenEntity twoFactorToken = new TwoFactorTokenEntity(userId, hashedOtp, expiresAt);
        twoFactorTokenRepository.save(twoFactorToken);

        TwoFactorConfirmationEntity twoFactorConfirmation = new TwoFactorConfirmationEntity(userId);
        twoFactorConfirmationRepository.save(twoFactorConfirmation);
    }

    public void enable2FA(Enable2FARequest request) throws MessagingException {
        String phoneNumber = request.getPhoneNumber();
        String userId = request.getUserId();

        logger.info("Starting 2fa enabling process for UserId: " + userId);

        // TODO: ADJUST THIS BY ASKING WHY THE OPTIONAL TYPE WILL NOT WORK WITH AN IF STATEMENT AND WHICH METHOD IS BETTER
        userRepository.findById(userId).orElseThrow(() -> {
            return new IllegalStateException("User not found");
        });

        try {

            saveOrUpdateUserContactInfo(userId, phoneNumber);

            logger.info("Generating OTP...");
            String otp = tokenUtility.generateOtp();
            String hashedOtp = tokenUtility.hashOtp(otp);

            logger.info("Hashed OTP length: {}", hashedOtp.length());
            Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

            logger.info("Saving OTP token...");
            TwoFactorTokenEntity twoFactorToken = new TwoFactorTokenEntity(userId, hashedOtp, expiresAt);
            twoFactorTokenRepository.save(twoFactorToken);

            logger.info("Saving 2FA confirmation...");
            smsService.sendOtpSMS(phoneNumber, otp);

            logger.info("Enabling 2FA for userId: " + userId);
            userRepository.enableTwoFactor(userId);


            logger.info("Generated OTP and hashed successfully.");
            TwoFactorConfirmationEntity twoFactorConfirmation = new TwoFactorConfirmationEntity(userId);
            twoFactorConfirmationRepository.save(twoFactorConfirmation);

            logger.info("2FA enabled successfully for userId: {}", userId);
        } catch (Exception e) {
            logger.error("Error during 2FA enabling process: ", e);
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }
    }

    public void verify2FA(String userId, String otp) {
        boolean isValid = twoFactorTokenRepository.isValidOtp(userId, otp);

        if (!isValid) {
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        try {
                userContactInfoRepository.verifyUserPhoneNumber(userId);
                twoFactorTokenRepository.deleteByUserId(userId);
        } catch (Exception e) {
            throw new IllegalStateException("Verification failed: " + e.getMessage());
        }
    }

    public String validateOtp(Map<String, String> request, HttpServletResponse response) {
        String userId = request.get("userId");
        String otp = request.get("otp");

        if (!twoFactorTokenRepository.isValidOtp(userId, otp)) {
            twoFactorTokenRepository.incrementAttemptCount(userId);
            throw new IllegalStateException("Invalid or expired OTP.");
        }

        twoFactorTokenRepository.deleteByUserId(userId);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        String accessToken = jwtTokenUtility.generateToken(authentication);
        String refreshToken = tokenService.generateAndSaveRefreshToken(userId);

        cookieUtility.setRefreshTokenCookie(refreshToken, response);
        return accessToken;
    }


    public void disabled2FA(String userId) {
        userRepository.disableTwoFactor(userId);
        twoFactorTokenRepository.deleteByUserId(userId);
        twoFactorConfirmationRepository.deleteByUserId(userId);
    }
}

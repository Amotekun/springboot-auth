package com.example.auth.controllers;

import com.example.auth.dto.Enable2FARequest;
import com.example.auth.entities.UserContactInfoEntity;
import com.example.auth.services.TwoFactorService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("api/v1/2fa")
public class TwoFactorController {

    private final TwoFactorService twoFactorService;

    public TwoFactorController(TwoFactorService twoFactorService) {
        this.twoFactorService = twoFactorService;
    }

    @PostMapping("/enable")
    public ResponseEntity<?> enable2FA(@Valid @RequestBody Enable2FARequest request) {
        try {
            twoFactorService.enable2FA(request);
            return ResponseEntity.ok("2FA enabled. OTP sent via SMS.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to enable 2FA.");
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> verifyOTP(@RequestBody Map<String, String> credentials) {
        try {
            String userId = credentials.get("userId");
            String otp = credentials.get("otp");

            twoFactorService.verify2FA(userId, otp);
            return ResponseEntity.ok("OTP verified successfully. Phone number marked as verified.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Verification failed: " + e.getMessage());
        }
    }

    // TODO: DISABLED CONTROLLER TO STOP 2FA.
    @PostMapping("/disable")
    public ResponseEntity<?> disable2FA(@RequestBody Map<String, String> credentials) {
        String userId = credentials.get("userId");
        twoFactorService.disabled2FA(userId);
        return ResponseEntity.ok("2FA disabled successfully.");
    }

    @PostMapping("/validate-otp")
    public ResponseEntity<?> validateOtp(@RequestBody Map<String, String> request, HttpServletResponse response) {
        try {
            String accessToken = twoFactorService.validateOtp(request, response);
            return ResponseEntity.ok(Map.of("accessToken", accessToken));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("OTP validation failed");
        }
    }

    @PostMapping("/resend-otp")
    public ResponseEntity<?> resentOtp(@RequestBody Map<String, String> request) {
        try {
            twoFactorService.resendOtp(request);
            return ResponseEntity.ok("OTP resent successfully");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (Exception e) {
            return  ResponseEntity.status(500).body("Failed to resend OTP");
        }
    }

}
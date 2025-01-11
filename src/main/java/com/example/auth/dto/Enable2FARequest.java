package com.example.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class Enable2FARequest {

    @NotBlank(message = "User ID is required.")
    public String userId;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^[+][0-9]{10,15}$", message = "Invalid phone number format")
    private String phoneNumber;

    public String getUserId() {
        return userId;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setPhoneNumber(String phoneNumber) {
        if (phoneNumber != null) {
            if (!phoneNumber.startsWith("+")) {
                this.phoneNumber = "+1" + phoneNumber;
            } else {
                this.phoneNumber = phoneNumber;
            }
        }
    }


}
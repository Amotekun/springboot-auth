package com.example.auth.services;

import com.example.auth.util.LoggerWrapper;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class SMSService {

    public LoggerWrapper logger = LoggerWrapper.getLogger(SMSService.class);

    @Value("${twilio.accountSid}")
    private String accountSid;

    @Value("${twilio.authToken}")
    private String authToken;

    @Value("${twilio.phoneNumber}")
    private String twilioPhoneNumber;

  /*  @Value("${twilio.verifyServiceSid}")
    private String verifyServiceSid;

    @PostConstruct
    public void initTwilio() {
        Twilio.init(accountSid, authToken);
    }

    public void sendTwilioSMS(String to) {
        Twilio.init(accountSid, authToken);
        Verification verification = Verification.creator(
                "verifyServiceSid",
                "to",
                "sms"
        ).create();

        logger.logInfo("SMS Otp sent:" + verification.getSid());
    }*/

    public void sendOtpSMS(String to, String otpCode) {
        Message message = Message.creator(
                new PhoneNumber(to),
                new PhoneNumber(twilioPhoneNumber),
                "Your OTP code is: "  + otpCode
        ).create();

        System.out.println("SMS sent with SID: " + message.getSid());
    }
}
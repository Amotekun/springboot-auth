package com.example.auth.services;


import com.example.auth.entities.UserEntity;
import com.example.auth.exception.UsernameAlreadyExistsException;
import com.example.auth.repositories.UserRepository;

import com.example.auth.util.TokenUtility;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@Transactional(readOnly = true)
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenUtility tokenUtility;
    private final EmailService emailService;


    public UserService(
            UserRepository userRepository,
            TokenUtility tokenUtility,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.tokenUtility = tokenUtility;
    }

    @Transactional
    public String register(UserEntity userEntity, String appUrl) {
        if (userRepository.findByUsername(userEntity.getUsername()).isPresent()) {
            throw new UsernameAlreadyExistsException();
        };

        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        userEntity.setActivationToken(tokenUtility.generateToken());
        userEntity.setIsVerified(false);
        userRepository.save(userEntity);

        String activationLink = appUrl + "/activation?token=" + userEntity.getActivationToken();

        try {
            emailService.sendActivationEmail(userEntity.getEmail(), activationLink);
        } catch (MessagingException e) {
            throw new IllegalStateException("Failed to send activation email", e);
        }

        return "User created. Please check your email to activate your account";
    }

    @Transactional
    public boolean userVerifiedTokenStatus(String token) {
        var verifyCheck = userRepository.findByActivationToken(token);
        if (verifyCheck.isPresent()) {
            userRepository.verifyUser(token);
            return true;
        }
        return false;
    }
}

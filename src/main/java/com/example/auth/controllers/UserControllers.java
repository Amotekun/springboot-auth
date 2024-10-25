package com.example.auth.controllers;

import com.example.auth.entities.User;
import com.example.auth.services.TokenService;
import com.example.auth.services.UserService;
import com.example.auth.services.EmailService;
import com.example.auth.util.JwtTokenProvider;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class UserControllers {

    private static final Logger LOG = LoggerFactory.getLogger(UserControllers.class);
    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${application.url}")
    private String appUrl;

    public UserControllers(
            UserService userService,
            EmailService emailService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider
    ) {
        this.userService = userService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to Java");
    }

    @PostMapping("/register")
    public ResponseEntity<String> saveUser(@RequestBody User user) {

        String activationToken = userService.createUser(user);

        String activationLink = appUrl + "/activation?token=" + activationToken;

        try {
            emailService.sendActivationEmail(user.getEmail(), activationLink);
        } catch (MessagingException e) {
            return ResponseEntity.internalServerError().body("Failed to send activation email");
        }
        return ResponseEntity.ok("User created. Please check your email to activate your account");
    }

    @GetMapping("/activation")
    public ResponseEntity<String> activateUser(@RequestParam("token") String token) {
        boolean verified = userService.userVerifiedTokenStatus(token);
        if (verified) {
            return ResponseEntity.ok("User activated successfully. You can now log in.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid activation token.");
        }

    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials) {
        String username = credentials.get("username");
        String password = credentials.get("password");

       try {
           Authentication authentication = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(username, password)
           );

           String token = jwtTokenProvider.generateToken(authentication);
           return ResponseEntity.ok(token);
       } catch (AuthenticationException e) {
           return ResponseEntity.status(401).body("Invalid credentials");
       }
    }

    @GetMapping("/user")
    public ResponseEntity<?> getUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
        }

        String username = authentication.getName();
        var authorities = authentication.getAuthorities();

        return ResponseEntity.ok(Map.of(
                "username", username,
                "roles", authorities.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList())
        ));
    }
}

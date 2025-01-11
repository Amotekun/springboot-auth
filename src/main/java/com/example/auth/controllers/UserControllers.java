package com.example.auth.controllers;

import com.example.auth.entities.UserEntity;
import com.example.auth.services.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserControllers {

    private final UserService userService;
    private final TokenService tokenService;
    private final AuthenticationService authenticationService;

    @Value("${application.url}")
    private String appUrl;

    public UserControllers(
            UserService userService,
            TokenService tokenService,
            AuthenticationService authenticationService
    ) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.authenticationService = authenticationService;
    }

    @GetMapping("/")
    public ResponseEntity<String> home() {
        return ResponseEntity.ok("Welcome to Java");
    }

    @PostMapping("/register")
    public ResponseEntity<String> saveUser(@RequestBody UserEntity userEntity) {
        try {
            String responseMessage = userService.register(userEntity, appUrl);
            return ResponseEntity.ok(responseMessage);
        } catch (IllegalStateException e) {
            return ResponseEntity.internalServerError().body(e.getMessage());
        }
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
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        try {
            String result = authenticationService.login(credentials, response);
            return ResponseEntity.ok(result);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(400).body(e.getMessage());
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body("Invalid credentials. ");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Login failed: " + e.getMessage());
        }
    }


    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        try {
            Map<String, String> tokens = tokenService.refreshToken(request, response);
            return ResponseEntity.ok(tokens);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred" + e.getMessage());
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


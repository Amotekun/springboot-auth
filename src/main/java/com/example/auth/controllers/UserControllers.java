package com.example.auth.controllers;

import com.example.auth.entities.PasswordReset;
import com.example.auth.entities.User;
import com.example.auth.services.PasswordResetService;
import com.example.auth.services.TokenService;
import com.example.auth.services.UserService;
import com.example.auth.services.EmailService;
import com.example.auth.util.JwtTokenProvider;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
public class UserControllers {

    private static final Logger LOG = LoggerFactory.getLogger(UserControllers.class);
    private final UserService userService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenService tokenService;
    private final PasswordResetService passwordResetService;

    @Value("${application.url}")
    private String appUrl;

    public UserControllers(
            UserService userService,
            EmailService emailService,
            PasswordResetService passwordResetService,
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            TokenService tokenService
    ) {
        this.userService = userService;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.tokenService = tokenService;
        this.passwordResetService = passwordResetService;
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
    public ResponseEntity<String> login(@RequestBody Map<String, String> credentials, HttpServletResponse response) {
        String username = credentials.get("username");
        String password = credentials.get("password");

       try {
           Authentication authentication = authenticationManager.authenticate(
                   new UsernamePasswordAuthenticationToken(username, password)
           );

           // Generate refresh token (Could be a UUID or JWT with a longer expiration)
           String accessToken = jwtTokenProvider.generateToken(authentication);

           // Generate refresh token (could be a UUID or JWT with a longer expiration)
           String refreshToken = UUID.randomUUID().toString();

           // Save refresh token in the database
           tokenService.saveRefreshToken(username, refreshToken);

           // Set refresh token in an HTTP-only cookie
           Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
           refreshCookie.setHttpOnly(true);
           refreshCookie.setPath("/"); // for accessibility to the entire application
           refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days (1 week)
           response.addCookie(refreshCookie);


           return ResponseEntity.ok(accessToken);
       } catch (AuthenticationException e) {
           return ResponseEntity.status(401).body("Invalid credentials");
       }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No refresh token provided");
        }

        // Validate refresh token with the database
        var userOpt = tokenService.getUserByRefreshToken(refreshToken);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }

        var user = userOpt.get();

        // Convert role (String) to a list of GrantedAuthority objects
        Collection<? extends GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(user.getRole()));

        //Generate new access token
        Authentication authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, authorities);
        String newAccessToken = jwtTokenProvider.generateToken(authentication);

        String newRefreshToken = UUID.randomUUID().toString();
        tokenService.saveRefreshToken(user.getUsername(), newRefreshToken);

        Cookie refreshCookie = new Cookie("refreshToken", newRefreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(refreshCookie);

        return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");

        try {
            passwordResetService.createPasswordResetToken(email);
            return ResponseEntity.ok("Password reset email sent.");
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send password reset email.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
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


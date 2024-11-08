package com.example.auth.repositories;

import com.example.auth.entities.PasswordReset;
import org.springframework.jdbc.core.JdbcTemplate;

public class PasswordResetRepository {
    private final JdbcTemplate jdbcTemplate;

    public PasswordResetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(PasswordReset passwordReset) {
        String sql = "INSERT INTO password_reset (id, user_id, token, expires_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, passwordReset.getId(), passwordReset.getUserId(), passwordReset.getToken(), passwordReset.getExpiresAt());
    }
}

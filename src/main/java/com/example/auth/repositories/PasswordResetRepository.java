package com.example.auth.repositories;

import com.example.auth.entities.PasswordResetEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

@Repository
public class PasswordResetRepository {
    private final JdbcTemplate jdbcTemplate;

    public PasswordResetRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void save(PasswordResetEntity passwordResetEntity) {
        String sql = "INSERT INTO password_reset (id, user_id, token, expires_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, passwordResetEntity.getId(), passwordResetEntity.getUserId(), passwordResetEntity.getToken(), passwordResetEntity.getExpiresAt());
    }

    public Optional<PasswordResetEntity> findByToken(String token) {
        String sql = "SELECT * FROM password_reset WHERE token = ?";
        return jdbcTemplate.query(sql, this::mapToPasswordReset, token).stream().findFirst();
    }

    public void deleteByUserId(String userId) {
        String sql = "DELETE FROM password_reset WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    private PasswordResetEntity mapToPasswordReset(ResultSet rs, int rowNum) throws SQLException {
        return new PasswordResetEntity(
                rs.getString("user_id"),
                rs.getString("token"),
                rs.getTimestamp("expires_at").toInstant()
        );
    }
}

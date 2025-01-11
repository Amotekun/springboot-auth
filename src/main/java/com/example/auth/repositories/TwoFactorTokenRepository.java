package com.example.auth.repositories;

import com.example.auth.entities.TwoFactorTokenEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TwoFactorTokenRepository {

    private final JdbcTemplate jdbcTemplate;

    public TwoFactorTokenRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int getAttemptCount(String userId) {
        String sql = "SELECT attempt_count FROM two_factor_tokens WHERE user_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId);
        return count != null ? count : 0;

    }

    public void incrementAttemptCount (String userId) {
        String sql = "UPDATE two_factor_tokens SET attempt_count = attempt_count + 1 WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void deleteByUserId(String userId) {
        String sql = "DELETE FROM two_factor_tokens WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public boolean isValidOtp(String userId, String token) {
        String sql = "SELECT COUNT(*) FROM two_factor_tokens WHERE user_id = ? AND token = ? AND expires_at > NOW()";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, userId, token);
        return count != null && count > 0;
    }

    public void save(TwoFactorTokenEntity twoFactorToken) {
        String sql = "INSERT INTO two_factor_tokens (id, user_id, token, expires_at) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                twoFactorToken.getId(),
                twoFactorToken.getUserId(),
                twoFactorToken.getToken(),
                twoFactorToken.getExpiresAt()
        );
    }
}

package com.example.auth.repositories;

import com.example.auth.entities.TwoFactorConfirmationEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class TwoFactorConfirmationRepository {

    private final JdbcTemplate jdbcTemplate;

    public TwoFactorConfirmationRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void deleteByUserId(String userId) {
        String sql = "DELETE FROM two_factor_confirmations WHERE user_id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void save(TwoFactorConfirmationEntity twoFactorConfirmation) {
        String sql = "INSERT INTO two_factor_confirmations (id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(
                sql,
                twoFactorConfirmation.getId(),
                twoFactorConfirmation.getUserId()
        );
    }
}

package com.example.auth.repositories;

import com.example.auth.entities.UserEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserRepository  {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<UserEntity> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        List<UserEntity> userEntity = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), username);

        if (userEntity.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(userEntity.getFirst());
        }
    }

    public Optional<UserEntity> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        List<UserEntity> userEntity = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), id);

        if (userEntity.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(userEntity.getFirst());
        }
    }

    public Optional<UserEntity> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email = ?";

        List<UserEntity> userEntity = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), email);

        if (userEntity.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(userEntity.getFirst());
        }
    }

    // ACTIVATION TOKEN IMPLEMENTATION
    public Optional<UserEntity> findByActivationToken(String token) {
        String sql = "SELECT * FROM users WHERE activationToken = ?";
        List<UserEntity> userEntity = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), token);
        return userEntity.isEmpty() ? Optional.empty() : Optional.of(userEntity.getFirst());
    }

    // this is to mark that the user is verified;
    public void verifyUser(String token) {
        String sql = "UPDATE users SET isVerified = true, activationToken = NULL WHERE activationToken = ?";
        jdbcTemplate.update(sql, token);
    }

    public boolean  isUserVerified(String username) {
        String sql = "SELECT isVerified FROM users WHERE username = ?";
        Boolean isVerified = jdbcTemplate.queryForObject(sql, Boolean.class, username);

        return Boolean.TRUE.equals(isVerified);

    }

    // === ACTIVATION TOKEN END ===

    public void save(UserEntity userEntity) {
        String sql = "INSERT INTO users(id, username, password, email, activationToken, isVerified, role) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(
                sql,
                userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getPassword(),
                userEntity.getEmail(),
                userEntity.getActivationToken(),
                userEntity.getIsVerified() ? 1 : 0, // Convert boolean to tinyint
                userEntity.getRole() != null ? userEntity.getRole() : "USER"
        );
    }

    public void updatePassword(String userId, String newPassword) {
        String sql = "UPDATE users SET password = ? WHERE id = ?";
        jdbcTemplate.update(sql, newPassword, userId);
    }

    public void enableTwoFactor(String userId) {
        String sql = "UPDATE users SET two_factor_enabled = TRUE WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    public void disableTwoFactor(String userId) {
        String sql = "UPDATE users SET two_factor_enabled = FALSE WHERE id = ?";
        jdbcTemplate.update(sql, userId);
    }

    // REFRESH TOKEN IMPLEMENTATION
    public Optional<UserEntity> findByRefreshToken(String refreshToken) {
        String sql = "SELECT * FROM users WHERE refreshToken = ?";
        List<UserEntity> userEntities = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), refreshToken);
        return userEntities.isEmpty() ? Optional.empty() : Optional.of(userEntities.getFirst());
    }

    public void updateRefreshToken(String username, String refreshToken) {
        String sql = "UPDATE users SET refreshToken = ? WHERE username = ?";
        jdbcTemplate.update(sql, refreshToken, username);
    }

    public void clearRefreshToken(String username) {
        String sql = "UPDATE users SET refreshToken = NULL WHERE username = ?";
        jdbcTemplate.update(sql, username);
    }

    private UserEntity mapRowToUser(ResultSet rs) throws SQLException {
        UserEntity userEntity = new UserEntity();
        userEntity.setId(rs.getString("id"));
        userEntity.setEmail(rs.getString("email"));
        userEntity.setUsername(rs.getString("username"));
        userEntity.setPassword(rs.getString("password"));
        userEntity.setRole(rs.getString("role"));
        userEntity.setRefreshToken(rs.getString("refreshToken"));
        userEntity.setIsVerified(rs.getBoolean("isVerified"));
        userEntity.setTwoFactorEnabled(rs.getBoolean("two_factor_enabled"));
        return userEntity;
    }
}
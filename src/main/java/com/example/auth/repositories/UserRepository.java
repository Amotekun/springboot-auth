package com.example.auth.repositories;

import com.example.auth.entities.User;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class UserRepository  {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), username);

        if (user.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(user.getFirst());
        }
    }

    public Optional<User> findById(String id) {
        String sql = "SELECT * FROM users WHERE id = ?";

        List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), id);

        if (user.isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(user.getFirst());
        }
    }

    public Optional<User> findByActivationToken(String token) {
        String sql = "SELECT * FROM users WHERE activationToken = ?";
        List<User> user = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowToUser(rs), token);
        return user.isEmpty() ? Optional.empty() : Optional.of(user.getFirst());
    }

    // this is to mark that the user is verified;
    public void verifyUser(String token) {
        String sql = "UPDATE users SET isVerified = true, activationToken = NULL WHERE activationToken = ?";
        jdbcTemplate.update(sql, token);
    }

    public boolean  isUserVerified(String email) {
        String sql = "SELECT isVerified FROM users WHERE email = ?";
        Boolean isVerified = jdbcTemplate.queryForObject(sql, Boolean.class, email);

        return Boolean.TRUE.equals(isVerified);

    }

    public void save(User user) {
        String sql = "INSERT INTO users(username, password, email, activationToken) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, user.getUsername(), user.getPassword(), user.getEmail(), user.getActivationToken());
    }


    private User mapRowToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getString("id"));
        user.setEmail(rs.getString("email"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(rs.getString("role"));

        return user;
    }

}
package com.example.auth.repositories;

import com.example.auth.entities.UserContactInfoEntity;
import com.example.auth.entities.UserEntity;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class UserContactInfoRepository {

    private final JdbcTemplate jdbcTemplate;

    public  UserContactInfoRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private RowMapper<UserContactInfoEntity> rowMapper() {
        return (rs, rowNum) -> {
            UserContactInfoEntity contactInfo = new UserContactInfoEntity(
                    rs.getString("user_id"),
                    rs.getString("phone_number")
            );
            contactInfo.setPhoneVerified(rs.getBoolean("phone_verified"));
            return contactInfo;
        };
    }

    public Optional<UserContactInfoEntity> findById(String userId) {
        String sql = "SELECT * FROM user_contact_info WHERE user_id = ?";
        try {
            UserContactInfoEntity contactInfo = jdbcTemplate.queryForObject(sql, rowMapper(), userId);
            return Optional.ofNullable(contactInfo);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void verifyUserPhoneNumber(String userId) {
        String sql = "UPDATE user_contact_info SET phone_verified = TRUE WHERE user_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, userId);

        if (rowsUpdated == 0) {
            throw new IllegalStateException("Failed to verify phone number: User ID not found.");
        }
    }

    public Optional<String> getPhoneNumberByUserId(String userId) {
        String sql = "SELECT phone_number FROM user_contact_info WHERE user_id = ? AND phone_verified = TRUE";
        return Optional.ofNullable(jdbcTemplate.queryForObject(sql, String.class, userId));
    }

    public void saveOrUpdate(UserContactInfoEntity userContactInfo) {
        String sql = """
                UPDATE user_contact_info
                SET phone_number = ?, phone_verified = ?
                WHERE user_id = ?
                """;

        int rowsUpdated = jdbcTemplate.update(
                sql,
                userContactInfo.getPhoneNumber(),
                userContactInfo.isPhoneVerified(),
                userContactInfo.getUserId()

        );

        if (rowsUpdated == 0) {
            String insertSql = """
                    INSERT INTO user_contact_info (id, user_id, phone_number, phone_verified)
                    VALUES(?, ?, ?, ?)
                    """;

            jdbcTemplate.update(
                    insertSql,
                    userContactInfo.getId(),
                    userContactInfo.getUserId(),
                    userContactInfo.getPhoneNumber(),
                    userContactInfo.isPhoneVerified()
            );
        }
    }
}


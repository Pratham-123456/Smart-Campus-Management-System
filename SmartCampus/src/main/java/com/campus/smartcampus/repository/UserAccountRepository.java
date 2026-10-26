package com.campus.smartcampus.repository;

import com.campus.smartcampus.model.UserAccount;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class UserAccountRepository {

    private final JdbcTemplate jdbcTemplate;

    public UserAccountRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private static class UserAccountRowMapper implements RowMapper<UserAccount> {
        @Override
        public UserAccount mapRow(ResultSet rs, int rowNum) throws SQLException {
            UserAccount acc = new UserAccount();
            acc.setAccountId(rs.getLong("Account_ID"));
            acc.setUsername(rs.getString("Username"));
            acc.setPasswordHash(rs.getString("Password_Hash"));
            acc.setRole(rs.getString("Role"));

            long sId = rs.getLong("Student_ID");
            acc.setStudentId(rs.wasNull() ? null : sId);

            long fId = rs.getLong("Faculty_ID");
            acc.setFacultyId(rs.wasNull() ? null : fId);

            acc.setIsActive(rs.getInt("Is_Active"));

            Timestamp cat = rs.getTimestamp("Created_At");
            acc.setCreatedAt(cat != null ? cat.toLocalDateTime() : null);

            Timestamp llog = rs.getTimestamp("Last_Login");
            acc.setLastLogin(llog != null ? llog.toLocalDateTime() : null);

            return acc;
        }
    }

    private final UserAccountRowMapper rowMapper = new UserAccountRowMapper();

    public Optional<UserAccount> findByUsername(String username) {
        if (username == null) return Optional.empty();
        String sql = "SELECT * FROM USER_ACCOUNT WHERE LOWER(Username) = LOWER(?) AND Is_Active = 1";
        try {
            UserAccount acc = jdbcTemplate.queryForObject(sql, rowMapper, username.trim());
            return Optional.ofNullable(acc);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public boolean existsByUsername(String username) {
        if (username == null) return false;
        String sql = "SELECT COUNT(*) FROM USER_ACCOUNT WHERE LOWER(Username) = LOWER(?)";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, username.trim());
        return count != null && count > 0;
    }

    public int countAdmins() {
        String sql = "SELECT COUNT(*) FROM USER_ACCOUNT WHERE Role = 'ADMINISTRATOR' AND Is_Active = 1";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class);
        return count != null ? count : 0;
    }

    public void createAccount(UserAccount acc) {
        String sql = "INSERT INTO USER_ACCOUNT (Username, Password_Hash, Role, Student_ID, Faculty_ID, Is_Active, Created_At) " +
                     "VALUES (?, ?, ?, ?, ?, 1, CURRENT_TIMESTAMP)";
        jdbcTemplate.update(sql,
                acc.getUsername().trim(),
                acc.getPasswordHash(),
                acc.getRole(),
                acc.getStudentId(),
                acc.getFacultyId()
        );
    }

    public void updateLastLogin(Long accountId) {
        String sql = "UPDATE USER_ACCOUNT SET Last_Login = CURRENT_TIMESTAMP WHERE Account_ID = ?";
        jdbcTemplate.update(sql, accountId);
    }

    public Optional<UserAccount> findByStudentId(Long studentId) {
        String sql = "SELECT * FROM USER_ACCOUNT WHERE Student_ID = ? AND Is_Active = 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, studentId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public Optional<UserAccount> findByFacultyId(Long facultyId) {
        String sql = "SELECT * FROM USER_ACCOUNT WHERE Faculty_ID = ? AND Is_Active = 1";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, rowMapper, facultyId));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public List<UserAccount> listAllAccounts() {
        String sql = "SELECT * FROM USER_ACCOUNT ORDER BY Account_ID ASC";
        return jdbcTemplate.query(sql, rowMapper);
    }
}

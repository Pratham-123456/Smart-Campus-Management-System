package com.campus.smartcampus.repository;

import com.campus.smartcampus.dto.LoginResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CommonRepository {

    private final JdbcTemplate jdbcTemplate;

    public CommonRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean testConnection() {
        try {
            Integer result = jdbcTemplate.queryForObject("SELECT 1 FROM DUAL", Integer.class);
            return result != null && result == 1;
        } catch (Exception e) {
            System.err.println("Database connection test error: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Optional<LoginResponse> verifyStudent(Long studentId, String email) {
        String sql = """
            SELECT s.Student_ID, s.Name, s.Email, p.Program_Name, d.Department_Name
            FROM STUDENT s
            JOIN PROGRAM p ON s.Program_ID = p.Program_ID
            JOIN DEPARTMENT d ON p.Department_ID = d.Department_ID
            WHERE s.Student_ID = ? AND LOWER(s.Email) = LOWER(?)
        """;
        List<LoginResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LoginResponse resp = new LoginResponse();
            resp.setRole("STUDENT");
            resp.setId(rs.getLong("Student_ID"));
            resp.setName(rs.getString("Name"));
            resp.setEmail(rs.getString("Email"));
            resp.setProgramName(rs.getString("Program_Name"));
            resp.setDepartmentName(rs.getString("Department_Name"));
            resp.setToken("STU-" + rs.getLong("Student_ID") + "-" + System.currentTimeMillis());
            return resp;
        }, studentId, email != null ? email.trim() : "");

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<LoginResponse> verifyFaculty(Long facultyId, String email) {
        String sql = """
            SELECT f.Faculty_ID, f.Name, f.Email, d.Department_Name
            FROM FACULTY f
            JOIN DEPARTMENT d ON f.Department_ID = d.Department_ID
            WHERE f.Faculty_ID = ? AND LOWER(f.Email) = LOWER(?)
        """;
        List<LoginResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LoginResponse resp = new LoginResponse();
            resp.setRole("FACULTY");
            resp.setId(rs.getLong("Faculty_ID"));
            resp.setName(rs.getString("Name"));
            resp.setEmail(rs.getString("Email"));
            resp.setDepartmentName(rs.getString("Department_Name"));
            resp.setProgramName("Faculty Department: " + rs.getString("Department_Name"));
            resp.setToken("FAC-" + rs.getLong("Faculty_ID") + "-" + System.currentTimeMillis());
            return resp;
        }, facultyId, email != null ? email.trim() : "");

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<LoginResponse> getStudentLoginDetails(Long studentId) {
        String sql = """
            SELECT s.Student_ID, s.Name, s.Email, p.Program_Name, d.Department_Name
            FROM STUDENT s
            JOIN PROGRAM p ON s.Program_ID = p.Program_ID
            JOIN DEPARTMENT d ON p.Department_ID = d.Department_ID
            WHERE s.Student_ID = ?
        """;
        List<LoginResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LoginResponse resp = new LoginResponse();
            resp.setRole("STUDENT");
            resp.setId(rs.getLong("Student_ID"));
            resp.setName(rs.getString("Name"));
            resp.setEmail(rs.getString("Email"));
            resp.setProgramName(rs.getString("Program_Name"));
            resp.setDepartmentName(rs.getString("Department_Name"));
            return resp;
        }, studentId);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public Optional<LoginResponse> getFacultyLoginDetails(Long facultyId) {
        String sql = """
            SELECT f.Faculty_ID, f.Name, f.Email, d.Department_Name
            FROM FACULTY f
            JOIN DEPARTMENT d ON f.Department_ID = d.Department_ID
            WHERE f.Faculty_ID = ?
        """;
        List<LoginResponse> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            LoginResponse resp = new LoginResponse();
            resp.setRole("FACULTY");
            resp.setId(rs.getLong("Faculty_ID"));
            resp.setName(rs.getString("Name"));
            resp.setEmail(rs.getString("Email"));
            resp.setDepartmentName(rs.getString("Department_Name"));
            resp.setProgramName("Department: " + rs.getString("Department_Name"));
            return resp;
        }, facultyId);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }
}


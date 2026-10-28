package com.campus.smartcampus.repository;

import com.campus.smartcampus.dto.StudentAttendanceMarksDTO;
import com.campus.smartcampus.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class FacultyRepository {

    private final JdbcTemplate jdbcTemplate;

    public FacultyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /** True when the student holds an active (ENROLLED) enrollment in the offering. */
    public boolean isStudentEnrolled(Long studentId, Long offeringId) {
        String sql = "SELECT COUNT(*) FROM ENROLLMENT WHERE Student_ID = ? AND Offering_ID = ? AND Status = 'ENROLLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, offeringId);
        return count != null && count > 0;
    }

    /** Highest Classes_Attended recorded for the offering; Classes_Held may not drop below it. */
    public int getMaxClassesAttended(Long offeringId) {
        String sql = "SELECT NVL(MAX(Classes_Attended), 0) FROM ATTENDANCE WHERE Offering_ID = ?";
        Integer max = jdbcTemplate.queryForObject(sql, Integer.class, offeringId);
        return max != null ? max : 0;
    }

    public Optional<Faculty> findById(Long facultyId) {
        String sql = """
            SELECT f.Faculty_ID, f.Name, f.Email, f.Phone, f.Designation,
                   f.Department_ID, d.Department_Name
            FROM FACULTY f
            JOIN DEPARTMENT d ON f.Department_ID = d.Department_ID
            WHERE f.Faculty_ID = ?
        """;
        List<Faculty> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Faculty f = new Faculty();
            f.setFacultyId(rs.getLong("Faculty_ID"));
            f.setName(rs.getString("Name"));
            f.setEmail(rs.getString("Email"));
            f.setPhone(rs.getString("Phone"));
            f.setDesignation(rs.getString("Designation"));
            f.setDepartmentId(rs.getLong("Department_ID"));
            f.setDepartmentName(rs.getString("Department_Name"));
            return f;
        }, facultyId);

        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    public List<CourseOffering> getOfferings(Long facultyId) {
        String sql = """
            SELECT co.Offering_ID, co.Course_ID, c.Course_Name, c.Credits, c.Course_Type,
                   co.Faculty_ID, f.Name AS Faculty_Name, co.Semester, co.Academic_Year,
                   co.Section, co.Room_No, co.Max_Seats, co.Classes_Held,
                   (SELECT COUNT(*) FROM ENROLLMENT en WHERE en.Offering_ID = co.Offering_ID AND en.Status = 'ENROLLED') AS Enrolled_Count
            FROM COURSE_OFFERING co
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
            WHERE co.Faculty_ID = ?
            ORDER BY co.Semester, c.Course_Name, co.Section
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            CourseOffering co = new CourseOffering();
            co.setOfferingId(rs.getLong("Offering_ID"));
            co.setCourseId(rs.getLong("Course_ID"));
            co.setCourseName(rs.getString("Course_Name"));
            co.setCredits(rs.getInt("Credits"));
            co.setCourseType(rs.getString("Course_Type"));
            co.setFacultyId(rs.getLong("Faculty_ID"));
            co.setFacultyName(rs.getString("Faculty_Name"));
            co.setSemester(rs.getInt("Semester"));
            co.setAcademicYear(rs.getString("Academic_Year"));
            co.setSection(rs.getString("Section"));
            co.setRoomNo(rs.getString("Room_No"));
            co.setMaxSeats(rs.getInt("Max_Seats"));
            co.setClassesHeld(rs.getInt("Classes_Held"));
            int enrolled = rs.getInt("Enrolled_Count");
            co.setEnrolledCount(enrolled);
            co.setAvailableSeats(Math.max(0, co.getMaxSeats() - enrolled));
            return co;
        }, facultyId);
    }

    public boolean isAssignedToOffering(Long facultyId, Long offeringId) {
        String sql = "SELECT COUNT(*) FROM COURSE_OFFERING WHERE Offering_ID = ? AND Faculty_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, offeringId, facultyId);
        return count != null && count > 0;
    }

    public Integer getClassesHeld(Long offeringId) {
        String sql = "SELECT Classes_Held FROM COURSE_OFFERING WHERE Offering_ID = ?";
        List<Integer> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), offeringId);
        return list.isEmpty() ? 0 : list.get(0);
    }

    public void updateClassesHeld(Long offeringId, Integer classesHeld) {
        String sql = "UPDATE COURSE_OFFERING SET Classes_Held = ? WHERE Offering_ID = ?";
        jdbcTemplate.update(sql, classesHeld, offeringId);
    }

    public List<StudentAttendanceMarksDTO> getEnrolledStudentsWithPerformance(Long offeringId) {
        String sql = """
            SELECT s.Student_ID, s.Name AS Student_Name, s.Email,
                   p.Program_Name,
                   co.Classes_Held,
                   NVL(a.Classes_Attended, 0) AS Classes_Attended,
                   NVL(m.Continuous_Assessment_Marks, 0) AS CA_Marks,
                   NVL(m.Midterm_Marks, 0) AS Midterm_Marks,
                   NVL(m.Final_Exam_Marks, 0) AS Final_Marks,
                   (NVL(m.Continuous_Assessment_Marks, 0) + NVL(m.Midterm_Marks, 0) + NVL(m.Final_Exam_Marks, 0)) AS Total_Marks
            FROM ENROLLMENT en
            JOIN STUDENT s ON en.Student_ID = s.Student_ID
            JOIN PROGRAM p ON s.Program_ID = p.Program_ID
            JOIN COURSE_OFFERING co ON en.Offering_ID = co.Offering_ID
            LEFT JOIN ATTENDANCE a ON a.Student_ID = s.Student_ID AND a.Offering_ID = co.Offering_ID
            LEFT JOIN MARKS m ON m.Student_ID = s.Student_ID AND m.Offering_ID = co.Offering_ID
            WHERE en.Offering_ID = ? AND en.Status = 'ENROLLED'
            ORDER BY s.Student_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            StudentAttendanceMarksDTO dto = new StudentAttendanceMarksDTO();
            dto.setStudentId(rs.getLong("Student_ID"));
            dto.setStudentName(rs.getString("Student_Name"));
            dto.setEmail(rs.getString("Email"));
            dto.setProgramName(rs.getString("Program_Name"));
            dto.setClassesHeld(rs.getInt("Classes_Held"));
            dto.setClassesAttended(rs.getInt("Classes_Attended"));
            dto.setContinuousAssessmentMarks(rs.getDouble("CA_Marks"));
            dto.setMidtermMarks(rs.getDouble("Midterm_Marks"));
            dto.setFinalExamMarks(rs.getDouble("Final_Marks"));
            dto.setTotalMarks(rs.getDouble("Total_Marks"));
            if (dto.getClassesHeld() != null && dto.getClassesHeld() > 0) {
                double pct = ((double) dto.getClassesAttended() / dto.getClassesHeld()) * 100.0;
                dto.setAttendancePercentage(Math.round(pct * 100.0) / 100.0);
            } else {
                dto.setAttendancePercentage(0.0);
            }
            return dto;
        }, offeringId);
    }

    public void updateAttendance(Long studentId, Long offeringId, Integer classesAttended) {
        String checkSql = "SELECT COUNT(*) FROM ATTENDANCE WHERE Student_ID = ? AND Offering_ID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, offeringId);
        if (count != null && count > 0) {
            String updateSql = "UPDATE ATTENDANCE SET Classes_Attended = ? WHERE Student_ID = ? AND Offering_ID = ?";
            jdbcTemplate.update(updateSql, classesAttended, studentId, offeringId);
        } else {
            String nextIdSql = "SELECT NVL(MAX(Attendance_ID), 600) + 1 FROM ATTENDANCE";
            Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
            String insertSql = "INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, nextId, studentId, offeringId, classesAttended);
        }
    }

    public void updateMarks(Long studentId, Long offeringId, Double ca, Double midterm, Double finalExam) {
        String checkSql = "SELECT COUNT(*) FROM MARKS WHERE Student_ID = ? AND Offering_ID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, offeringId);
        if (count != null && count > 0) {
            String updateSql = "UPDATE MARKS SET Continuous_Assessment_Marks = ?, Midterm_Marks = ?, Final_Exam_Marks = ? WHERE Student_ID = ? AND Offering_ID = ?";
            jdbcTemplate.update(updateSql, ca, midterm, finalExam, studentId, offeringId);
        } else {
            String nextIdSql = "SELECT NVL(MAX(Mark_ID), 700) + 1 FROM MARKS";
            Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
            String insertSql = "INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, nextId, studentId, offeringId, ca, midterm, finalExam);
        }
    }

    public List<Exam> getExamsForFaculty(Long facultyId) {
        String sql = """
            SELECT ex.Exam_ID, ex.Offering_ID, c.Course_Name, co.Section, f.Name AS Faculty_Name,
                   ex.Exam_Type, ex.Exam_Date, ex.Start_Time, ex.End_Time, ex.Venue,
                   ex.Max_Marks, ex.Status,
                   (SELECT COUNT(*) FROM EXAM_REGISTRATION er WHERE er.Exam_ID = ex.Exam_ID) AS Registered_Count
            FROM EXAM ex
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
            WHERE co.Faculty_ID = ?
            ORDER BY ex.Exam_Date DESC, ex.Start_Time
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Exam ex = new Exam();
            ex.setExamId(rs.getLong("Exam_ID"));
            ex.setOfferingId(rs.getLong("Offering_ID"));
            ex.setCourseName(rs.getString("Course_Name"));
            ex.setSection(rs.getString("Section"));
            ex.setFacultyName(rs.getString("Faculty_Name"));
            ex.setExamType(rs.getString("Exam_Type"));
            if (rs.getDate("Exam_Date") != null) {
                ex.setExamDate(rs.getDate("Exam_Date").toLocalDate());
            }
            if (rs.getTimestamp("Start_Time") != null) {
                ex.setStartTime(rs.getTimestamp("Start_Time").toLocalDateTime());
            }
            if (rs.getTimestamp("End_Time") != null) {
                ex.setEndTime(rs.getTimestamp("End_Time").toLocalDateTime());
            }
            ex.setVenue(rs.getString("Venue"));
            ex.setMaxMarks(rs.getDouble("Max_Marks"));
            ex.setStatus(rs.getString("Status"));
            return ex;
        }, facultyId);
    }
}

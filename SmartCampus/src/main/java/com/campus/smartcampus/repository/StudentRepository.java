package com.campus.smartcampus.repository;

import com.campus.smartcampus.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class StudentRepository {

    private final JdbcTemplate jdbcTemplate;

    public StudentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<Student> findById(Long studentId) {
        String sql = """
            SELECT s.Student_ID, s.Name, s.Email, s.Phone, s.DOB, s.Admission_Year,
                   s.Program_ID, p.Program_Name, p.Degree, d.Department_Name
            FROM STUDENT s
            JOIN PROGRAM p ON s.Program_ID = p.Program_ID
            JOIN DEPARTMENT d ON p.Department_ID = d.Department_ID
            WHERE s.Student_ID = ?
        """;
        List<Student> results = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Student s = new Student();
            s.setStudentId(rs.getLong("Student_ID"));
            s.setName(rs.getString("Name"));
            s.setEmail(rs.getString("Email"));
            s.setPhone(rs.getString("Phone"));
            if (rs.getDate("DOB") != null) {
                s.setDob(rs.getDate("DOB").toLocalDate());
            }
            s.setAdmissionYear(rs.getInt("Admission_Year"));
            s.setProgramId(rs.getLong("Program_ID"));
            s.setProgramName(rs.getString("Program_Name"));
            s.setDegree(rs.getString("Degree"));
            s.setDepartmentName(rs.getString("Department_Name"));
            return s;
        }, studentId);

        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    public List<Enrollment> getEnrollments(Long studentId) {
        String sql = """
            SELECT e.Enrollment_ID, e.Student_ID, s.Name AS Student_Name, e.Offering_ID,
                   c.Course_Name, c.Credits, co.Section, f.Name AS Faculty_Name,
                   e.Enrollment_Date, e.Status
            FROM ENROLLMENT e
            JOIN STUDENT s ON e.Student_ID = s.Student_ID
            JOIN COURSE_OFFERING co ON e.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
            WHERE e.Student_ID = ?
            ORDER BY co.Offering_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Enrollment e = new Enrollment();
            e.setEnrollmentId(rs.getLong("Enrollment_ID"));
            e.setStudentId(rs.getLong("Student_ID"));
            e.setStudentName(rs.getString("Student_Name"));
            e.setOfferingId(rs.getLong("Offering_ID"));
            e.setCourseName(rs.getString("Course_Name"));
            e.setCredits(rs.getInt("Credits"));
            e.setSection(rs.getString("Section"));
            e.setFacultyName(rs.getString("Faculty_Name"));
            if (rs.getDate("Enrollment_Date") != null) {
                e.setEnrollmentDate(rs.getDate("Enrollment_Date").toLocalDate());
            }
            e.setStatus(rs.getString("Status"));
            return e;
        }, studentId);
    }

    public List<CourseOffering> getAvailableOfferings(Long studentId) {
        String sql = """
            SELECT co.Offering_ID, co.Course_ID, c.Course_Name, c.Credits, c.Course_Type,
                   co.Faculty_ID, f.Name AS Faculty_Name, co.Semester, co.Academic_Year,
                   co.Section, co.Room_No, co.Max_Seats, co.Classes_Held,
                   (SELECT COUNT(*) FROM ENROLLMENT en WHERE en.Offering_ID = co.Offering_ID AND en.Status = 'ENROLLED') AS Enrolled_Count,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM ENROLLMENT en WHERE en.Offering_ID = co.Offering_ID AND en.Student_ID = ? AND en.Status = 'ENROLLED'
                   ) THEN 1 ELSE 0 END AS Is_Enrolled
            FROM COURSE_OFFERING co
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
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
            co.setIsEnrolled(rs.getInt("Is_Enrolled") == 1);
            return co;
        }, studentId);
    }

    public boolean isEnrolled(Long studentId, Long offeringId) {
        String sql = "SELECT COUNT(*) FROM ENROLLMENT WHERE Student_ID = ? AND Offering_ID = ? AND Status = 'ENROLLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, offeringId);
        return count != null && count > 0;
    }

    public int getEnrolledCount(Long offeringId) {
        String sql = "SELECT COUNT(*) FROM ENROLLMENT WHERE Offering_ID = ? AND Status = 'ENROLLED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, offeringId);
        return count != null ? count : 0;
    }

    public Integer getMaxSeats(Long offeringId) {
        String sql = "SELECT Max_Seats FROM COURSE_OFFERING WHERE Offering_ID = ?";
        List<Integer> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getInt(1), offeringId);
        return list.isEmpty() ? null : list.get(0);
    }

    public Long enrollStudent(Long studentId, Long offeringId) {
        String nextIdSql = "SELECT NVL(MAX(Enrollment_ID), 500) + 1 FROM ENROLLMENT";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);

        String insertSql = "INSERT INTO ENROLLMENT (Enrollment_ID, Student_ID, Offering_ID, Enrollment_Date, Status) VALUES (?, ?, ?, SYSDATE, 'ENROLLED')";
        jdbcTemplate.update(insertSql, nextId, studentId, offeringId);
        return nextId;
    }

    public void initAttendanceRecord(Long studentId, Long offeringId) {
        String checkSql = "SELECT COUNT(*) FROM ATTENDANCE WHERE Student_ID = ? AND Offering_ID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, offeringId);
        if (count == null || count == 0) {
            String nextIdSql = "SELECT NVL(MAX(Attendance_ID), 600) + 1 FROM ATTENDANCE";
            Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
            String insertSql = "INSERT INTO ATTENDANCE (Attendance_ID, Student_ID, Offering_ID, Classes_Attended) VALUES (?, ?, ?, 0)";
            jdbcTemplate.update(insertSql, nextId, studentId, offeringId);
        }
    }

    public void initMarksRecord(Long studentId, Long offeringId) {
        String checkSql = "SELECT COUNT(*) FROM MARKS WHERE Student_ID = ? AND Offering_ID = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, studentId, offeringId);
        if (count == null || count == 0) {
            String nextIdSql = "SELECT NVL(MAX(Mark_ID), 700) + 1 FROM MARKS";
            Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
            String insertSql = "INSERT INTO MARKS (Mark_ID, Student_ID, Offering_ID, Continuous_Assessment_Marks, Midterm_Marks, Final_Exam_Marks) VALUES (?, ?, ?, 0, 0, 0)";
            jdbcTemplate.update(insertSql, nextId, studentId, offeringId);
        }
    }

    public List<Attendance> getAttendance(Long studentId) {
        String sql = """
            SELECT a.Attendance_ID, a.Student_ID, s.Name AS Student_Name, a.Offering_ID,
                   c.Course_Name, co.Section, a.Classes_Attended, co.Classes_Held
            FROM ATTENDANCE a
            JOIN STUDENT s ON a.Student_ID = s.Student_ID
            JOIN COURSE_OFFERING co ON a.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE a.Student_ID = ?
              AND EXISTS (SELECT 1 FROM ENROLLMENT en WHERE en.Student_ID = a.Student_ID
                          AND en.Offering_ID = a.Offering_ID AND en.Status <> 'DROPPED')
            ORDER BY c.Course_Name
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Attendance a = new Attendance();
            a.setAttendanceId(rs.getLong("Attendance_ID"));
            a.setStudentId(rs.getLong("Student_ID"));
            a.setStudentName(rs.getString("Student_Name"));
            a.setOfferingId(rs.getLong("Offering_ID"));
            a.setCourseName(rs.getString("Course_Name"));
            a.setSection(rs.getString("Section"));
            a.setClassesHeld(rs.getInt("Classes_Held"));
            a.setClassesAttended(rs.getInt("Classes_Attended"));
            return a;
        }, studentId);
    }

    public List<Marks> getMarks(Long studentId) {
        String sql = """
            SELECT m.Mark_ID, m.Student_ID, s.Name AS Student_Name, m.Offering_ID,
                   c.Course_Name, c.Credits, co.Section, m.Continuous_Assessment_Marks,
                   m.Midterm_Marks, m.Final_Exam_Marks
            FROM MARKS m
            JOIN STUDENT s ON m.Student_ID = s.Student_ID
            JOIN COURSE_OFFERING co ON m.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE m.Student_ID = ?
              AND EXISTS (SELECT 1 FROM ENROLLMENT en WHERE en.Student_ID = m.Student_ID
                          AND en.Offering_ID = m.Offering_ID AND en.Status <> 'DROPPED')
            ORDER BY c.Course_Name
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Marks m = new Marks();
            m.setMarkId(rs.getLong("Mark_ID"));
            m.setStudentId(rs.getLong("Student_ID"));
            m.setStudentName(rs.getString("Student_Name"));
            m.setOfferingId(rs.getLong("Offering_ID"));
            m.setCourseName(rs.getString("Course_Name"));
            m.setCredits(rs.getInt("Credits"));
            m.setSection(rs.getString("Section"));
            m.setContinuousAssessmentMarks(rs.getDouble("Continuous_Assessment_Marks"));
            m.setMidtermMarks(rs.getDouble("Midterm_Marks"));
            m.setFinalExamMarks(rs.getDouble("Final_Exam_Marks"));
            m.computeTotal();
            m.setGradeCode(determineGrade(m.getPercentage()));
            m.setGradePoint(determineGradePoint(m.getGradeCode()));
            return m;
        }, studentId);
    }

    public List<Exam> getAvailableExams(Long studentId) {
        String sql = """
            SELECT ex.Exam_ID, ex.Offering_ID, c.Course_Name, co.Section, f.Name AS Faculty_Name,
                   ex.Exam_Type, ex.Exam_Date, ex.Start_Time, ex.End_Time, ex.Venue,
                   ex.Max_Marks, ex.Status,
                   er.Exam_Registration_ID
            FROM EXAM ex
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
            JOIN ENROLLMENT en ON co.Offering_ID = en.Offering_ID AND en.Student_ID = ? AND en.Status = 'ENROLLED'
            LEFT JOIN EXAM_REGISTRATION er ON ex.Exam_ID = er.Exam_ID AND er.Student_ID = ? AND er.Status = 'REGISTERED'
            ORDER BY ex.Exam_Date, ex.Start_Time
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
            Long regId = rs.getObject("Exam_Registration_ID", Long.class);
            ex.setIsRegistered(regId != null);
            ex.setExamRegistrationId(regId);
            return ex;
        }, studentId, studentId);
    }

    public boolean isExamRegistered(Long studentId, Long examId) {
        String sql = "SELECT COUNT(*) FROM EXAM_REGISTRATION WHERE Student_ID = ? AND Exam_ID = ? AND Status = 'REGISTERED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, examId);
        return count != null && count > 0;
    }

    public Long registerExam(Long studentId, Long examId) {
        String nextIdSql = "SELECT NVL(MAX(Exam_Registration_ID), 900) + 1 FROM EXAM_REGISTRATION";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String insertSql = "INSERT INTO EXAM_REGISTRATION (Exam_Registration_ID, Student_ID, Exam_ID, Registration_Date, Status) VALUES (?, ?, ?, SYSDATE, 'REGISTERED')";
        jdbcTemplate.update(insertSql, nextId, studentId, examId);
        return nextId;
    }

    public List<ExamResult> getExamResults(Long studentId) {
        String sql = """
            SELECT res.Exam_Result_ID, res.Exam_Registration_ID, reg.Student_ID, s.Name AS Student_Name,
                   ex.Exam_ID, c.Course_Name, ex.Exam_Type, ex.Exam_Date, ex.Max_Marks,
                   res.Marks_Obtained, res.Result_Status, res.Grade_Code, res.Published_Date,
                   (SELECT COUNT(*) FROM RE_EXAM_APPLICATION rea WHERE rea.Exam_Result_ID = res.Exam_Result_ID) AS Has_Reexam,
                   (SELECT rea.Status FROM RE_EXAM_APPLICATION rea WHERE rea.Exam_Result_ID = res.Exam_Result_ID AND ROWNUM = 1) AS Reexam_Status
            FROM EXAM_RESULT res
            JOIN EXAM_REGISTRATION reg ON res.Exam_Registration_ID = reg.Exam_Registration_ID
            JOIN STUDENT s ON reg.Student_ID = s.Student_ID
            JOIN EXAM ex ON reg.Exam_ID = ex.Exam_ID
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE reg.Student_ID = ? AND res.Result_Status <> 'WITHHELD'
            ORDER BY ex.Exam_Date DESC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ExamResult r = new ExamResult();
            r.setExamResultId(rs.getLong("Exam_Result_ID"));
            r.setExamRegistrationId(rs.getLong("Exam_Registration_ID"));
            r.setStudentId(rs.getLong("Student_ID"));
            r.setStudentName(rs.getString("Student_Name"));
            r.setExamId(rs.getLong("Exam_ID"));
            r.setCourseName(rs.getString("Course_Name"));
            r.setExamType(rs.getString("Exam_Type"));
            if (rs.getDate("Exam_Date") != null) {
                r.setExamDate(rs.getDate("Exam_Date").toLocalDate());
            }
            r.setMaxMarks(rs.getDouble("Max_Marks"));
            r.setMarksObtained(rs.getDouble("Marks_Obtained"));
            r.setResultStatus(rs.getString("Result_Status"));
            r.setGradeCode(rs.getString("Grade_Code"));
            if (rs.getDate("Published_Date") != null) {
                r.setPublishedDate(rs.getDate("Published_Date").toLocalDate());
            }
            r.setHasReExamApplied(rs.getInt("Has_Reexam") > 0);
            r.setReExamStatus(rs.getString("Reexam_Status"));
            return r;
        }, studentId);
    }

    public List<ReExamApplication> getReExamApplications(Long studentId) {
        String sql = """
            SELECT rea.ReExam_Application_ID, rea.Exam_Result_ID, reg.Student_ID, s.Name AS Student_Name,
                   c.Course_Name, ex.Exam_Type, res.Marks_Obtained, res.Grade_Code,
                   rea.Application_Date, rea.Reason, rea.Status, rea.Approval_Date
            FROM RE_EXAM_APPLICATION rea
            JOIN EXAM_RESULT res ON rea.Exam_Result_ID = res.Exam_Result_ID
            JOIN EXAM_REGISTRATION reg ON res.Exam_Registration_ID = reg.Exam_Registration_ID
            JOIN STUDENT s ON reg.Student_ID = s.Student_ID
            JOIN EXAM ex ON reg.Exam_ID = ex.Exam_ID
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE reg.Student_ID = ?
            ORDER BY rea.Application_Date DESC
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            ReExamApplication app = new ReExamApplication();
            app.setReExamApplicationId(rs.getLong("ReExam_Application_ID"));
            app.setExamResultId(rs.getLong("Exam_Result_ID"));
            app.setStudentId(rs.getLong("Student_ID"));
            app.setStudentName(rs.getString("Student_Name"));
            app.setCourseName(rs.getString("Course_Name"));
            app.setExamType(rs.getString("Exam_Type"));
            app.setMarksObtained(rs.getDouble("Marks_Obtained"));
            app.setGradeCode(rs.getString("Grade_Code"));
            if (rs.getDate("Application_Date") != null) {
                app.setApplicationDate(rs.getDate("Application_Date").toLocalDate());
            }
            app.setReason(rs.getString("Reason"));
            app.setStatus(rs.getString("Status"));
            if (rs.getDate("Approval_Date") != null) {
                app.setApprovalDate(rs.getDate("Approval_Date").toLocalDate());
            }
            return app;
        }, studentId);
    }

    public boolean hasReExamApplication(Long examResultId) {
        String sql = "SELECT COUNT(*) FROM RE_EXAM_APPLICATION WHERE Exam_Result_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, examResultId);
        return count != null && count > 0;
    }

    public Long applyReExam(Long examResultId, String reason) {
        String nextIdSql = "SELECT NVL(MAX(ReExam_Application_ID), 11000) + 1 FROM RE_EXAM_APPLICATION";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String insertSql = "INSERT INTO RE_EXAM_APPLICATION (ReExam_Application_ID, Exam_Result_ID, Application_Date, Reason, Status) VALUES (?, ?, SYSDATE, ?, 'PENDING')";
        jdbcTemplate.update(insertSql, nextId, examResultId, reason);
        return nextId;
    }

    public Optional<ExamResult> getExamResultById(Long examResultId) {
        String sql = """
            SELECT res.Exam_Result_ID, res.Exam_Registration_ID, reg.Student_ID, s.Name AS Student_Name,
                   ex.Exam_ID, c.Course_Name, ex.Exam_Type, ex.Exam_Date, ex.Max_Marks,
                   res.Marks_Obtained, res.Result_Status, res.Grade_Code, res.Published_Date,
                   (SELECT COUNT(*) FROM RE_EXAM_APPLICATION rea WHERE rea.Exam_Result_ID = res.Exam_Result_ID) AS Has_Reexam,
                   (SELECT rea.Status FROM RE_EXAM_APPLICATION rea WHERE rea.Exam_Result_ID = res.Exam_Result_ID AND ROWNUM = 1) AS Reexam_Status
            FROM EXAM_RESULT res
            JOIN EXAM_REGISTRATION reg ON res.Exam_Registration_ID = reg.Exam_Registration_ID
            JOIN STUDENT s ON reg.Student_ID = s.Student_ID
            JOIN EXAM ex ON reg.Exam_ID = ex.Exam_ID
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE res.Exam_Result_ID = ?
        """;
        List<ExamResult> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            ExamResult r = new ExamResult();
            r.setExamResultId(rs.getLong("Exam_Result_ID"));
            r.setExamRegistrationId(rs.getLong("Exam_Registration_ID"));
            r.setStudentId(rs.getLong("Student_ID"));
            r.setStudentName(rs.getString("Student_Name"));
            r.setExamId(rs.getLong("Exam_ID"));
            r.setCourseName(rs.getString("Course_Name"));
            r.setExamType(rs.getString("Exam_Type"));
            if (rs.getDate("Exam_Date") != null) {
                r.setExamDate(rs.getDate("Exam_Date").toLocalDate());
            }
            r.setMaxMarks(rs.getDouble("Max_Marks"));
            r.setMarksObtained(rs.getDouble("Marks_Obtained"));
            r.setResultStatus(rs.getString("Result_Status"));
            r.setGradeCode(rs.getString("Grade_Code"));
            if (rs.getDate("Published_Date") != null) {
                r.setPublishedDate(rs.getDate("Published_Date").toLocalDate());
            }
            r.setHasReExamApplied(rs.getInt("Has_Reexam") > 0);
            r.setReExamStatus(rs.getString("Reexam_Status"));
            return r;
        }, examResultId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // ---- Phase 3: enrollment lifecycle ----------------------------------------------

    /** Status of this student's row for the offering (ENROLLED / DROPPED / COMPLETED), or null. */
    public String getEnrollmentStatus(Long studentId, Long offeringId) {
        String sql = "SELECT Status FROM ENROLLMENT WHERE Student_ID = ? AND Offering_ID = ?";
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), studentId, offeringId);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * Appendix A Finding 4: another ENROLLED section of the same course in the same
     * semester and academic year. Returns that offering's "#id (Section X)" label, or null.
     */
    public String findOtherActiveSection(Long studentId, Long offeringId) {
        String sql = """
            SELECT '#' || other.Offering_ID || ' (Section ' || other.Section || ')'
            FROM COURSE_OFFERING target
            JOIN COURSE_OFFERING other ON other.Course_ID = target.Course_ID
                 AND other.Semester = target.Semester
                 AND other.Academic_Year = target.Academic_Year
                 AND other.Offering_ID <> target.Offering_ID
            JOIN ENROLLMENT en ON en.Offering_ID = other.Offering_ID
                 AND en.Student_ID = ? AND en.Status = 'ENROLLED'
            WHERE target.Offering_ID = ?
        """;
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), studentId, offeringId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void setEnrollmentStatus(Long studentId, Long offeringId, String status) {
        String sql = "UPDATE ENROLLMENT SET Status = ? WHERE Student_ID = ? AND Offering_ID = ?";
        jdbcTemplate.update(sql, status, studentId, offeringId);
    }

    /** Number of published results this student holds for exams of the offering. */
    public int countResultsInOffering(Long studentId, Long offeringId) {
        String sql = """
            SELECT COUNT(*) FROM EXAM_RESULT r
            JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
            JOIN EXAM x ON x.Exam_ID = er.Exam_ID
            WHERE er.Student_ID = ? AND x.Offering_ID = ?
        """;
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId, offeringId);
        return count != null ? count : 0;
    }

    /** Cancels the student's still-active registrations for exams of an offering being dropped. */
    public int cancelRegistrationsInOffering(Long studentId, Long offeringId) {
        String sql = """
            UPDATE EXAM_REGISTRATION er SET Status = 'CANCELLED'
            WHERE er.Student_ID = ? AND er.Status = 'REGISTERED'
              AND er.Exam_ID IN (SELECT Exam_ID FROM EXAM WHERE Offering_ID = ?)
              AND NOT EXISTS (SELECT 1 FROM EXAM_RESULT r WHERE r.Exam_Registration_ID = er.Exam_Registration_ID)
        """;
        return jdbcTemplate.update(sql, studentId, offeringId);
    }

    // ---- Phase 3: exam registration lifecycle --------------------------------------

    /** Schedule facts for one exam, or empty if the exam does not exist. */
    public Optional<Exam> findExamById(Long examId) {
        String sql = """
            SELECT ex.Exam_ID, ex.Offering_ID, c.Course_Name, ex.Exam_Type, ex.Exam_Date,
                   ex.Start_Time, ex.End_Time, ex.Venue, ex.Max_Marks, ex.Status
            FROM EXAM ex
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            WHERE ex.Exam_ID = ?
        """;
        List<Exam> list = jdbcTemplate.query(sql, (rs, rowNum) -> {
            Exam ex = new Exam();
            ex.setExamId(rs.getLong("Exam_ID"));
            ex.setOfferingId(rs.getLong("Offering_ID"));
            ex.setCourseName(rs.getString("Course_Name"));
            ex.setExamType(rs.getString("Exam_Type"));
            ex.setExamDate(rs.getDate("Exam_Date").toLocalDate());
            ex.setStartTime(rs.getTimestamp("Start_Time").toLocalDateTime());
            ex.setEndTime(rs.getTimestamp("End_Time").toLocalDateTime());
            ex.setVenue(rs.getString("Venue"));
            ex.setMaxMarks(rs.getDouble("Max_Marks"));
            ex.setStatus(rs.getString("Status"));
            return ex;
        }, examId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * Appendix A Finding 5: an active registration of this student whose exam is on the
     * same date and overlaps in time. Strict inequalities allow back-to-back exams
     * (one ending exactly when the next starts). Returns a description, or null.
     */
    public String findClashingExam(Long studentId, Long examId) {
        String sql = """
            SELECT '#' || other.Exam_ID || ' ' || c.Course_Name || ' ' || other.Exam_Type || ' ('
                   || TO_CHAR(other.Start_Time, 'DD-Mon-YYYY HH24:MI') || '-' || TO_CHAR(other.End_Time, 'HH24:MI') || ')'
            FROM EXAM target
            JOIN EXAM other ON other.Exam_Date = target.Exam_Date AND other.Exam_ID <> target.Exam_ID
                 AND target.Start_Time < other.End_Time AND other.Start_Time < target.End_Time
            JOIN EXAM_REGISTRATION er ON er.Exam_ID = other.Exam_ID AND er.Student_ID = ? AND er.Status = 'REGISTERED'
            JOIN COURSE_OFFERING co ON co.Offering_ID = other.Offering_ID
            JOIN COURSE c ON c.Course_ID = co.Course_ID
            WHERE target.Exam_ID = ?
        """;
        List<String> list = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString(1), studentId, examId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** {registrationId, status, resultCount} for (student, exam), or null when none exists. */
    public Object[] findRegistration(Long studentId, Long examId) {
        String sql = """
            SELECT er.Exam_Registration_ID, er.Status,
                   (SELECT COUNT(*) FROM EXAM_RESULT r WHERE r.Exam_Registration_ID = er.Exam_Registration_ID) AS Result_Count
            FROM EXAM_REGISTRATION er WHERE er.Student_ID = ? AND er.Exam_ID = ?
        """;
        List<Object[]> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Object[]{rs.getLong(1), rs.getString(2), rs.getInt(3)}, studentId, examId);
        return list.isEmpty() ? null : list.get(0);
    }

    public void setRegistrationStatus(Long registrationId, String status) {
        String sql = "UPDATE EXAM_REGISTRATION SET Status = ? WHERE Exam_Registration_ID = ?";
        jdbcTemplate.update(sql, status, registrationId);
    }

    // ---- Phase 3: re-exam withdrawal -------------------------------------------------

    /** {ownerStudentId, status} of a re-exam application, or null if it does not exist. */
    public Object[] findReExamOwnerAndStatus(Long applicationId) {
        String sql = """
            SELECT reg.Student_ID, rea.Status
            FROM RE_EXAM_APPLICATION rea
            JOIN EXAM_RESULT res ON rea.Exam_Result_ID = res.Exam_Result_ID
            JOIN EXAM_REGISTRATION reg ON res.Exam_Registration_ID = reg.Exam_Registration_ID
            WHERE rea.ReExam_Application_ID = ?
        """;
        List<Object[]> list = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Object[]{rs.getLong(1), rs.getString(2)}, applicationId);
        return list.isEmpty() ? null : list.get(0);
    }

    public int deleteReExamApplication(Long applicationId) {
        String sql = "DELETE FROM RE_EXAM_APPLICATION WHERE ReExam_Application_ID = ? AND Status = 'PENDING'";
        return jdbcTemplate.update(sql, applicationId);
    }

    private String determineGrade(Double totalPercentage) {
        if (totalPercentage == null) return "F";
        try {
            String sql = "SELECT Grade_Code FROM GRADE WHERE ? >= Min_Percentage AND ? <= Max_Percentage AND ROWNUM = 1";
            List<String> codes = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("Grade_Code"), totalPercentage, totalPercentage);
            if (!codes.isEmpty()) {
                return codes.get(0);
            }
        } catch (Exception ignored) {}
        if (totalPercentage >= 90) return "S";
        if (totalPercentage >= 80) return "A";
        if (totalPercentage >= 70) return "B";
        if (totalPercentage >= 60) return "C";
        if (totalPercentage >= 50) return "D";
        return "F";
    }

    private Double determineGradePoint(String gradeCode) {
        if (gradeCode == null) return 0.0;
        try {
            String sql = "SELECT Grade_Point FROM GRADE WHERE Grade_Code = ?";
            List<Double> pts = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getDouble("Grade_Point"), gradeCode);
            if (!pts.isEmpty()) return pts.get(0);
        } catch (Exception ignored) {}
        return switch (gradeCode) {
            case "S" -> 10.0;
            case "A" -> 9.0;
            case "B" -> 8.0;
            case "C" -> 7.0;
            case "D" -> 6.0;
            default -> 0.0;
        };
    }
}

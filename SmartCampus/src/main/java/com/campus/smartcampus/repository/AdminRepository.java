package com.campus.smartcampus.repository;

import com.campus.smartcampus.dto.*;
import com.campus.smartcampus.model.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class AdminRepository {

    private final JdbcTemplate jdbcTemplate;

    public AdminRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public java.util.Map<String, Object> getAdminStats() {
        int students = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM STUDENT", Integer.class);
        int faculty = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FACULTY", Integer.class);
        int courses = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM COURSE", Integer.class);
        int offerings = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM COURSE_OFFERING", Integer.class);
        int enrollments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM ENROLLMENT WHERE Status = 'ENROLLED'", Integer.class);
        int pendingReExams = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM RE_EXAM_APPLICATION WHERE Status = 'PENDING'", Integer.class);
        int departments = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM DEPARTMENT", Integer.class);
        int programs = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM PROGRAM", Integer.class);
        int exams = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EXAM", Integer.class);
        int results = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM EXAM_RESULT", Integer.class);
        return java.util.Map.of(
            "departments", departments,
            "programs", programs,
            "exams", exams,
            "results", results,
            "students", students,
            "faculty", faculty,
            "courses", courses,
            "offerings", offerings,
            "enrollments", enrollments,
            "pendingReExams", pendingReExams
        );
    }

    // Departments
    public List<Department> getAllDepartments() {
        String sql = "SELECT Department_ID, Department_Name, Office_Location FROM DEPARTMENT ORDER BY Department_ID";
        return jdbcTemplate.query(sql, (rs, rowNum) -> new Department(
            rs.getLong("Department_ID"),
            rs.getString("Department_Name"),
            rs.getString("Office_Location")
        ));
    }

    public Long createDepartment(DepartmentCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Department_ID), 0) + 1 FROM DEPARTMENT";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = "INSERT INTO DEPARTMENT (Department_ID, Department_Name, Office_Location) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, nextId, req.getDepartmentName(), req.getOfficeLocation());
        return nextId;
    }

    // Programs
    public List<Program> getAllPrograms() {
        String sql = """
            SELECT p.Program_ID, p.Program_Name, p.Degree, p.Duration_Years,
                   p.Department_ID, d.Department_Name
            FROM PROGRAM p
            JOIN DEPARTMENT d ON p.Department_ID = d.Department_ID
            ORDER BY p.Program_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Program p = new Program();
            p.setProgramId(rs.getLong("Program_ID"));
            p.setProgramName(rs.getString("Program_Name"));
            p.setDegree(rs.getString("Degree"));
            p.setDurationYears(rs.getInt("Duration_Years"));
            p.setDepartmentId(rs.getLong("Department_ID"));
            p.setDepartmentName(rs.getString("Department_Name"));
            return p;
        });
    }

    public Long createProgram(ProgramCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Program_ID), 0) + 1 FROM PROGRAM";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = "INSERT INTO PROGRAM (Program_ID, Program_Name, Degree, Duration_Years, Department_ID) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nextId, req.getProgramName(), req.getDegree(), req.getDurationYears(), req.getDepartmentId());
        return nextId;
    }

    // Students
    public List<Student> getAllStudents() {
        String sql = """
            SELECT s.Student_ID, s.Name, s.Email, s.Phone, s.DOB, s.Admission_Year,
                   s.Program_ID, p.Program_Name, p.Degree, d.Department_Name
            FROM STUDENT s
            JOIN PROGRAM p ON s.Program_ID = p.Program_ID
            JOIN DEPARTMENT d ON p.Department_ID = d.Department_ID
            ORDER BY s.Student_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
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
        });
    }

    public Long createStudent(StudentCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Student_ID), 1000) + 1 FROM STUDENT";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = "INSERT INTO STUDENT (Student_ID, Name, Email, Phone, DOB, Admission_Year, Program_ID) VALUES (?, ?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nextId, req.getName(), req.getEmail(), req.getPhone(),
                req.getDob() != null ? java.sql.Date.valueOf(req.getDob()) : null,
                req.getAdmissionYear(), req.getProgramId());
        return nextId;
    }

    // Faculty
    public List<Faculty> getAllFaculty() {
        String sql = """
            SELECT f.Faculty_ID, f.Name, f.Email, f.Phone, f.Designation,
                   f.Department_ID, d.Department_Name
            FROM FACULTY f
            JOIN DEPARTMENT d ON f.Department_ID = d.Department_ID
            ORDER BY f.Faculty_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Faculty f = new Faculty();
            f.setFacultyId(rs.getLong("Faculty_ID"));
            f.setName(rs.getString("Name"));
            f.setEmail(rs.getString("Email"));
            f.setPhone(rs.getString("Phone"));
            f.setDesignation(rs.getString("Designation"));
            f.setDepartmentId(rs.getLong("Department_ID"));
            f.setDepartmentName(rs.getString("Department_Name"));
            return f;
        });
    }

    public Long createFaculty(FacultyCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Faculty_ID), 200) + 1 FROM FACULTY";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = "INSERT INTO FACULTY (Faculty_ID, Name, Email, Phone, Designation, Department_ID) VALUES (?, ?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nextId, req.getName(), req.getEmail(), req.getPhone(), req.getDesignation(), req.getDepartmentId());
        return nextId;
    }

    // Courses
    public List<Course> getAllCourses() {
        String sql = """
            SELECT c.Course_ID, c.Course_Name, c.Credits, c.Course_Type,
                   c.Department_ID, d.Department_Name
            FROM COURSE c
            JOIN DEPARTMENT d ON c.Department_ID = d.Department_ID
            ORDER BY c.Course_ID
        """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Course c = new Course();
            c.setCourseId(rs.getLong("Course_ID"));
            c.setCourseName(rs.getString("Course_Name"));
            c.setCredits(rs.getInt("Credits"));
            c.setCourseType(rs.getString("Course_Type"));
            c.setDepartmentId(rs.getLong("Department_ID"));
            c.setDepartmentName(rs.getString("Department_Name"));
            return c;
        });
    }

    public Long createCourse(CourseCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Course_ID), 300) + 1 FROM COURSE";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = "INSERT INTO COURSE (Course_ID, Course_Name, Credits, Course_Type, Department_ID) VALUES (?, ?, ?, ?, ?)";
        jdbcTemplate.update(sql, nextId, req.getCourseName(), req.getCredits(), req.getCourseType(), req.getDepartmentId());
        return nextId;
    }

    // Course Offerings
    public List<CourseOffering> getAllOfferings() {
        String sql = """
            SELECT co.Offering_ID, co.Course_ID, c.Course_Name, c.Credits, c.Course_Type,
                   co.Faculty_ID, f.Name AS Faculty_Name, co.Semester, co.Academic_Year,
                   co.Section, co.Room_No, co.Max_Seats, co.Classes_Held,
                   (SELECT COUNT(*) FROM ENROLLMENT en WHERE en.Offering_ID = co.Offering_ID AND en.Status = 'ENROLLED') AS Enrolled_Count
            FROM COURSE_OFFERING co
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
            ORDER BY co.Offering_ID
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
        });
    }

    public boolean existsOffering(Long courseId, Integer semester, String academicYear, String section) {
        String sql = "SELECT COUNT(*) FROM COURSE_OFFERING WHERE Course_ID = ? AND Semester = ? AND Academic_Year = ? AND Section = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, courseId, semester, academicYear, section);
        return count != null && count > 0;
    }

    public Long createOffering(OfferingCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Offering_ID), 400) + 1 FROM COURSE_OFFERING";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = """
            INSERT INTO COURSE_OFFERING (Offering_ID, Course_ID, Faculty_ID, Semester, Academic_Year, Section, Room_No, Max_Seats, Classes_Held)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
        """;
        jdbcTemplate.update(sql, nextId, req.getCourseId(), req.getFacultyId(), req.getSemester(),
                req.getAcademicYear(), req.getSection(), req.getRoomNo(), req.getMaxSeats(),
                req.getClassesHeld() != null ? req.getClassesHeld() : 0);
        return nextId;
    }

    // Exams
    public List<Exam> getAllExams() {
        String sql = """
            SELECT ex.Exam_ID, ex.Offering_ID, c.Course_Name, co.Section, f.Name AS Faculty_Name,
                   ex.Exam_Type, ex.Exam_Date, ex.Start_Time, ex.End_Time, ex.Venue,
                   ex.Max_Marks, ex.Status
            FROM EXAM ex
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            JOIN FACULTY f ON co.Faculty_ID = f.Faculty_ID
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
        });
    }

    public Long createExam(ExamCreateRequest req) {
        String nextIdSql = "SELECT NVL(MAX(Exam_ID), 800) + 1 FROM EXAM";
        Long nextId = jdbcTemplate.queryForObject(nextIdSql, Long.class);
        String sql = """
            INSERT INTO EXAM (Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'SCHEDULED')
        """;
        jdbcTemplate.update(sql, nextId, req.getOfferingId(), req.getExamType(),
                java.sql.Date.valueOf(req.getExamDate()),
                Timestamp.valueOf(req.getStartTime()),
                Timestamp.valueOf(req.getEndTime()),
                req.getVenue(), req.getMaxMarks());
        return nextId;
    }

    // Re-exam applications
    public List<ReExamApplication> getAllReExamApplications() {
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
        });
    }

    public void updateReExamStatus(Long applicationId, String status) {
        String sql = "UPDATE RE_EXAM_APPLICATION SET Status = ?, Approval_Date = SYSDATE WHERE ReExam_Application_ID = ?";
        jdbcTemplate.update(sql, status, applicationId);
    }

    // Exam Results
    public List<ExamResult> getAllExamResults() {
        String sql = """
            SELECT res.Exam_Result_ID, res.Exam_Registration_ID, reg.Student_ID, s.Name AS Student_Name,
                   ex.Exam_ID, c.Course_Name, ex.Exam_Type, ex.Exam_Date, ex.Max_Marks,
                   res.Marks_Obtained, res.Result_Status, res.Grade_Code, res.Published_Date
            FROM EXAM_RESULT res
            JOIN EXAM_REGISTRATION reg ON res.Exam_Registration_ID = reg.Exam_Registration_ID
            JOIN STUDENT s ON reg.Student_ID = s.Student_ID
            JOIN EXAM ex ON reg.Exam_ID = ex.Exam_ID
            JOIN COURSE_OFFERING co ON ex.Offering_ID = co.Offering_ID
            JOIN COURSE c ON co.Course_ID = c.Course_ID
            ORDER BY res.Exam_Result_ID DESC
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
            return r;
        });
    }

    // ==================================================================================
    // Phase 3: Update / Delete via JDBC PreparedStatements (JdbcTemplate binds every ?)
    // ==================================================================================

    /** COUNT(*) helper used for existence and dependency pre-checks. */
    public int count(String sql, Object... args) {
        Integer n = jdbcTemplate.queryForObject(sql, Integer.class, args);
        return n != null ? n : 0;
    }

    public Long findEnrollmentId(Long studentId, Long offeringId) {
        return jdbcTemplate.queryForObject(
                "SELECT Enrollment_ID FROM ENROLLMENT WHERE Student_ID = ? AND Offering_ID = ?", Long.class, studentId, offeringId);
    }

    public int updateDepartment(Long id, DepartmentCreateRequest req) {
        return jdbcTemplate.update("UPDATE DEPARTMENT SET Department_Name = ?, Office_Location = ? WHERE Department_ID = ?",
                req.getDepartmentName().trim(), req.getOfficeLocation(), id);
    }

    public int deleteDepartment(Long id) {
        return jdbcTemplate.update("DELETE FROM DEPARTMENT WHERE Department_ID = ?", id);
    }

    public int updateProgram(Long id, ProgramCreateRequest req) {
        return jdbcTemplate.update("UPDATE PROGRAM SET Program_Name = ?, Degree = ?, Duration_Years = ?, Department_ID = ? WHERE Program_ID = ?",
                req.getProgramName().trim(), req.getDegree(), req.getDurationYears(), req.getDepartmentId(), id);
    }

    public int deleteProgram(Long id) {
        return jdbcTemplate.update("DELETE FROM PROGRAM WHERE Program_ID = ?", id);
    }

    public int updateStudent(Long id, StudentCreateRequest req) {
        return jdbcTemplate.update("UPDATE STUDENT SET Name = ?, Email = ?, Phone = ?, DOB = ?, Admission_Year = ?, Program_ID = ? WHERE Student_ID = ?",
                req.getName().trim(), req.getEmail().trim(), req.getPhone(),
                req.getDob() != null ? java.sql.Date.valueOf(req.getDob()) : null,
                req.getAdmissionYear(), req.getProgramId(), id);
    }

    /** Deletes a student with no academic history; the login row goes via FK_ACC_STUDENT ON DELETE CASCADE. */
    public int deleteStudent(Long id) {
        return jdbcTemplate.update("DELETE FROM STUDENT WHERE Student_ID = ?", id);
    }

    public int updateFaculty(Long id, FacultyCreateRequest req) {
        return jdbcTemplate.update("UPDATE FACULTY SET Name = ?, Email = ?, Phone = ?, Designation = ?, Department_ID = ? WHERE Faculty_ID = ?",
                req.getName().trim(), req.getEmail().trim(), req.getPhone(), req.getDesignation(), req.getDepartmentId(), id);
    }

    public int deleteFaculty(Long id) {
        return jdbcTemplate.update("DELETE FROM FACULTY WHERE Faculty_ID = ?", id);
    }

    public int updateCourse(Long id, CourseCreateRequest req) {
        return jdbcTemplate.update("UPDATE COURSE SET Course_Name = ?, Credits = ?, Course_Type = ?, Department_ID = ? WHERE Course_ID = ?",
                req.getCourseName().trim(), req.getCredits(), req.getCourseType(), req.getDepartmentId(), id);
    }

    public int deleteCourse(Long id) {
        return jdbcTemplate.update("DELETE FROM COURSE WHERE Course_ID = ?", id);
    }

    public boolean existsOtherOffering(Long offeringId, Long courseId, Integer semester, String academicYear, String section) {
        return count("SELECT COUNT(*) FROM COURSE_OFFERING WHERE Course_ID = ? AND Semester = ? AND Academic_Year = ? AND Section = ? AND Offering_ID <> ?",
                courseId, semester, academicYear, section, offeringId) > 0;
    }

    public int updateOffering(Long id, OfferingCreateRequest req) {
        return jdbcTemplate.update("""
                UPDATE COURSE_OFFERING SET Course_ID = ?, Faculty_ID = ?, Semester = ?, Academic_Year = ?, Section = ?,
                       Room_No = ?, Max_Seats = ?, Classes_Held = ?
                WHERE Offering_ID = ?
                """,
                req.getCourseId(), req.getFacultyId(), req.getSemester(), req.getAcademicYear(), req.getSection(),
                req.getRoomNo(), req.getMaxSeats(), req.getClassesHeld() != null ? req.getClassesHeld() : 0, id);
    }

    public int deleteOffering(Long id) {
        return jdbcTemplate.update("DELETE FROM COURSE_OFFERING WHERE Offering_ID = ?", id);
    }

    public Optional<Exam> findExam(Long examId) {
        List<Exam> list = jdbcTemplate.query("""
                SELECT Exam_ID, Offering_ID, Exam_Type, Exam_Date, Start_Time, End_Time, Venue, Max_Marks, Status
                FROM EXAM WHERE Exam_ID = ?
                """, (rs, rowNum) -> {
            Exam ex = new Exam();
            ex.setExamId(rs.getLong("Exam_ID"));
            ex.setOfferingId(rs.getLong("Offering_ID"));
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

    public int updateExam(Long id, ExamCreateRequest req) {
        return jdbcTemplate.update("""
                UPDATE EXAM SET Offering_ID = ?, Exam_Type = ?, Exam_Date = ?, Start_Time = ?, End_Time = ?, Venue = ?, Max_Marks = ?
                WHERE Exam_ID = ?
                """,
                req.getOfferingId(), req.getExamType(), java.sql.Date.valueOf(req.getExamDate()),
                Timestamp.valueOf(req.getStartTime()), Timestamp.valueOf(req.getEndTime()),
                req.getVenue(), req.getMaxMarks(), id);
    }

    public int updateExamStatus(Long id, String status) {
        return jdbcTemplate.update("UPDATE EXAM SET Status = ? WHERE Exam_ID = ?", status, id);
    }

    public int deleteExam(Long id) {
        return jdbcTemplate.update("DELETE FROM EXAM WHERE Exam_ID = ?", id);
    }

    /** Registrations for one exam with any published result, for the result-publication screen. */
    public List<java.util.Map<String, Object>> getExamRegistrations(Long examId) {
        return jdbcTemplate.query("""
                SELECT er.Exam_Registration_ID, er.Student_ID, s.Name AS Student_Name, er.Status,
                       r.Exam_Result_ID, r.Marks_Obtained, r.Grade_Code, r.Result_Status
                FROM EXAM_REGISTRATION er
                JOIN STUDENT s ON s.Student_ID = er.Student_ID
                LEFT JOIN EXAM_RESULT r ON r.Exam_Registration_ID = er.Exam_Registration_ID
                WHERE er.Exam_ID = ?
                ORDER BY er.Student_ID
                """, (rs, rowNum) -> {
            java.util.Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("examRegistrationId", rs.getLong("Exam_Registration_ID"));
            row.put("studentId", rs.getLong("Student_ID"));
            row.put("studentName", rs.getString("Student_Name"));
            row.put("status", rs.getString("Status"));
            long resultId = rs.getLong("Exam_Result_ID");
            row.put("examResultId", rs.wasNull() ? null : resultId);
            double marks = rs.getDouble("Marks_Obtained");
            row.put("marksObtained", rs.wasNull() ? null : marks);
            row.put("gradeCode", rs.getString("Grade_Code"));
            row.put("resultStatus", rs.getString("Result_Status"));
            return row;
        }, examId);
    }

    /** {examId, registrationStatus} for a registration, or null if it does not exist. */
    public Object[] findRegistrationExam(Long registrationId) {
        List<Object[]> list = jdbcTemplate.query(
                "SELECT Exam_ID, Status FROM EXAM_REGISTRATION WHERE Exam_Registration_ID = ?",
                (rs, rowNum) -> new Object[]{rs.getLong(1), rs.getString(2)}, registrationId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** {registrationId, examId} for a result, or null if it does not exist. */
    public Object[] findResult(Long resultId) {
        List<Object[]> list = jdbcTemplate.query("""
                SELECT r.Exam_Registration_ID, er.Exam_ID FROM EXAM_RESULT r
                JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
                WHERE r.Exam_Result_ID = ?
                """, (rs, rowNum) -> new Object[]{rs.getLong(1), rs.getLong(2)}, resultId);
        return list.isEmpty() ? null : list.get(0);
    }

    /** Grade band from the GRADE table for a percentage (two-decimal bands, e.g. 80.00-89.99). */
    public String gradeForPercentage(double percentage) {
        List<String> codes = jdbcTemplate.query(
                "SELECT Grade_Code FROM GRADE WHERE ? BETWEEN Min_Percentage AND Max_Percentage",
                (rs, rowNum) -> rs.getString(1), Math.round(percentage * 100.0) / 100.0);
        return codes.isEmpty() ? null : codes.get(0);
    }

    public List<java.util.Map<String, Object>> getGrades() {
        return jdbcTemplate.queryForList(
                "SELECT Grade_Code AS \"gradeCode\", Grade_Point AS \"gradePoint\", Min_Percentage AS \"minPercentage\", "
                + "Max_Percentage AS \"maxPercentage\" FROM GRADE ORDER BY Grade_Point DESC");
    }

    public Long insertResult(Long registrationId, double marks, String status, String grade) {
        Long nextId = jdbcTemplate.queryForObject("SELECT NVL(MAX(Exam_Result_ID), 10000) + 1 FROM EXAM_RESULT", Long.class);
        jdbcTemplate.update("""
                INSERT INTO EXAM_RESULT (Exam_Result_ID, Exam_Registration_ID, Marks_Obtained, Result_Status, Grade_Code, Published_Date)
                VALUES (?, ?, ?, ?, ?, SYSDATE)
                """, nextId, registrationId, marks, status, grade);
        return nextId;
    }

    public int updateResult(Long resultId, double marks, String status, String grade) {
        return jdbcTemplate.update("UPDATE EXAM_RESULT SET Marks_Obtained = ?, Result_Status = ?, Grade_Code = ?, Published_Date = SYSDATE WHERE Exam_Result_ID = ?",
                marks, status, grade, resultId);
    }

    public int deleteResult(Long resultId) {
        return jdbcTemplate.update("DELETE FROM EXAM_RESULT WHERE Exam_Result_ID = ?", resultId);
    }

    /** Current status of a re-exam application, or null if it does not exist. */
    public String findReExamStatus(Long applicationId) {
        List<String> list = jdbcTemplate.query("SELECT Status FROM RE_EXAM_APPLICATION WHERE ReExam_Application_ID = ?",
                (rs, rowNum) -> rs.getString(1), applicationId);
        return list.isEmpty() ? null : list.get(0);
    }

    // Foreign Key Existence Checks
    public boolean departmentExists(Long departmentId) {
        String sql = "SELECT COUNT(*) FROM DEPARTMENT WHERE Department_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, departmentId);
        return count != null && count > 0;
    }

    public boolean programExists(Long programId) {
        String sql = "SELECT COUNT(*) FROM PROGRAM WHERE Program_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, programId);
        return count != null && count > 0;
    }

    public boolean facultyExists(Long facultyId) {
        String sql = "SELECT COUNT(*) FROM FACULTY WHERE Faculty_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, facultyId);
        return count != null && count > 0;
    }

    public boolean courseExists(Long courseId) {
        String sql = "SELECT COUNT(*) FROM COURSE WHERE Course_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, courseId);
        return count != null && count > 0;
    }

    public boolean offeringExists(Long offeringId) {
        String sql = "SELECT COUNT(*) FROM COURSE_OFFERING WHERE Offering_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, offeringId);
        return count != null && count > 0;
    }

    public boolean studentExists(Long studentId) {
        String sql = "SELECT COUNT(*) FROM STUDENT WHERE Student_ID = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, studentId);
        return count != null && count > 0;
    }
}

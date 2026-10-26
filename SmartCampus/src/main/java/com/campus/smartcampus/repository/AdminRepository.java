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
        return java.util.Map.of(
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

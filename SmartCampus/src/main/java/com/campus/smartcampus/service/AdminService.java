package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.*;
import com.campus.smartcampus.exception.ConflictException;
import com.campus.smartcampus.exception.NotFoundException;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.repository.AdminRepository;
import com.campus.smartcampus.repository.UserAccountRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Administrator use cases. Deletes never cascade through academic history: each parent
 * delete first counts its child rows and answers 409 Conflict while any exist, 404 when
 * the id is unknown, and only then issues the DELETE.
 */
@Service
public class AdminService {

    private final AdminRepository adminRepository;
    private final UserAccountRepository userAccountRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminService(AdminRepository adminRepository, UserAccountRepository userAccountRepository) {
        this.adminRepository = adminRepository;
        this.userAccountRepository = userAccountRepository;
    }

    public java.util.Map<String, Object> getAdminStats() {
        return adminRepository.getAdminStats();
    }

    // ---------------------------------------------------------------- Departments

    public List<Department> getAllDepartments() {
        return adminRepository.getAllDepartments();
    }

    @Transactional
    public Long createDepartment(DepartmentCreateRequest req) {
        if (req.getDepartmentName() == null || req.getDepartmentName().isBlank()) {
            throw new IllegalArgumentException("Department name cannot be empty.");
        }
        return adminRepository.createDepartment(req);
    }

    @Transactional
    public void updateDepartment(Long id, DepartmentCreateRequest req) {
        requireExists(adminRepository.departmentExists(id), "Department", id);
        adminRepository.updateDepartment(id, req);
    }

    @Transactional
    public void deleteDepartment(Long id) {
        requireExists(adminRepository.departmentExists(id), "Department", id);
        blockIfReferenced("Department " + id,
                dependency("program(s)", "SELECT COUNT(*) FROM PROGRAM WHERE Department_ID = ?", id),
                dependency("faculty member(s)", "SELECT COUNT(*) FROM FACULTY WHERE Department_ID = ?", id),
                dependency("course(s)", "SELECT COUNT(*) FROM COURSE WHERE Department_ID = ?", id));
        adminRepository.deleteDepartment(id);
    }

    // ---------------------------------------------------------------- Programs

    public List<Program> getAllPrograms() {
        return adminRepository.getAllPrograms();
    }

    @Transactional
    public Long createProgram(ProgramCreateRequest req) {
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        return adminRepository.createProgram(req);
    }

    @Transactional
    public void updateProgram(Long id, ProgramCreateRequest req) {
        requireExists(adminRepository.programExists(id), "Program", id);
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        adminRepository.updateProgram(id, req);
    }

    @Transactional
    public void deleteProgram(Long id) {
        requireExists(adminRepository.programExists(id), "Program", id);
        blockIfReferenced("Program " + id,
                dependency("student(s)", "SELECT COUNT(*) FROM STUDENT WHERE Program_ID = ?", id));
        adminRepository.deleteProgram(id);
    }

    // ---------------------------------------------------------------- Students

    public List<Student> getAllStudents() {
        return adminRepository.getAllStudents();
    }

    @Transactional
    public Long createStudent(StudentCreateRequest req) {
        requireParent(adminRepository.programExists(req.getProgramId()), "Program", req.getProgramId());
        return adminRepository.createStudent(req);
    }

    @Transactional
    public void updateStudent(Long id, StudentCreateRequest req) {
        requireExists(adminRepository.studentExists(id), "Student", id);
        requireParent(adminRepository.programExists(req.getProgramId()), "Program", req.getProgramId());
        adminRepository.updateStudent(id, req);
    }

    @Transactional
    public void deleteStudent(Long id) {
        requireExists(adminRepository.studentExists(id), "Student", id);
        blockIfReferenced("Student " + id,
                dependency("enrollment(s)", "SELECT COUNT(*) FROM ENROLLMENT WHERE Student_ID = ?", id),
                dependency("attendance record(s)", "SELECT COUNT(*) FROM ATTENDANCE WHERE Student_ID = ?", id),
                dependency("marks record(s)", "SELECT COUNT(*) FROM MARKS WHERE Student_ID = ?", id),
                dependency("exam registration(s)", "SELECT COUNT(*) FROM EXAM_REGISTRATION WHERE Student_ID = ?", id));
        adminRepository.deleteStudent(id);
    }

    // ---------------------------------------------------------------- Faculty

    public List<Faculty> getAllFaculty() {
        return adminRepository.getAllFaculty();
    }

    @Transactional
    public Long createFaculty(FacultyCreateRequest req) {
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        return adminRepository.createFaculty(req);
    }

    @Transactional
    public void updateFaculty(Long id, FacultyCreateRequest req) {
        requireExists(adminRepository.facultyExists(id), "Faculty", id);
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        adminRepository.updateFaculty(id, req);
    }

    @Transactional
    public void deleteFaculty(Long id) {
        requireExists(adminRepository.facultyExists(id), "Faculty", id);
        blockIfReferenced("Faculty " + id,
                dependency("course offering(s)", "SELECT COUNT(*) FROM COURSE_OFFERING WHERE Faculty_ID = ?", id));
        adminRepository.deleteFaculty(id);
    }

    // ---------------------------------------------------------------- Courses

    public List<Course> getAllCourses() {
        return adminRepository.getAllCourses();
    }

    @Transactional
    public Long createCourse(CourseCreateRequest req) {
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        return adminRepository.createCourse(req);
    }

    @Transactional
    public void updateCourse(Long id, CourseCreateRequest req) {
        requireExists(adminRepository.courseExists(id), "Course", id);
        requireParent(adminRepository.departmentExists(req.getDepartmentId()), "Department", req.getDepartmentId());
        adminRepository.updateCourse(id, req);
    }

    @Transactional
    public void deleteCourse(Long id) {
        requireExists(adminRepository.courseExists(id), "Course", id);
        blockIfReferenced("Course " + id,
                dependency("course offering(s)", "SELECT COUNT(*) FROM COURSE_OFFERING WHERE Course_ID = ?", id));
        adminRepository.deleteCourse(id);
    }

    // ---------------------------------------------------------------- Course offerings

    public List<CourseOffering> getAllOfferings() {
        return adminRepository.getAllOfferings();
    }

    @Transactional
    public Long createOffering(OfferingCreateRequest req) {
        validateOfferingParents(req);
        // Check UQ_OFFERING: Course_ID, Semester, Academic_Year, Section
        if (adminRepository.existsOffering(req.getCourseId(), req.getSemester(), req.getAcademicYear(), req.getSection())) {
            throw new ConflictException("A course offering already exists for Course " + req.getCourseId()
                    + ", Semester " + req.getSemester() + ", Year " + req.getAcademicYear()
                    + ", Section " + req.getSection());
        }
        return adminRepository.createOffering(req);
    }

    @Transactional
    public void updateOffering(Long id, OfferingCreateRequest req) {
        requireExists(adminRepository.offeringExists(id), "Course offering", id);
        validateOfferingParents(req);
        if (adminRepository.existsOtherOffering(id, req.getCourseId(), req.getSemester(), req.getAcademicYear(), req.getSection())) {
            throw new ConflictException("Another offering already uses Course " + req.getCourseId() + ", Semester "
                    + req.getSemester() + ", Year " + req.getAcademicYear() + ", Section " + req.getSection() + ".");
        }
        int enrolled = adminRepository.count("SELECT COUNT(*) FROM ENROLLMENT WHERE Offering_ID = ? AND Status = 'ENROLLED'", id);
        if (req.getMaxSeats() < enrolled) {
            throw new ConflictException("Max seats (" + req.getMaxSeats() + ") cannot be below the " + enrolled + " students already enrolled.");
        }
        int held = req.getClassesHeld() != null ? req.getClassesHeld() : 0;
        int maxAttended = adminRepository.count("SELECT NVL(MAX(Classes_Attended), 0) FROM ATTENDANCE WHERE Offering_ID = ?", id);
        if (held < maxAttended) {
            throw new IllegalArgumentException("Classes held (" + held + ") cannot be below " + maxAttended + " classes already attended by a student.");
        }
        adminRepository.updateOffering(id, req);
    }

    @Transactional
    public void deleteOffering(Long id) {
        requireExists(adminRepository.offeringExists(id), "Course offering", id);
        blockIfReferenced("Course offering " + id,
                dependency("enrollment(s)", "SELECT COUNT(*) FROM ENROLLMENT WHERE Offering_ID = ?", id),
                dependency("attendance record(s)", "SELECT COUNT(*) FROM ATTENDANCE WHERE Offering_ID = ?", id),
                dependency("marks record(s)", "SELECT COUNT(*) FROM MARKS WHERE Offering_ID = ?", id),
                dependency("exam(s)", "SELECT COUNT(*) FROM EXAM WHERE Offering_ID = ?", id));
        adminRepository.deleteOffering(id);
    }

    private void validateOfferingParents(OfferingCreateRequest req) {
        requireParent(adminRepository.courseExists(req.getCourseId()), "Course", req.getCourseId());
        requireParent(adminRepository.facultyExists(req.getFacultyId()), "Faculty", req.getFacultyId());
        // Appendix A Finding 7: 1..10 (also @Min/@Max on the DTO and CHECK in Oracle on NUMBER(2))
        if (req.getSemester() == null || req.getSemester() < 1 || req.getSemester() > 10) {
            throw new IllegalArgumentException("Semester must be between 1 and 10.");
        }
    }

    // ---------------------------------------------------------------- Exams

    public List<Exam> getAllExams() {
        return adminRepository.getAllExams();
    }

    @Transactional
    public Long createExam(ExamCreateRequest req) {
        requireParent(adminRepository.offeringExists(req.getOfferingId()), "Course offering", req.getOfferingId());
        validateExamTimes(req);
        return adminRepository.createExam(req);
    }

    @Transactional
    public void updateExam(Long id, ExamCreateRequest req) {
        adminRepository.findExam(id).orElseThrow(() -> new NotFoundException("Exam " + id + " does not exist."));
        requireParent(adminRepository.offeringExists(req.getOfferingId()), "Course offering", req.getOfferingId());
        validateExamTimes(req);
        if (resultCountForExam(id) > 0) {
            throw new ConflictException("Exam " + id + " already has published results; its schedule is locked.");
        }
        adminRepository.updateExam(id, req);
    }

    /**
     * Appendix A Finding 6: an exam can only become COMPLETED once its end time has passed,
     * and an exam with published results cannot be re-opened or cancelled.
     */
    @Transactional
    public void updateExamStatus(Long id, String status) {
        Exam exam = adminRepository.findExam(id).orElseThrow(() -> new NotFoundException("Exam " + id + " does not exist."));
        String target = status.toUpperCase();
        if ("COMPLETED".equals(target) && exam.getEndTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Exam " + id + " ends on " + exam.getEndTime().toString().replace('T', ' ')
                    + "; it cannot be marked COMPLETED before it has been held.");
        }
        if (!"COMPLETED".equals(target) && resultCountForExam(id) > 0) {
            throw new ConflictException("Exam " + id + " has published results, so it must stay COMPLETED.");
        }
        adminRepository.updateExamStatus(id, target);
    }

    @Transactional
    public void deleteExam(Long id) {
        adminRepository.findExam(id).orElseThrow(() -> new NotFoundException("Exam " + id + " does not exist."));
        blockIfReferenced("Exam " + id,
                dependency("exam registration(s)", "SELECT COUNT(*) FROM EXAM_REGISTRATION WHERE Exam_ID = ?", id));
        adminRepository.deleteExam(id);
    }

    public List<Map<String, Object>> getExamRegistrations(Long examId) {
        adminRepository.findExam(examId).orElseThrow(() -> new NotFoundException("Exam " + examId + " does not exist."));
        return adminRepository.getExamRegistrations(examId);
    }

    private void validateExamTimes(ExamCreateRequest req) {
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("Exam end time must be after start time.");
        }
        if (!req.getStartTime().toLocalDate().equals(req.getExamDate()) || !req.getEndTime().toLocalDate().equals(req.getExamDate())) {
            throw new IllegalArgumentException("Exam start and end time must fall on the exam date.");
        }
    }

    private int resultCountForExam(Long examId) {
        return adminRepository.count("""
                SELECT COUNT(*) FROM EXAM_RESULT r JOIN EXAM_REGISTRATION er ON er.Exam_Registration_ID = r.Exam_Registration_ID
                WHERE er.Exam_ID = ?
                """, examId);
    }

    // ---------------------------------------------------------------- Exam results

    public List<ExamResult> getAllExamResults() {
        return adminRepository.getAllExamResults();
    }

    public List<Map<String, Object>> getGrades() {
        return adminRepository.getGrades();
    }

    /** Publishes a result; grade and PASS/FAIL are derived from GRADE using marks / Max_Marks. */
    @Transactional
    public Long publishResult(ResultPublishRequest req) {
        if (req.getExamRegistrationId() == null) {
            throw new IllegalArgumentException("Exam registration ID is required.");
        }
        Object[] reg = adminRepository.findRegistrationExam(req.getExamRegistrationId());
        if (reg == null) {
            throw new NotFoundException("Exam registration " + req.getExamRegistrationId() + " does not exist.");
        }
        if (!"REGISTERED".equals(reg[1])) {
            throw new ConflictException("Registration " + req.getExamRegistrationId() + " is " + reg[1] + "; results can only be published for active registrations.");
        }
        Exam exam = examReadyForResults((Long) reg[0]);
        if (adminRepository.count("SELECT COUNT(*) FROM EXAM_RESULT WHERE Exam_Registration_ID = ?", req.getExamRegistrationId()) > 0) {
            throw new ConflictException("A result is already published for registration " + req.getExamRegistrationId() + "; edit it instead.");
        }
        String[] graded = grade(req, exam);
        return adminRepository.insertResult(req.getExamRegistrationId(), req.getMarksObtained(), graded[0], graded[1]);
    }

    @Transactional
    public void updateResult(Long resultId, ResultPublishRequest req) {
        Object[] result = adminRepository.findResult(resultId);
        if (result == null) {
            throw new NotFoundException("Exam result " + resultId + " does not exist.");
        }
        Exam exam = examReadyForResults((Long) result[1]);
        String[] graded = grade(req, exam);
        adminRepository.updateResult(resultId, req.getMarksObtained(), graded[0], graded[1]);
    }

    @Transactional
    public void deleteResult(Long resultId) {
        if (adminRepository.findResult(resultId) == null) {
            throw new NotFoundException("Exam result " + resultId + " does not exist.");
        }
        blockIfReferenced("Exam result " + resultId,
                dependency("re-exam application(s)", "SELECT COUNT(*) FROM RE_EXAM_APPLICATION WHERE Exam_Result_ID = ?", resultId));
        adminRepository.deleteResult(resultId);
    }

    /** Appendix A Finding 6: results only for exams that have ended and are marked COMPLETED. */
    private Exam examReadyForResults(Long examId) {
        Exam exam = adminRepository.findExam(examId).orElseThrow(() -> new NotFoundException("Exam " + examId + " does not exist."));
        if (exam.getEndTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Exam " + examId + " has not been held yet (" + exam.getExamDate()
                    + "); results cannot be published for a future exam.");
        }
        if (!"COMPLETED".equals(exam.getStatus())) {
            throw new ConflictException("Exam " + examId + " is " + exam.getStatus() + "; mark it COMPLETED before publishing results.");
        }
        return exam;
    }

    /** Returns {resultStatus, gradeCode}. */
    private String[] grade(ResultPublishRequest req, Exam exam) {
        double marks = req.getMarksObtained();
        if (marks < 0 || marks > exam.getMaxMarks()) {
            throw new IllegalArgumentException("Marks obtained must be between 0 and the exam maximum of " + exam.getMaxMarks() + ".");
        }
        if (Boolean.TRUE.equals(req.getWithheld())) {
            return new String[]{"WITHHELD", null};
        }
        String gradeCode = adminRepository.gradeForPercentage(marks / exam.getMaxMarks() * 100.0);
        if (gradeCode == null) {
            throw new IllegalStateException("No GRADE band covers this percentage; check the GRADE table.");
        }
        return new String[]{"F".equals(gradeCode) ? "FAIL" : "PASS", gradeCode};
    }

    // ---------------------------------------------------------------- Re-exam applications

    public List<ReExamApplication> getAllReExamApplications() {
        return adminRepository.getAllReExamApplications();
    }

    @Transactional
    public void processReExamDecision(ReExamDecisionRequest req) {
        if (!"APPROVED".equalsIgnoreCase(req.getDecision()) && !"REJECTED".equalsIgnoreCase(req.getDecision())) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED.");
        }
        String current = adminRepository.findReExamStatus(req.getApplicationId());
        if (current == null) {
            throw new NotFoundException("Re-exam application " + req.getApplicationId() + " does not exist.");
        }
        if (!"PENDING".equals(current)) {
            throw new ConflictException("Application " + req.getApplicationId() + " was already " + current + ".");
        }
        adminRepository.updateReExamStatus(req.getApplicationId(), req.getDecision().toUpperCase());
    }

    // ---------------------------------------------------------------- User accounts

    /** Account list without password hashes. */
    public List<Map<String, Object>> getAccounts() {
        return userAccountRepository.listAllAccounts().stream().map(a -> {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("accountId", a.getAccountId());
            row.put("username", a.getUsername());
            row.put("role", a.getRole());
            row.put("studentId", a.getStudentId());
            row.put("facultyId", a.getFacultyId());
            row.put("active", a.getIsActive() != null && a.getIsActive() == 1);
            row.put("createdAt", a.getCreatedAt());
            row.put("lastLogin", a.getLastLogin());
            return row;
        }).toList();
    }

    @Transactional
    public void updateAccount(Long accountId, AccountUpdateRequest req) {
        UserAccount account = userAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account " + accountId + " does not exist."));
        if (req.getActive() == null && (req.getNewPassword() == null || req.getNewPassword().isBlank())) {
            throw new IllegalArgumentException("Nothing to update: send 'active' and/or 'newPassword'.");
        }
        if (Boolean.FALSE.equals(req.getActive())) {
            guardLastAdmin(account, "deactivate");
        }
        if (req.getActive() != null) {
            userAccountRepository.setActive(accountId, req.getActive());
        }
        if (req.getNewPassword() != null && !req.getNewPassword().isBlank()) {
            userAccountRepository.updatePasswordHash(accountId, passwordEncoder.encode(req.getNewPassword()));
        }
    }

    @Transactional
    public void deleteAccount(Long accountId) {
        UserAccount account = userAccountRepository.findById(accountId)
                .orElseThrow(() -> new NotFoundException("Account " + accountId + " does not exist."));
        guardLastAdmin(account, "delete");
        userAccountRepository.deleteAccount(accountId);
    }

    private void guardLastAdmin(UserAccount account, String action) {
        if ("ADMINISTRATOR".equals(account.getRole()) && account.getIsActive() == 1
                && userAccountRepository.countActiveAdmins() <= 1) {
            throw new ConflictException("Cannot " + action + " the last active administrator account.");
        }
    }

    // ---------------------------------------------------------------- helpers

    private record Dependency(String label, int count) {}

    private Dependency dependency(String label, String sql, Long id) {
        return new Dependency(label, adminRepository.count(sql, id));
    }

    private static void blockIfReferenced(String what, Dependency... deps) {
        StringBuilder sb = new StringBuilder();
        for (Dependency d : deps) {
            if (d.count() > 0) {
                sb.append(sb.length() == 0 ? "" : ", ").append(d.count()).append(' ').append(d.label());
            }
        }
        if (sb.length() > 0) {
            throw new ConflictException(what + " cannot be deleted: it is still referenced by " + sb
                    + ". Academic history is never cascade-deleted.");
        }
    }

    private static void requireExists(boolean exists, String entity, Long id) {
        if (!exists) {
            throw new NotFoundException(entity + " " + id + " does not exist.");
        }
    }

    /** A missing parent named in the request body is a client input error (400), not a missing resource. */
    private static void requireParent(boolean exists, String entity, Long id) {
        if (!exists) {
            throw new IllegalArgumentException(entity + " ID " + id + " does not exist.");
        }
    }
}

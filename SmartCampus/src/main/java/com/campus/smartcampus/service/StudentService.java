package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.CourseRegistrationRequest;
import com.campus.smartcampus.dto.ExamRegistrationRequest;
import com.campus.smartcampus.dto.ReExamApplicationRequest;
import com.campus.smartcampus.exception.ConflictException;
import com.campus.smartcampus.exception.ForbiddenException;
import com.campus.smartcampus.exception.NotFoundException;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.repository.AdminRepository;
import com.campus.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final AdminRepository adminRepository;

    public StudentService(StudentRepository studentRepository, AdminRepository adminRepository) {
        this.studentRepository = studentRepository;
        this.adminRepository = adminRepository;
    }

    public Student getProfile(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found with ID: " + studentId));
    }

    public List<Enrollment> getEnrollments(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getEnrollments(studentId);
    }

    public List<CourseOffering> getAvailableOfferings(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getAvailableOfferings(studentId);
    }

    @Transactional
    public Long registerCourse(CourseRegistrationRequest req) {
        Long studentId = req.getStudentId();
        Long offeringId = req.getOfferingId();
        validateStudentExists(studentId);

        if (!adminRepository.offeringExists(offeringId)) {
            throw new NotFoundException("Course offering ID " + offeringId + " does not exist.");
        }

        // 1. Same offering twice (also guarded by UQ_ENROLLMENT in Oracle)
        String existing = studentRepository.getEnrollmentStatus(studentId, offeringId);
        if ("ENROLLED".equals(existing) || "COMPLETED".equals(existing)) {
            throw new ConflictException("Student is already enrolled in this course offering.");
        }

        // 2. Appendix A Finding 4: another section of the same course in the same term
        String otherSection = studentRepository.findOtherActiveSection(studentId, offeringId);
        if (otherSection != null) {
            throw new ConflictException("Already enrolled in another section of this course for the same semester: offering "
                    + otherSection + ". Drop that section first.");
        }

        // 3. Seat capacity
        Integer maxSeats = studentRepository.getMaxSeats(offeringId);
        if (maxSeats == null || maxSeats <= 0) {
            throw new ConflictException("Invalid seat capacity for course offering.");
        }
        int enrolledCount = studentRepository.getEnrolledCount(offeringId);
        if (enrolledCount >= maxSeats) {
            throw new ConflictException("Course offering is at full capacity (" + enrolledCount + "/" + maxSeats + " seats filled).");
        }

        // 4. Re-activate a previously dropped row (UQ_ENROLLMENT allows one row per pair) or insert
        Long enrollmentId;
        if ("DROPPED".equals(existing)) {
            studentRepository.setEnrollmentStatus(studentId, offeringId, "ENROLLED");
            enrollmentId = adminRepository.findEnrollmentId(studentId, offeringId);
        } else {
            enrollmentId = studentRepository.enrollStudent(studentId, offeringId);
        }
        studentRepository.initAttendanceRecord(studentId, offeringId);
        studentRepository.initMarksRecord(studentId, offeringId);
        return enrollmentId;
    }

    /**
     * Drops an active enrollment (logical: Status = DROPPED, the row and its attendance /
     * marks history are kept). Blocked once results have been published for the course.
     */
    @Transactional
    public void dropCourse(Long studentId, Long offeringId) {
        validateStudentExists(studentId);
        String status = studentRepository.getEnrollmentStatus(studentId, offeringId);
        if (status == null || "DROPPED".equals(status)) {
            throw new NotFoundException("No active enrollment in offering " + offeringId + " for this student.");
        }
        if ("COMPLETED".equals(status)) {
            throw new ConflictException("This course is already completed and cannot be dropped.");
        }
        if (studentRepository.countResultsInOffering(studentId, offeringId) > 0) {
            throw new ConflictException("Exam results have already been published for this course, so it can no longer be dropped.");
        }
        studentRepository.cancelRegistrationsInOffering(studentId, offeringId);
        studentRepository.setEnrollmentStatus(studentId, offeringId, "DROPPED");
    }

    public List<Attendance> getAttendance(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getAttendance(studentId);
    }

    public List<Marks> getMarks(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getMarks(studentId);
    }

    public List<Exam> getAvailableExams(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getAvailableExams(studentId);
    }

    @Transactional
    public Long registerExam(ExamRegistrationRequest req) {
        Long studentId = req.getStudentId();
        Long examId = req.getExamId();
        validateStudentExists(studentId);

        Exam exam = studentRepository.findExamById(examId)
                .orElseThrow(() -> new NotFoundException("Exam ID " + examId + " does not exist."));

        Object[] registration = studentRepository.findRegistration(studentId, examId);
        if (registration != null && "REGISTERED".equals(registration[1])) {
            throw new ConflictException("Student is already registered for this exam.");
        }

        if (!"ENROLLED".equals(studentRepository.getEnrollmentStatus(studentId, exam.getOfferingId()))) {
            throw new ConflictException("Student is not enrolled in the course offering for this exam.");
        }

        if (!"SCHEDULED".equals(exam.getStatus()) || !exam.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("Registration is closed: this exam is " + exam.getStatus().toLowerCase()
                    + " or has already started.");
        }

        // Appendix A Finding 5: overlapping exam on the same date (back-to-back is allowed)
        String clash = studentRepository.findClashingExam(studentId, examId);
        if (clash != null) {
            throw new ConflictException("Timetable clash: this exam overlaps your registered exam " + clash + ".");
        }

        if (registration != null) {
            Long registrationId = (Long) registration[0];
            studentRepository.setRegistrationStatus(registrationId, "REGISTERED");
            return registrationId;
        }
        return studentRepository.registerExam(studentId, examId);
    }

    /** Cancels an exam registration (logical: Status = CANCELLED) before the exam starts. */
    @Transactional
    public void cancelExamRegistration(Long studentId, Long examId) {
        validateStudentExists(studentId);
        Exam exam = studentRepository.findExamById(examId)
                .orElseThrow(() -> new NotFoundException("Exam ID " + examId + " does not exist."));
        Object[] registration = studentRepository.findRegistration(studentId, examId);
        if (registration == null || !"REGISTERED".equals(registration[1])) {
            throw new NotFoundException("No active registration for exam " + examId + ".");
        }
        if ((Integer) registration[2] > 0 || !exam.getStartTime().isAfter(LocalDateTime.now())) {
            throw new ConflictException("This exam has already started or has a published result; the registration cannot be cancelled.");
        }
        studentRepository.setRegistrationStatus((Long) registration[0], "CANCELLED");
    }

    public List<ExamResult> getExamResults(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getExamResults(studentId);
    }

    public List<ReExamApplication> getReExamApplications(Long studentId) {
        validateStudentExists(studentId);
        return studentRepository.getReExamApplications(studentId);
    }

    @Transactional
    public Long submitReExamApplication(Long studentId, ReExamApplicationRequest req) {
        validateStudentExists(studentId);

        ExamResult result = studentRepository.getExamResultById(req.getExamResultId())
                .orElseThrow(() -> new NotFoundException("Exam result ID " + req.getExamResultId() + " not found."));

        if (!result.getStudentId().equals(studentId)) {
            throw new ForbiddenException("Exam result does not belong to the logged-in student.");
        }

        if ("WITHHELD".equals(result.getResultStatus())) {
            throw new ConflictException("This result is withheld and cannot be appealed yet.");
        }

        if (studentRepository.hasReExamApplication(req.getExamResultId())) {
            throw new ConflictException("A re-examination application has already been submitted for this result.");
        }

        if (req.getReason() == null || req.getReason().trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a valid reason (at least 5 characters) for the re-examination application.");
        }

        return studentRepository.applyReExam(req.getExamResultId(), req.getReason().trim());
    }

    /** Withdraws a PENDING re-exam application (decided applications are kept as history). */
    @Transactional
    public void withdrawReExamApplication(Long studentId, Long applicationId) {
        validateStudentExists(studentId);
        Object[] app = studentRepository.findReExamOwnerAndStatus(applicationId);
        if (app == null) {
            throw new NotFoundException("Re-exam application " + applicationId + " not found.");
        }
        if (!studentId.equals(app[0])) {
            throw new ForbiddenException("This re-exam application belongs to another student.");
        }
        if (!"PENDING".equals(app[1])) {
            throw new ConflictException("Only PENDING applications can be withdrawn; this one is " + app[1] + ".");
        }
        studentRepository.deleteReExamApplication(applicationId);
    }

    private void validateStudentExists(Long studentId) {
        if (!adminRepository.studentExists(studentId)) {
            throw new NotFoundException("Student does not exist with ID: " + studentId);
        }
    }
}

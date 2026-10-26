package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.CourseRegistrationRequest;
import com.campus.smartcampus.dto.ExamRegistrationRequest;
import com.campus.smartcampus.dto.ReExamApplicationRequest;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.repository.AdminRepository;
import com.campus.smartcampus.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new IllegalArgumentException("Student not found with ID: " + studentId));
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
        validateStudentExists(req.getStudentId());

        if (!adminRepository.offeringExists(req.getOfferingId())) {
            throw new IllegalArgumentException("Course offering ID " + req.getOfferingId() + " does not exist.");
        }

        // 1. Prevent duplicate enrollment
        if (studentRepository.isEnrolled(req.getStudentId(), req.getOfferingId())) {
            throw new IllegalStateException("Student is already enrolled in this course offering.");
        }

        // 2. Enforce course seat capacity
        Integer maxSeats = studentRepository.getMaxSeats(req.getOfferingId());
        if (maxSeats == null || maxSeats <= 0) {
            throw new IllegalStateException("Invalid seat capacity for course offering.");
        }
        int enrolledCount = studentRepository.getEnrolledCount(req.getOfferingId());
        if (enrolledCount >= maxSeats) {
            throw new IllegalStateException("Course offering is at full capacity (" + enrolledCount + "/" + maxSeats + " seats filled).");
        }

        // 3. Perform enrollment and default attendance / mark records
        Long enrollmentId = studentRepository.enrollStudent(req.getStudentId(), req.getOfferingId());
        studentRepository.initAttendanceRecord(req.getStudentId(), req.getOfferingId());
        studentRepository.initMarksRecord(req.getStudentId(), req.getOfferingId());

        return enrollmentId;
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
        validateStudentExists(req.getStudentId());

        // Check duplicate registration
        if (studentRepository.isExamRegistered(req.getStudentId(), req.getExamId())) {
            throw new IllegalStateException("Student is already registered for this exam.");
        }

        // Ensure student is enrolled in the course offering that conducts this exam
        boolean isEligible = false;
        List<Exam> exams = studentRepository.getAvailableExams(req.getStudentId());
        for (Exam e : exams) {
            if (e.getExamId().equals(req.getExamId())) {
                isEligible = true;
                break;
            }
        }

        if (!isEligible) {
            throw new IllegalStateException("Student is not enrolled in the course offering for this exam.");
        }

        return studentRepository.registerExam(req.getStudentId(), req.getExamId());
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
                .orElseThrow(() -> new IllegalArgumentException("Exam result ID " + req.getExamResultId() + " not found."));

        if (!result.getStudentId().equals(studentId)) {
            throw new IllegalStateException("Exam result does not belong to the logged-in student.");
        }

        if (studentRepository.hasReExamApplication(req.getExamResultId())) {
            throw new IllegalStateException("A re-examination application has already been submitted for this result.");
        }

        if (req.getReason() == null || req.getReason().trim().length() < 5) {
            throw new IllegalArgumentException("Please provide a valid reason for the re-examination application.");
        }

        return studentRepository.applyReExam(req.getExamResultId(), req.getReason().trim());
    }

    private void validateStudentExists(Long studentId) {
        if (!adminRepository.studentExists(studentId)) {
            throw new IllegalArgumentException("Student does not exist with ID: " + studentId);
        }
    }
}

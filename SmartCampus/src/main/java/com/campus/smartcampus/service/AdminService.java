package com.campus.smartcampus.service;

import com.campus.smartcampus.dto.*;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.repository.AdminRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public java.util.Map<String, Object> getAdminStats() {
        return adminRepository.getAdminStats();
    }

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

    public List<Program> getAllPrograms() {
        return adminRepository.getAllPrograms();
    }

    @Transactional
    public Long createProgram(ProgramCreateRequest req) {
        if (!adminRepository.departmentExists(req.getDepartmentId())) {
            throw new IllegalArgumentException("Department ID " + req.getDepartmentId() + " does not exist.");
        }
        return adminRepository.createProgram(req);
    }

    public List<Student> getAllStudents() {
        return adminRepository.getAllStudents();
    }

    @Transactional
    public Long createStudent(StudentCreateRequest req) {
        if (!adminRepository.programExists(req.getProgramId())) {
            throw new IllegalArgumentException("Program ID " + req.getProgramId() + " does not exist.");
        }
        return adminRepository.createStudent(req);
    }

    public List<Faculty> getAllFaculty() {
        return adminRepository.getAllFaculty();
    }

    @Transactional
    public Long createFaculty(FacultyCreateRequest req) {
        if (!adminRepository.departmentExists(req.getDepartmentId())) {
            throw new IllegalArgumentException("Department ID " + req.getDepartmentId() + " does not exist.");
        }
        return adminRepository.createFaculty(req);
    }

    public List<Course> getAllCourses() {
        return adminRepository.getAllCourses();
    }

    @Transactional
    public Long createCourse(CourseCreateRequest req) {
        if (!adminRepository.departmentExists(req.getDepartmentId())) {
            throw new IllegalArgumentException("Department ID " + req.getDepartmentId() + " does not exist.");
        }
        return adminRepository.createCourse(req);
    }

    public List<CourseOffering> getAllOfferings() {
        return adminRepository.getAllOfferings();
    }

    @Transactional
    public Long createOffering(OfferingCreateRequest req) {
        if (!adminRepository.courseExists(req.getCourseId())) {
            throw new IllegalArgumentException("Course ID " + req.getCourseId() + " does not exist.");
        }
        if (!adminRepository.facultyExists(req.getFacultyId())) {
            throw new IllegalArgumentException("Faculty ID " + req.getFacultyId() + " does not exist.");
        }

        // Check UQ_OFFERING: Course_ID, Semester, Academic_Year, Section
        if (adminRepository.existsOffering(req.getCourseId(), req.getSemester(), req.getAcademicYear(), req.getSection())) {
            throw new IllegalStateException("A course offering already exists for Course " + req.getCourseId()
                    + ", Semester " + req.getSemester() + ", Year " + req.getAcademicYear()
                    + ", Section " + req.getSection());
        }

        return adminRepository.createOffering(req);
    }

    public List<Exam> getAllExams() {
        return adminRepository.getAllExams();
    }

    @Transactional
    public Long createExam(ExamCreateRequest req) {
        if (!adminRepository.offeringExists(req.getOfferingId())) {
            throw new IllegalArgumentException("Course Offering ID " + req.getOfferingId() + " does not exist.");
        }
        if (!req.getEndTime().isAfter(req.getStartTime())) {
            throw new IllegalArgumentException("Exam end time must be after start time.");
        }

        return adminRepository.createExam(req);
    }

    public List<ReExamApplication> getAllReExamApplications() {
        return adminRepository.getAllReExamApplications();
    }

    @Transactional
    public void processReExamDecision(ReExamDecisionRequest req) {
        if (!"APPROVED".equalsIgnoreCase(req.getDecision()) && !"REJECTED".equalsIgnoreCase(req.getDecision())) {
            throw new IllegalArgumentException("Decision must be APPROVED or REJECTED.");
        }
        adminRepository.updateReExamStatus(req.getApplicationId(), req.getDecision().toUpperCase());
    }

    public List<ExamResult> getAllExamResults() {
        return adminRepository.getAllExamResults();
    }
}

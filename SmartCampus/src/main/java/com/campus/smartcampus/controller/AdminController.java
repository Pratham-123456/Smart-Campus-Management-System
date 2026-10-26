package com.campus.smartcampus.controller;

import com.campus.smartcampus.dto.*;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAdminStats()));
    }

    // Departments
    @GetMapping("/departments")
    public ResponseEntity<ApiResponse<List<Department>>> getDepartments() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllDepartments()));
    }

    @PostMapping("/departments")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createDepartment(@Valid @RequestBody DepartmentCreateRequest req) {
        Long id = adminService.createDepartment(req);
        return ResponseEntity.ok(ApiResponse.ok("Department created successfully.", Map.of("departmentId", id)));
    }

    // Programs
    @GetMapping("/programs")
    public ResponseEntity<ApiResponse<List<Program>>> getPrograms() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllPrograms()));
    }

    @PostMapping("/programs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProgram(@Valid @RequestBody ProgramCreateRequest req) {
        Long id = adminService.createProgram(req);
        return ResponseEntity.ok(ApiResponse.ok("Program created successfully.", Map.of("programId", id)));
    }

    // Students
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> getStudents() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStudents()));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStudent(@Valid @RequestBody StudentCreateRequest req) {
        Long id = adminService.createStudent(req);
        return ResponseEntity.ok(ApiResponse.ok("Student created successfully.", Map.of("studentId", id)));
    }

    // Faculty
    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<Faculty>>> getFaculty() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllFaculty()));
    }

    @PostMapping("/faculty")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFaculty(@Valid @RequestBody FacultyCreateRequest req) {
        Long id = adminService.createFaculty(req);
        return ResponseEntity.ok(ApiResponse.ok("Faculty created successfully.", Map.of("facultyId", id)));
    }

    // Courses
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<Course>>> getCourses() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllCourses()));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCourse(@Valid @RequestBody CourseCreateRequest req) {
        Long id = adminService.createCourse(req);
        return ResponseEntity.ok(ApiResponse.ok("Course created successfully.", Map.of("courseId", id)));
    }

    // Course Offerings
    @GetMapping("/offerings")
    public ResponseEntity<ApiResponse<List<CourseOffering>>> getOfferings() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllOfferings()));
    }

    @PostMapping("/offerings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOffering(@Valid @RequestBody OfferingCreateRequest req) {
        Long id = adminService.createOffering(req);
        return ResponseEntity.ok(ApiResponse.ok("Course offering created successfully.", Map.of("offeringId", id)));
    }

    // Exams
    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getExams() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllExams()));
    }

    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExam(@Valid @RequestBody ExamCreateRequest req) {
        Long id = adminService.createExam(req);
        return ResponseEntity.ok(ApiResponse.ok("Exam schedule created successfully.", Map.of("examId", id)));
    }

    // Exam Results
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<ExamResult>>> getResults() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllExamResults()));
    }

    // Re-Exam Applications
    @GetMapping("/re-exams")
    public ResponseEntity<ApiResponse<List<ReExamApplication>>> getReExams() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllReExamApplications()));
    }

    @PostMapping("/re-exams/decision")
    public ResponseEntity<ApiResponse<Void>> processReExamDecision(@Valid @RequestBody ReExamDecisionRequest req) {
        adminService.processReExamDecision(req);
        return ResponseEntity.ok(ApiResponse.ok("Application " + req.getDecision().toLowerCase() + " successfully.", null));
    }
}

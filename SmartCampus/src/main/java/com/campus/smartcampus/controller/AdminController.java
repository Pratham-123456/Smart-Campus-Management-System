package com.campus.smartcampus.controller;

import com.campus.smartcampus.dto.*;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Administrator REST API (all paths require the ADMINISTRATOR role, enforced by AuthInterceptor).
 * POST creates (201), PUT/PATCH updates (200), DELETE removes (200) or answers 404 / 409.
 */
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
        return created("Department created successfully.", Map.of("departmentId", id));
    }

    @PutMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> updateDepartment(@PathVariable Long id, @Valid @RequestBody DepartmentCreateRequest req) {
        adminService.updateDepartment(id, req);
        return done("Department updated successfully.");
    }

    @DeleteMapping("/departments/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        adminService.deleteDepartment(id);
        return done("Department deleted.");
    }

    // Programs
    @GetMapping("/programs")
    public ResponseEntity<ApiResponse<List<Program>>> getPrograms() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllPrograms()));
    }

    @PostMapping("/programs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createProgram(@Valid @RequestBody ProgramCreateRequest req) {
        Long id = adminService.createProgram(req);
        return created("Program created successfully.", Map.of("programId", id));
    }

    @PutMapping("/programs/{id}")
    public ResponseEntity<ApiResponse<Void>> updateProgram(@PathVariable Long id, @Valid @RequestBody ProgramCreateRequest req) {
        adminService.updateProgram(id, req);
        return done("Program updated successfully.");
    }

    @DeleteMapping("/programs/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteProgram(@PathVariable Long id) {
        adminService.deleteProgram(id);
        return done("Program deleted.");
    }

    // Students
    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<Student>>> getStudents() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllStudents()));
    }

    @PostMapping("/students")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createStudent(@Valid @RequestBody StudentCreateRequest req) {
        Long id = adminService.createStudent(req);
        return created("Student created successfully.", Map.of("studentId", id));
    }

    @PutMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> updateStudent(@PathVariable Long id, @Valid @RequestBody StudentCreateRequest req) {
        adminService.updateStudent(id, req);
        return done("Student updated successfully.");
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteStudent(@PathVariable Long id) {
        adminService.deleteStudent(id);
        return done("Student deleted.");
    }

    // Faculty
    @GetMapping("/faculty")
    public ResponseEntity<ApiResponse<List<Faculty>>> getFaculty() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllFaculty()));
    }

    @PostMapping("/faculty")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createFaculty(@Valid @RequestBody FacultyCreateRequest req) {
        Long id = adminService.createFaculty(req);
        return created("Faculty created successfully.", Map.of("facultyId", id));
    }

    @PutMapping("/faculty/{id}")
    public ResponseEntity<ApiResponse<Void>> updateFaculty(@PathVariable Long id, @Valid @RequestBody FacultyCreateRequest req) {
        adminService.updateFaculty(id, req);
        return done("Faculty updated successfully.");
    }

    @DeleteMapping("/faculty/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteFaculty(@PathVariable Long id) {
        adminService.deleteFaculty(id);
        return done("Faculty member deleted.");
    }

    // Courses
    @GetMapping("/courses")
    public ResponseEntity<ApiResponse<List<Course>>> getCourses() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllCourses()));
    }

    @PostMapping("/courses")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCourse(@Valid @RequestBody CourseCreateRequest req) {
        Long id = adminService.createCourse(req);
        return created("Course created successfully.", Map.of("courseId", id));
    }

    @PutMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseCreateRequest req) {
        adminService.updateCourse(id, req);
        return done("Course updated successfully.");
    }

    @DeleteMapping("/courses/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCourse(@PathVariable Long id) {
        adminService.deleteCourse(id);
        return done("Course deleted.");
    }

    // Course Offerings
    @GetMapping("/offerings")
    public ResponseEntity<ApiResponse<List<CourseOffering>>> getOfferings() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllOfferings()));
    }

    @PostMapping("/offerings")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createOffering(@Valid @RequestBody OfferingCreateRequest req) {
        Long id = adminService.createOffering(req);
        return created("Course offering created successfully.", Map.of("offeringId", id));
    }

    @PutMapping("/offerings/{id}")
    public ResponseEntity<ApiResponse<Void>> updateOffering(@PathVariable Long id, @Valid @RequestBody OfferingCreateRequest req) {
        adminService.updateOffering(id, req);
        return done("Course offering updated successfully.");
    }

    @DeleteMapping("/offerings/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteOffering(@PathVariable Long id) {
        adminService.deleteOffering(id);
        return done("Course offering deleted.");
    }

    // Exams
    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getExams() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllExams()));
    }

    @PostMapping("/exams")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createExam(@Valid @RequestBody ExamCreateRequest req) {
        Long id = adminService.createExam(req);
        return created("Exam schedule created successfully.", Map.of("examId", id));
    }

    @PutMapping("/exams/{id}")
    public ResponseEntity<ApiResponse<Void>> updateExam(@PathVariable Long id, @Valid @RequestBody ExamCreateRequest req) {
        adminService.updateExam(id, req);
        return done("Exam schedule updated successfully.");
    }

    @PatchMapping("/exams/{id}/status")
    public ResponseEntity<ApiResponse<Void>> updateExamStatus(@PathVariable Long id, @Valid @RequestBody ExamStatusRequest req) {
        adminService.updateExamStatus(id, req.getStatus());
        return done("Exam marked " + req.getStatus().toUpperCase() + ".");
    }

    @DeleteMapping("/exams/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteExam(@PathVariable Long id) {
        adminService.deleteExam(id);
        return done("Exam deleted.");
    }

    @GetMapping("/exams/{id}/registrations")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getExamRegistrations(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getExamRegistrations(id)));
    }

    // Exam Results
    @GetMapping("/results")
    public ResponseEntity<ApiResponse<List<ExamResult>>> getResults() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllExamResults()));
    }

    @PostMapping("/results")
    public ResponseEntity<ApiResponse<Map<String, Object>>> publishResult(@Valid @RequestBody ResultPublishRequest req) {
        Long id = adminService.publishResult(req);
        return created("Result published; grade derived from the GRADE scale.", Map.of("examResultId", id));
    }

    @PutMapping("/results/{id}")
    public ResponseEntity<ApiResponse<Void>> updateResult(@PathVariable Long id, @Valid @RequestBody ResultPublishRequest req) {
        adminService.updateResult(id, req);
        return done("Result updated and re-graded.");
    }

    @DeleteMapping("/results/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteResult(@PathVariable Long id) {
        adminService.deleteResult(id);
        return done("Result deleted.");
    }

    @GetMapping("/grades")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getGrades() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getGrades()));
    }

    // Re-Exam Applications
    @GetMapping("/re-exams")
    public ResponseEntity<ApiResponse<List<ReExamApplication>>> getReExams() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAllReExamApplications()));
    }

    @PostMapping("/re-exams/decision")
    public ResponseEntity<ApiResponse<Void>> processReExamDecision(@Valid @RequestBody ReExamDecisionRequest req) {
        adminService.processReExamDecision(req);
        return done("Application " + req.getDecision().toLowerCase() + " successfully.");
    }

    // User accounts (creation stays at POST /api/auth/provision)
    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAccounts() {
        return ResponseEntity.ok(ApiResponse.ok(adminService.getAccounts()));
    }

    @PatchMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> updateAccount(@PathVariable Long id, @Valid @RequestBody AccountUpdateRequest req) {
        adminService.updateAccount(id, req);
        return done("Account updated.");
    }

    @DeleteMapping("/accounts/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(@PathVariable Long id) {
        adminService.deleteAccount(id);
        return done("Account deleted.");
    }

    private static ResponseEntity<ApiResponse<Map<String, Object>>> created(String message, Map<String, Object> body) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(message, body));
    }

    private static ResponseEntity<ApiResponse<Void>> done(String message) {
        return ResponseEntity.ok(ApiResponse.ok(message, null));
    }
}

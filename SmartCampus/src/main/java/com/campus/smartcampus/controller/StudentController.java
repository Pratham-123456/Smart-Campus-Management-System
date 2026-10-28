package com.campus.smartcampus.controller;

import com.campus.smartcampus.dto.CourseRegistrationRequest;
import com.campus.smartcampus.dto.ExamRegistrationRequest;
import com.campus.smartcampus.dto.ReExamApplicationRequest;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping("/{studentId}/profile")
    public ResponseEntity<ApiResponse<Student>> getProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getProfile(studentId)));
    }

    @GetMapping("/{studentId}/enrollments")
    public ResponseEntity<ApiResponse<List<Enrollment>>> getEnrollments(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getEnrollments(studentId)));
    }

    @GetMapping("/{studentId}/offerings")
    public ResponseEntity<ApiResponse<List<CourseOffering>>> getOfferings(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAvailableOfferings(studentId)));
    }

    @PostMapping("/{studentId}/enroll")
    public ResponseEntity<ApiResponse<Map<String, Object>>> enroll(
            @PathVariable Long studentId,
            @Valid @RequestBody CourseRegistrationRequest req) {
        req.setStudentId(studentId);
        Long enrollmentId = studentService.registerCourse(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Enrolled successfully in course offering.", Map.of("enrollmentId", enrollmentId)));
    }

    @DeleteMapping("/{studentId}/enrollments/{offeringId}")
    public ResponseEntity<ApiResponse<Void>> dropCourse(@PathVariable Long studentId, @PathVariable Long offeringId) {
        studentService.dropCourse(studentId, offeringId);
        return ResponseEntity.ok(ApiResponse.ok("Course dropped. Attendance and marks history is retained.", null));
    }

    @GetMapping("/{studentId}/attendance")
    public ResponseEntity<ApiResponse<List<Attendance>>> getAttendance(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAttendance(studentId)));
    }

    @GetMapping("/{studentId}/marks")
    public ResponseEntity<ApiResponse<List<Marks>>> getMarks(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getMarks(studentId)));
    }

    @GetMapping("/{studentId}/exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getExams(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getAvailableExams(studentId)));
    }

    @PostMapping("/{studentId}/exams/register")
    public ResponseEntity<ApiResponse<Map<String, Object>>> registerExam(
            @PathVariable Long studentId,
            @Valid @RequestBody ExamRegistrationRequest req) {
        req.setStudentId(studentId);
        Long regId = studentService.registerExam(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Registered for exam successfully.", Map.of("examRegistrationId", regId)));
    }

    @DeleteMapping("/{studentId}/exams/{examId}/registration")
    public ResponseEntity<ApiResponse<Void>> cancelExamRegistration(@PathVariable Long studentId, @PathVariable Long examId) {
        studentService.cancelExamRegistration(studentId, examId);
        return ResponseEntity.ok(ApiResponse.ok("Exam registration cancelled.", null));
    }

    @GetMapping("/{studentId}/results")
    public ResponseEntity<ApiResponse<List<ExamResult>>> getResults(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getExamResults(studentId)));
    }

    @GetMapping("/{studentId}/re-exams")
    public ResponseEntity<ApiResponse<List<ReExamApplication>>> getReExams(@PathVariable Long studentId) {
        return ResponseEntity.ok(ApiResponse.ok(studentService.getReExamApplications(studentId)));
    }

    @PostMapping("/{studentId}/re-exams/apply")
    public ResponseEntity<ApiResponse<Map<String, Object>>> applyReExam(
            @PathVariable Long studentId,
            @Valid @RequestBody ReExamApplicationRequest req) {
        Long appId = studentService.submitReExamApplication(studentId, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Re-examination application submitted successfully.", Map.of("reExamApplicationId", appId)));
    }

    @DeleteMapping("/{studentId}/re-exams/{applicationId}")
    public ResponseEntity<ApiResponse<Void>> withdrawReExam(@PathVariable Long studentId, @PathVariable Long applicationId) {
        studentService.withdrawReExamApplication(studentId, applicationId);
        return ResponseEntity.ok(ApiResponse.ok("Pending re-examination application withdrawn.", null));
    }
}

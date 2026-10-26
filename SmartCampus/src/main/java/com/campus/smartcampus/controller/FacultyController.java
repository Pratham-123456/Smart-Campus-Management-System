package com.campus.smartcampus.controller;

import com.campus.smartcampus.dto.AttendanceUpdateRequest;
import com.campus.smartcampus.dto.MarksUpdateRequest;
import com.campus.smartcampus.dto.StudentAttendanceMarksDTO;
import com.campus.smartcampus.model.*;
import com.campus.smartcampus.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping("/{facultyId}/profile")
    public ResponseEntity<ApiResponse<Faculty>> getProfile(@PathVariable Long facultyId) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getProfile(facultyId)));
    }

    @GetMapping("/{facultyId}/offerings")
    public ResponseEntity<ApiResponse<List<CourseOffering>>> getOfferings(@PathVariable Long facultyId) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getOfferings(facultyId)));
    }

    @GetMapping("/{facultyId}/offerings/{offeringId}/students")
    public ResponseEntity<ApiResponse<List<StudentAttendanceMarksDTO>>> getEnrolledStudents(
            @PathVariable Long facultyId,
            @PathVariable Long offeringId) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getEnrolledStudents(facultyId, offeringId)));
    }

    @PostMapping("/{facultyId}/offerings/{offeringId}/classes-held")
    public ResponseEntity<ApiResponse<Void>> updateClassesHeld(
            @PathVariable Long facultyId,
            @PathVariable Long offeringId,
            @RequestBody Map<String, Integer> payload) {
        Integer classesHeld = payload.get("classesHeld");
        facultyService.updateClassesHeld(facultyId, offeringId, classesHeld);
        return ResponseEntity.ok(ApiResponse.ok("Classes held updated successfully.", null));
    }

    @PostMapping("/{facultyId}/attendance")
    public ResponseEntity<ApiResponse<Void>> updateAttendance(
            @PathVariable Long facultyId,
            @Valid @RequestBody AttendanceUpdateRequest req) {
        facultyService.updateAttendance(facultyId, req);
        return ResponseEntity.ok(ApiResponse.ok("Attendance updated successfully.", null));
    }

    @PostMapping("/{facultyId}/marks")
    public ResponseEntity<ApiResponse<Void>> updateMarks(
            @PathVariable Long facultyId,
            @Valid @RequestBody MarksUpdateRequest req) {
        facultyService.updateMarks(facultyId, req);
        return ResponseEntity.ok(ApiResponse.ok("Marks updated successfully.", null));
    }

    @GetMapping("/{facultyId}/exams")
    public ResponseEntity<ApiResponse<List<Exam>>> getExams(@PathVariable Long facultyId) {
        return ResponseEntity.ok(ApiResponse.ok(facultyService.getExams(facultyId)));
    }
}

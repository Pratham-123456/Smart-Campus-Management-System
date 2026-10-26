package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotNull;

public class CourseRegistrationRequest {
    private Long studentId;

    @NotNull(message = "Course Offering ID is required")
    private Long offeringId;

    public CourseRegistrationRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }
}

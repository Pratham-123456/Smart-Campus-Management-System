package com.campus.smartcampus.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AttendanceUpdateRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Offering ID is required")
    private Long offeringId;

    @NotNull(message = "Classes attended is required")
    @Min(value = 0, message = "Classes attended cannot be negative")
    private Integer classesAttended;

    public AttendanceUpdateRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }

    public Integer getClassesAttended() { return classesAttended; }
    public void setClassesAttended(Integer classesAttended) { this.classesAttended = classesAttended; }
}

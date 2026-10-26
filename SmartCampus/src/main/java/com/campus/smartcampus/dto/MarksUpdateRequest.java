package com.campus.smartcampus.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class MarksUpdateRequest {
    @NotNull(message = "Student ID is required")
    private Long studentId;

    @NotNull(message = "Offering ID is required")
    private Long offeringId;

    @NotNull(message = "Continuous Assessment marks required")
    @DecimalMin(value = "0.0", message = "CA marks cannot be negative")
    @DecimalMax(value = "30.0", message = "CA marks cannot exceed 30")
    private Double continuousAssessmentMarks;

    @NotNull(message = "Midterm marks required")
    @DecimalMin(value = "0.0", message = "Midterm marks cannot be negative")
    @DecimalMax(value = "30.0", message = "Midterm marks cannot exceed 30")
    private Double midtermMarks;

    @NotNull(message = "Final Exam marks required")
    @DecimalMin(value = "0.0", message = "Final Exam marks cannot be negative")
    @DecimalMax(value = "50.0", message = "Final Exam marks cannot exceed 50")
    private Double finalExamMarks;

    public MarksUpdateRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }

    public Double getContinuousAssessmentMarks() { return continuousAssessmentMarks; }
    public void setContinuousAssessmentMarks(Double continuousAssessmentMarks) { this.continuousAssessmentMarks = continuousAssessmentMarks; }

    public Double getMidtermMarks() { return midtermMarks; }
    public void setMidtermMarks(Double midtermMarks) { this.midtermMarks = midtermMarks; }

    public Double getFinalExamMarks() { return finalExamMarks; }
    public void setFinalExamMarks(Double finalExamMarks) { this.finalExamMarks = finalExamMarks; }
}

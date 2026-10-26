package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotNull;

public class ExamRegistrationRequest {
    private Long studentId;

    @NotNull(message = "Exam ID is required")
    private Long examId;

    public ExamRegistrationRequest() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }
}

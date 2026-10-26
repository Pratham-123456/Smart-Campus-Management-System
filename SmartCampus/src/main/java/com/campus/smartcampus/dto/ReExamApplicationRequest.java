package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ReExamApplicationRequest {
    @NotNull(message = "Exam Result ID is required")
    private Long examResultId;

    @NotBlank(message = "Reason for re-examination request is required")
    @Size(max = 500, message = "Reason cannot exceed 500 characters")
    private String reason;

    public ReExamApplicationRequest() {}

    public Long getExamResultId() { return examResultId; }
    public void setExamResultId(Long examResultId) { this.examResultId = examResultId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

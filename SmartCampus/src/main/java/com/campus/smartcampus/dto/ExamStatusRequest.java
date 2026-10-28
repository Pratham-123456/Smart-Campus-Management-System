package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class ExamStatusRequest {
    @NotBlank(message = "Status is required")
    @Pattern(regexp = "SCHEDULED|COMPLETED|CANCELLED", message = "Status must be SCHEDULED, COMPLETED or CANCELLED")
    private String status;

    public ExamStatusRequest() {}

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

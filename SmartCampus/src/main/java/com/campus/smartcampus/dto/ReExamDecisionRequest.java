package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class ReExamDecisionRequest {
    @NotNull(message = "Re-exam Application ID is required")
    private Long applicationId;

    @NotBlank(message = "Decision is required")
    @Pattern(regexp = "APPROVED|REJECTED", message = "Decision must be APPROVED or REJECTED")
    private String decision;

    public ReExamDecisionRequest() {}

    public Long getApplicationId() { return applicationId; }
    public void setApplicationId(Long applicationId) { this.applicationId = applicationId; }

    public String getDecision() { return decision; }
    public void setDecision(String decision) { this.decision = decision; }
}

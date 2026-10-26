package com.campus.smartcampus.dto;

import jakarta.validation.constraints.*;

public class ProgramCreateRequest {
    @NotBlank(message = "Program name is required")
    private String programName;

    @NotBlank(message = "Degree is required (e.g. B.Tech, M.Tech)")
    private String degree;

    @NotNull(message = "Duration in years is required")
    @Min(value = 1, message = "Duration must be at least 1 year")
    @Max(value = 6, message = "Duration cannot exceed 6 years")
    private Integer durationYears;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public ProgramCreateRequest() {}

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = degree; }

    public Integer getDurationYears() { return durationYears; }
    public void setDurationYears(Integer durationYears) { this.durationYears = durationYears; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}

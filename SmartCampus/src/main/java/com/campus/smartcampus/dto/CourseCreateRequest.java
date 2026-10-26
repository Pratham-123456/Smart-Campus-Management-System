package com.campus.smartcampus.dto;

import jakarta.validation.constraints.*;

public class CourseCreateRequest {
    @NotBlank(message = "Course name is required")
    private String courseName;

    @NotNull(message = "Credits is required")
    @Min(value = 1, message = "Credits must be greater than 0")
    @Max(value = 10, message = "Credits cannot exceed 10")
    private Integer credits;

    @NotBlank(message = "Course type is required (e.g. Core, Elective)")
    private String courseType;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    public CourseCreateRequest() {}

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public String getCourseType() { return courseType; }
    public void setCourseType(String courseType) { this.courseType = courseType; }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }
}

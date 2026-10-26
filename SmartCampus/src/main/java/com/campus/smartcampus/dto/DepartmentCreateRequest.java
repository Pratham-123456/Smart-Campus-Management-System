package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;

public class DepartmentCreateRequest {
    @NotBlank(message = "Department name is required")
    private String departmentName;

    private String officeLocation;

    public DepartmentCreateRequest() {}

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
}

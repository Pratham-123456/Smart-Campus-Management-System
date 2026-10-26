package com.campus.smartcampus.model;

public class Department {
    private Long departmentId;
    private String departmentName;
    private String officeLocation;

    public Department() {}

    public Department(Long departmentId, String departmentName, String officeLocation) {
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.officeLocation = officeLocation;
    }

    public Long getDepartmentId() { return departmentId; }
    public void setDepartmentId(Long departmentId) { this.departmentId = departmentId; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getOfficeLocation() { return officeLocation; }
    public void setOfficeLocation(String officeLocation) { this.officeLocation = officeLocation; }
}

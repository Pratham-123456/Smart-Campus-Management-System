package com.campus.smartcampus.dto;

import jakarta.validation.constraints.*;

public class OfferingCreateRequest {
    @NotNull(message = "Course ID is required")
    private Long courseId;

    @NotNull(message = "Faculty ID is required")
    private Long facultyId;

    @NotNull(message = "Semester is required")
    @Min(value = 1, message = "Semester must be between 1 and 10")
    @Max(value = 10, message = "Semester must be between 1 and 10")
    private Integer semester;

    @NotBlank(message = "Academic year is required (e.g. 2026-2027)")
    @Pattern(regexp = "^\\d{4}-\\d{4}$", message = "Academic year must be formatted as YYYY-YYYY (e.g. 2026-2027)")
    private String academicYear;

    @NotBlank(message = "Section is required (e.g. A, B)")
    private String section;

    private String roomNo;

    @NotNull(message = "Maximum seats is required")
    @Min(value = 1, message = "Max seats must be greater than 0")
    private Integer maxSeats;

    @Min(value = 0, message = "Classes held cannot be negative")
    private Integer classesHeld = 0;

    public OfferingCreateRequest() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getFacultyId() { return facultyId; }
    public void setFacultyId(Long facultyId) { this.facultyId = facultyId; }

    public Integer getSemester() { return semester; }
    public void setSemester(Integer semester) { this.semester = semester; }

    public String getAcademicYear() { return academicYear; }
    public void setAcademicYear(String academicYear) { this.academicYear = academicYear; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getRoomNo() { return roomNo; }
    public void setRoomNo(String roomNo) { this.roomNo = roomNo; }

    public Integer getMaxSeats() { return maxSeats; }
    public void setMaxSeats(Integer maxSeats) { this.maxSeats = maxSeats; }

    public Integer getClassesHeld() { return classesHeld; }
    public void setClassesHeld(Integer classesHeld) { this.classesHeld = classesHeld; }
}

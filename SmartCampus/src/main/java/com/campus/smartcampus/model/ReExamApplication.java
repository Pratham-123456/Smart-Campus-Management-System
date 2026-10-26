package com.campus.smartcampus.model;

import java.time.LocalDate;

public class ReExamApplication {
    private Long reExamApplicationId;
    private Long examResultId;
    private Long studentId;
    private String studentName;
    private String courseName;
    private String examType;
    private Double marksObtained;
    private String gradeCode;
    private LocalDate applicationDate;
    private String reason;
    private String status;
    private LocalDate approvalDate;

    public ReExamApplication() {}

    public Long getReExamApplicationId() { return reExamApplicationId; }
    public void setReExamApplicationId(Long reExamApplicationId) { this.reExamApplicationId = reExamApplicationId; }

    public Long getExamResultId() { return examResultId; }
    public void setExamResultId(Long examResultId) { this.examResultId = examResultId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }

    public LocalDate getApplicationDate() { return applicationDate; }
    public void setApplicationDate(LocalDate applicationDate) { this.applicationDate = applicationDate; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDate getApprovalDate() { return approvalDate; }
    public void setApprovalDate(LocalDate approvalDate) { this.approvalDate = approvalDate; }
}

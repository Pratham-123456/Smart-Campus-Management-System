package com.campus.smartcampus.model;

import java.time.LocalDate;

public class ExamRegistration {
    private Long examRegistrationId;
    private Long studentId;
    private String studentName;
    private Long examId;
    private String courseName;
    private String examType;
    private LocalDate examDate;
    private LocalDate registrationDate;
    private String status;

    public ExamRegistration() {}

    public Long getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(Long examRegistrationId) { this.examRegistrationId = examRegistrationId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

package com.campus.smartcampus.model;

import java.time.LocalDate;

public class ExamResult {
    private Long examResultId;
    private Long examRegistrationId;
    private Long studentId;
    private String studentName;
    private Long examId;
    private String courseName;
    private String examType;
    private LocalDate examDate;
    private Double marksObtained;
    private Double maxMarks;
    private Double percentage;
    private String resultStatus;
    private String gradeCode;
    private LocalDate publishedDate;
    private Boolean hasReExamApplied;
    private String reExamStatus;

    public ExamResult() {}

    public Long getExamResultId() { return examResultId; }
    public void setExamResultId(Long examResultId) { this.examResultId = examResultId; }

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

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) {
        this.marksObtained = marksObtained;
        calculatePercentage();
    }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) {
        this.maxMarks = maxMarks;
        calculatePercentage();
    }

    public Double getPercentage() { return percentage; }
    public void setPercentage(Double percentage) { this.percentage = percentage; }

    public String getResultStatus() { return resultStatus; }
    public void setResultStatus(String resultStatus) { this.resultStatus = resultStatus; }

    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }

    public LocalDate getPublishedDate() { return publishedDate; }
    public void setPublishedDate(LocalDate publishedDate) { this.publishedDate = publishedDate; }

    public Boolean getHasReExamApplied() { return hasReExamApplied; }
    public void setHasReExamApplied(Boolean hasReExamApplied) { this.hasReExamApplied = hasReExamApplied; }

    public String getReExamStatus() { return reExamStatus; }
    public void setReExamStatus(String reExamStatus) { this.reExamStatus = reExamStatus; }

    private void calculatePercentage() {
        if (marksObtained != null && maxMarks != null && maxMarks > 0) {
            double p = (marksObtained / maxMarks) * 100.0;
            this.percentage = Math.round(p * 100.0) / 100.0;
        }
    }
}

package com.campus.smartcampus.model;

public class Marks {
    private Long markId;
    private Long studentId;
    private String studentName;
    private Long offeringId;
    private String courseName;
    private Integer credits;
    private String section;
    private Double continuousAssessmentMarks;
    private Double midtermMarks;
    private Double finalExamMarks;
    private Double totalMarks;
    private String gradeCode;
    private Double gradePoint;

    public Marks() {}

    public Long getMarkId() { return markId; }
    public void setMarkId(Long markId) { this.markId = markId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public Integer getCredits() { return credits; }
    public void setCredits(Integer credits) { this.credits = credits; }

    public Double getGradePoint() { return gradePoint; }
    public void setGradePoint(Double gradePoint) { this.gradePoint = gradePoint; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Double getContinuousAssessmentMarks() { return continuousAssessmentMarks; }
    public void setContinuousAssessmentMarks(Double continuousAssessmentMarks) {
        this.continuousAssessmentMarks = continuousAssessmentMarks;
        computeTotal();
    }

    public Double getMidtermMarks() { return midtermMarks; }
    public void setMidtermMarks(Double midtermMarks) {
        this.midtermMarks = midtermMarks;
        computeTotal();
    }

    public Double getFinalExamMarks() { return finalExamMarks; }
    public void setFinalExamMarks(Double finalExamMarks) {
        this.finalExamMarks = finalExamMarks;
        computeTotal();
    }

    public Double getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Double totalMarks) { this.totalMarks = totalMarks; }

    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }

    public void computeTotal() {
        double ca = continuousAssessmentMarks != null ? continuousAssessmentMarks : 0.0;
        double mid = midtermMarks != null ? midtermMarks : 0.0;
        double fin = finalExamMarks != null ? finalExamMarks : 0.0;
        this.totalMarks = Math.round((ca + mid + fin) * 100.0) / 100.0;
    }
}

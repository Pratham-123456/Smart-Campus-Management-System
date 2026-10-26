package com.campus.smartcampus.model;

public class Grade {
    private String gradeCode;
    private Double gradePoint;
    private Double minPercentage;
    private Double maxPercentage;

    public Grade() {}

    public Grade(String gradeCode, Double gradePoint, Double minPercentage, Double maxPercentage) {
        this.gradeCode = gradeCode;
        this.gradePoint = gradePoint;
        this.minPercentage = minPercentage;
        this.maxPercentage = maxPercentage;
    }

    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }

    public Double getGradePoint() { return gradePoint; }
    public void setGradePoint(Double gradePoint) { this.gradePoint = gradePoint; }

    public Double getMinPercentage() { return minPercentage; }
    public void setMinPercentage(Double minPercentage) { this.minPercentage = minPercentage; }

    public Double getMaxPercentage() { return maxPercentage; }
    public void setMaxPercentage(Double maxPercentage) { this.maxPercentage = maxPercentage; }
}

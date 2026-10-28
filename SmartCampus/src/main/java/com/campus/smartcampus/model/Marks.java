package com.campus.smartcampus.model;

/**
 * Course marks. Component ceilings are CA 30, Midterm 30 and Final 50, so the raw sum
 * of the components is out of {@link #MAX_RAW_TOTAL} = 110. The course total reported
 * everywhere is that sum scaled to {@link #MAX_TOTAL} = 100 (raw * 100 / 110), so a
 * total never exceeds 100; the course grade is looked up from this /100 total.
 */
public class Marks {
    public static final double MAX_CA = 30.0;
    public static final double MAX_MIDTERM = 30.0;
    public static final double MAX_FINAL = 50.0;
    public static final double MAX_RAW_TOTAL = MAX_CA + MAX_MIDTERM + MAX_FINAL;
    public static final double MAX_TOTAL = 100.0;

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
    private Double rawTotal;
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

    /** Course total out of 100. */
    public Double getTotalMarks() { return totalMarks; }
    public Double getMaxTotal() { return MAX_TOTAL; }
    /** Plain sum CA + Midterm + Final (out of 110), kept for transparency. */
    public Double getRawTotal() { return rawTotal; }
    /** Same value as the /100 total; kept so existing clients reading "percentage" still work. */
    public Double getPercentage() { return totalMarks; }

    public String getGradeCode() { return gradeCode; }
    public void setGradeCode(String gradeCode) { this.gradeCode = gradeCode; }

    public void computeTotal() {
        double ca = continuousAssessmentMarks != null ? continuousAssessmentMarks : 0.0;
        double mid = midtermMarks != null ? midtermMarks : 0.0;
        double fin = finalExamMarks != null ? finalExamMarks : 0.0;
        this.rawTotal = Math.round((ca + mid + fin) * 100.0) / 100.0;
        this.totalMarks = toTotalOutOf100(this.rawTotal);
    }

    /** Scales a raw /110 component sum to the /100 course total, rounded to 2 dp. */
    public static double toTotalOutOf100(double rawTotal) {
        return Math.round(rawTotal / MAX_RAW_TOTAL * MAX_TOTAL * 100.0) / 100.0;
    }
}

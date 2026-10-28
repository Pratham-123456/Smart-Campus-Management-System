package com.campus.smartcampus.dto;

public class StudentAttendanceMarksDTO {
    private Long studentId;
    private String studentName;
    private String email;
    private String programName;
    private Integer classesHeld;
    private Integer classesAttended;
    private Double attendancePercentage;
    private Double continuousAssessmentMarks;
    private Double midtermMarks;
    private Double finalExamMarks;
    private Double totalMarks;

    public StudentAttendanceMarksDTO() {}

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public Integer getClassesHeld() { return classesHeld; }
    public void setClassesHeld(Integer classesHeld) { this.classesHeld = classesHeld; }

    public Integer getClassesAttended() { return classesAttended; }
    public void setClassesAttended(Integer classesAttended) { this.classesAttended = classesAttended; }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) { this.attendancePercentage = attendancePercentage; }

    public Double getContinuousAssessmentMarks() { return continuousAssessmentMarks; }
    public void setContinuousAssessmentMarks(Double continuousAssessmentMarks) { this.continuousAssessmentMarks = continuousAssessmentMarks; }

    public Double getMidtermMarks() { return midtermMarks; }
    public void setMidtermMarks(Double midtermMarks) { this.midtermMarks = midtermMarks; }

    public Double getFinalExamMarks() { return finalExamMarks; }
    public void setFinalExamMarks(Double finalExamMarks) { this.finalExamMarks = finalExamMarks; }

    /** Course total out of 100 (raw CA + Midterm + Final scaled from 110). */
    public Double getTotalMarks() { return totalMarks == null ? null : com.campus.smartcampus.model.Marks.toTotalOutOf100(totalMarks); }
    public Double getMaxTotal() { return com.campus.smartcampus.model.Marks.MAX_TOTAL; }
    /** Plain component sum, out of 110. */
    public Double getRawTotal() { return totalMarks; }
    /** Takes the raw component sum (as selected from MARKS). */
    public void setTotalMarks(Double rawTotal) { this.totalMarks = rawTotal; }
}

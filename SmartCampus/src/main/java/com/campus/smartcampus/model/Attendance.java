package com.campus.smartcampus.model;

public class Attendance {
    private Long attendanceId;
    private Long studentId;
    private String studentName;
    private Long offeringId;
    private String courseName;
    private String section;
    private Integer classesAttended;
    private Integer classesHeld;
    private Double attendancePercentage;
    private Boolean isShortage;

    public Attendance() {}

    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public Integer getClassesAttended() { return classesAttended; }
    public void setClassesAttended(Integer classesAttended) {
        this.classesAttended = classesAttended;
        calculatePercentage();
    }

    public Integer getClassesHeld() { return classesHeld; }
    public void setClassesHeld(Integer classesHeld) {
        this.classesHeld = classesHeld;
        calculatePercentage();
    }

    public Double getAttendancePercentage() { return attendancePercentage; }
    public void setAttendancePercentage(Double attendancePercentage) {
        this.attendancePercentage = attendancePercentage;
        this.isShortage = (attendancePercentage != null && attendancePercentage < 75.0);
    }

    public Boolean getIsShortage() { return isShortage; }
    public void setIsShortage(Boolean isShortage) { this.isShortage = isShortage; }

    private void calculatePercentage() {
        if (classesHeld != null && classesHeld > 0 && classesAttended != null) {
            double pct = ((double) classesAttended / (double) classesHeld) * 100.0;
            this.attendancePercentage = Math.round(pct * 100.0) / 100.0;
            this.isShortage = (this.attendancePercentage < 75.0);
        } else {
            this.attendancePercentage = 0.0;
            this.isShortage = false;
        }
    }
}

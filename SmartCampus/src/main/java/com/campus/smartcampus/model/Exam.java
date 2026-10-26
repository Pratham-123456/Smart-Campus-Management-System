package com.campus.smartcampus.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Exam {
    private Long examId;
    private Long offeringId;
    private String courseName;
    private String section;
    private String facultyName;
    private String examType;
    private LocalDate examDate;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String venue;
    private Double maxMarks;
    private String status;
    private Boolean isRegistered;
    private Long examRegistrationId;

    public Exam() {}

    public Long getExamId() { return examId; }
    public void setExamId(Long examId) { this.examId = examId; }

    public Long getOfferingId() { return offeringId; }
    public void setOfferingId(Long offeringId) { this.offeringId = offeringId; }

    public String getCourseName() { return courseName; }
    public void setCourseName(String courseName) { this.courseName = courseName; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getFacultyName() { return facultyName; }
    public void setFacultyName(String facultyName) { this.facultyName = facultyName; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public LocalDate getExamDate() { return examDate; }
    public void setExamDate(LocalDate examDate) { this.examDate = examDate; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getVenue() { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public Double getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Double maxMarks) { this.maxMarks = maxMarks; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Boolean getIsRegistered() { return isRegistered; }
    public void setIsRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; }

    public Long getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(Long examRegistrationId) { this.examRegistrationId = examRegistrationId; }
}

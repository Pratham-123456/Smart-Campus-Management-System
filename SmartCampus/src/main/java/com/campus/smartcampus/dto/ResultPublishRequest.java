package com.campus.smartcampus.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/** Publishes (or, with an id in the path, re-publishes) one exam result. The grade is derived from GRADE. */
public class ResultPublishRequest {
    private Long examRegistrationId;

    @NotNull(message = "Marks obtained is required")
    @DecimalMin(value = "0.0", message = "Marks obtained cannot be negative")
    private Double marksObtained;

    /** When true the result is stored as WITHHELD with no grade and is hidden from the student. */
    private Boolean withheld;

    public ResultPublishRequest() {}

    public Long getExamRegistrationId() { return examRegistrationId; }
    public void setExamRegistrationId(Long examRegistrationId) { this.examRegistrationId = examRegistrationId; }

    public Double getMarksObtained() { return marksObtained; }
    public void setMarksObtained(Double marksObtained) { this.marksObtained = marksObtained; }

    public Boolean getWithheld() { return withheld; }
    public void setWithheld(Boolean withheld) { this.withheld = withheld; }
}

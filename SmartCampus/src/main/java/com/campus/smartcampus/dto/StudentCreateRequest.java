package com.campus.smartcampus.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.time.LocalDate;

public class StudentCreateRequest {
    @NotBlank(message = "Student name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    private String phone;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dob;

    @NotNull(message = "Admission year is required")
    @Min(value = 2000, message = "Invalid admission year")
    @Max(value = 2100, message = "Invalid admission year")
    private Integer admissionYear;

    @NotNull(message = "Program ID is required")
    private Long programId;

    public StudentCreateRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public Integer getAdmissionYear() { return admissionYear; }
    public void setAdmissionYear(Integer admissionYear) { this.admissionYear = admissionYear; }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }
}

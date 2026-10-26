package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AccountProvisionRequest {
    @NotBlank(message = "Role must be STUDENT or FACULTY")
    private String role;

    @NotNull(message = "Record ID (Student ID or Faculty ID) is required")
    private Long recordId;

    @NotBlank(message = "Login username/registration number is required")
    @Size(min = 2, max = 50)
    private String username;

    @NotBlank(message = "Initial password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String password;

    public AccountProvisionRequest() {}

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getRecordId() { return recordId; }
    public void setRecordId(Long recordId) { this.recordId = recordId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

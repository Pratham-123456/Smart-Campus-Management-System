package com.campus.smartcampus.dto;

import jakarta.validation.constraints.NotBlank;

public class LoginRequest {
    @NotBlank(message = "Portal role is required (STUDENT, FACULTY, ADMINISTRATOR)")
    private String role;

    @NotBlank(message = "Username / Identifier is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;

    // Optional legacy identifier fallback
    private Long id;
    private String email;

    public LoginRequest() {}

    public LoginRequest(String role, String username, String password) {
        this.role = role;
        this.username = username;
        this.password = password;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getUsername() {
        if (username != null && !username.trim().isEmpty()) {
            return username.trim();
        }
        if (id != null) {
            return String.valueOf(id);
        }
        return null;
    }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}

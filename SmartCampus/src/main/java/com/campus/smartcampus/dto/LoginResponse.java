package com.campus.smartcampus.dto;

public class LoginResponse {
    private Long accountId;
    private String username;
    private String role;
    private Long id;
    private String name;
    private String email;
    private String departmentName;
    private String programName;
    private String token;

    public LoginResponse() {}

    public LoginResponse(Long accountId, String username, String role, Long id, String name, String email, String departmentName, String programName, String token) {
        this.accountId = accountId;
        this.username = username;
        this.role = role;
        this.id = id;
        this.name = name;
        this.email = email;
        this.departmentName = departmentName;
        this.programName = programName;
        this.token = token;
    }

    public Long getAccountId() { return accountId; }
    public void setAccountId(Long accountId) { this.accountId = accountId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}

package com.campus.smartcampus.dto;

import jakarta.validation.constraints.Size;

/** Partial update of a USER_ACCOUNT: toggle Is_Active and/or set a new password (BCrypt-hashed). */
public class AccountUpdateRequest {
    private Boolean active;

    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    private String newPassword;

    public AccountUpdateRequest() {}

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}

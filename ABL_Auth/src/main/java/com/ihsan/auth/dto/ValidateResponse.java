package com.ihsan.auth.dto;

public class ValidateResponse {
    private boolean valid;
    private String email;
    private String role;
    private String message;

    public ValidateResponse() {
    }

    public ValidateResponse(boolean valid, String email, String role, String message) {
        this.valid = valid;
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

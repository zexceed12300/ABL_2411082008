package com.ihsan.auth.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private String email;
    private String role;
    private String message;

    public AuthResponse() {
    }

    public AuthResponse(String token, String email, String role, String message) {
        this.token = token;
        this.type = "Bearer";
        this.email = email;
        this.role = role;
        this.message = message;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
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

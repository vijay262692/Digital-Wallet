package com.digitalwallet.model;

import java.util.*;
import java.time.LocalDateTime;

public class User {

    private String username;
    private String email;
    private String password;
    private String role; // "ADMIN" or "USER"
    private boolean active;
    private String activationCode;
    private LocalDateTime activationExpiry;

    public User() {}

    // 🧩 Full constructor for registration
    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.active = true; // default: active on creation
        this.activationCode = UUID.randomUUID().toString();
        this.activationExpiry = LocalDateTime.now().plusMinutes(15);
    }

    // 🧩 Constructor for simple login/session use
    public User(String username, String password, String role) {
        this(username, null, password, role);
    }

    // ✅ Getters & Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getActivationCode() { return activationCode; }
    public void setActivationCode(String activationCode) { this.activationCode = activationCode; }

    public LocalDateTime getActivationExpiry() { return activationExpiry; }
    public void setActivationExpiry(LocalDateTime activationExpiry) { this.activationExpiry = activationExpiry; }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}

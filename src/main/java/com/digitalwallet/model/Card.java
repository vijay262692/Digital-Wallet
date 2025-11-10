package com.digitalwallet.model;

import javax.persistence.*;

@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // BIGINT PK

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // FK

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private String maskedPan;

    @Column(nullable = false)
    private String expiry;

    @Column(nullable = false)
    private String provider;

    @Column(nullable = false)
    private String status;

    // ---- GETTERS & SETTERS ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMaskedPan() { return maskedPan; }
    public void setMaskedPan(String maskedPan) { this.maskedPan = maskedPan; }

    public String getExpiry() { return expiry; }
    public void setExpiry(String expiry) { this.expiry = expiry; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

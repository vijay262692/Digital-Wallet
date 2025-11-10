package com.digitalwallet.model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "transactions")
public class TransactionRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // BIGINT PK

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // FK

    @ManyToOne
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;  // FK

    private String token;
    private String merchant;
    private String provider;
    private String maskedPan;
    private String status;
    private double amount;

    @Temporal(TemporalType.TIMESTAMP)
    private Date timestamp = new Date();

    // ---- GETTERS & SETTERS ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Wallet getWallet() { return wallet; }
    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getMerchant() { return merchant; }
    public void setMerchant(String merchant) { this.merchant = merchant; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getMaskedPan() { return maskedPan; }
    public void setMaskedPan(String maskedPan) { this.maskedPan = maskedPan; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }
}

package com.digitalwallet.model;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // BIGINT PK

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;  // FK → users.id (BIGINT)

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    // ---- GETTERS & SETTERS ----
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
}

# 💳 Digital Wallet System (Enhanced)

A secure and feature-rich Digital Wallet application built using **Spring Boot**, demonstrating modern payment flows including **tokenized cards, OTP verification, Razorpay integration, email & WhatsApp notifications, and transaction exports (CSV/PDF)**.

> ⚠️ This is a demo/academic project. Not production-ready without security hardening.

---

# 📌 Table of Contents

* Overview
* Features
* Architecture
* Database Design
* Setup & Run
* API Reference
* Integrations
* Tech Stack
* Future Enhancements
* Author

---

# 🔍 Overview

This project simulates a **real-world digital wallet system**:

* Secure login using **RSA encryption**
* Tokenized card storage (no PAN stored)
* Wallet payments + Razorpay payments
* OTP-based verification
* Email & WhatsApp notifications
* Downloadable transaction statements (CSV & PDF)

---

# 🚀 Features

## 🔐 Security

* RSA encryption (client → server)
* Wallet PIN (BCrypt hashed)
* OTP-based account activation
* OTP for high-value transactions (₹5000+ ready)

---

## 💳 Card Management

* Add card (tokenized via mock VTS/MDES)
* Masked PAN storage
* Card lifecycle:

  * ACTIVE
  * SUSPENDED
  * TERMINATED

---

## 💸 Payments

### 1. Wallet Payment

* Pay using tokenized cards
* PIN validation required
* Transaction stored with metadata

### 2. Razorpay Integration

* Real payment gateway
* Order creation + verification
* Transaction recorded in system

---

## 📧 Notifications

### Email

* Account activation
* Payment success
* CSV + PDF attachments

### WhatsApp (Meta Cloud API)

* Payment success message
* Real-time notification

Example:

```
✅ Payment Successful

Merchant: Amazon
Amount: ₹500.00
Status: SUCCESS
Card: **** **** 1234
Provider: VISA
Txn ID: 12345
Date: 2026-04-21 14:30:00
```

---

## 📊 Transactions

* View transaction history
* Pagination
* Auto-refresh
* Date filter (calendar)
* Export options:

  * CSV
  * PDF (with formatted receipt)

---

## 📄 Receipt System

* Auto-generated receipt after payment
* Opens in new tab
* Printable format
* Logo supported

---

## 📥 Export Features

### CSV Export

```
GET /api/wallet/transactions/{username}/export
```

### PDF Export

```
GET /api/wallet/transactions/{username}/export/pdf
```

Supports:

* Date filtering
* Structured table format

---

# 🏗️ Architecture

```
Frontend (HTML + JS)
        ↓ (RSA Encrypted)
Spring Boot Backend
        ↓
Business Logic Layer
        ↓
Repositories (JPA)
        ↓
PostgreSQL DB
        ↓
External Integrations:
   - Razorpay API
   - Email (SMTP)
   - WhatsApp Cloud API
```

---

# 🗄️ Database Design

Tables:

### users

* username
* email
* password
* activated
* walletPin
* otpCode
* otpExpiry

### wallets

* balance
* user_id

### cards

* token
* masked_pan
* provider
* status

### transactions

* id
* amount
* merchant
* provider
* status
* timestamp
* referenceId
* channel (CARD / RAZORPAY)

---

# ⚙️ Setup & Run

## 1. Clone

```bash
git clone https://github.com/vijay262692/Digital-Wallet.git
cd Digital-Wallet
```

---

## 2. Configure DB

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/walletdb
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

---

## 3. Mail Config

```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## 4. WhatsApp Config

```properties
whatsapp.token=YOUR_META_TOKEN
whatsapp.phoneNumberId=YOUR_PHONE_ID
```

---

## 5. Run

```bash
mvn clean install
mvn spring-boot:run
```

---

## 🌐 App URL

```
http://localhost:8080
```

---

# 🔌 API Reference (Important)

### Auth

* POST `/api/user/register`
* POST `/api/user/login`
* GET `/api/user/activate`

---

### Wallet

* POST `/api/wallet/addCard/{username}`
* GET `/api/wallet/cards/{username}`
* POST `/api/wallet/pay`

---

### Razorpay

* POST `/api/wallet/create-order`
* POST `/api/wallet/verify-payment`

---

### Transactions

* GET `/api/wallet/transactions/{username}`
* GET `/api/wallet/transactions/{username}/export`
* GET `/api/wallet/transactions/{username}/export/pdf`

---

# 🔗 Integrations

## Razorpay

* Secure payment gateway
* Order + Payment verification

## Email (JavaMailSender)

* Sends CSV + PDF attachments

## WhatsApp Cloud API

* Sends payment success messages

---

# 🧠 Advanced Concepts Used

* RSA Encryption
* Tokenization (Card Security)
* BCrypt Hashing
* OTP Expiry Handling
* File Streaming (CSV/PDF)
* REST APIs
* Third-party API Integration

---

# 🚀 Future Enhancements

* JWT Authentication
* Redis caching
* Fraud detection system
* Event-driven architecture (Kafka)
* Push notifications
* Mobile app (React Native)

---

# 👨‍💻 Author

**BaluRaju P V**
Senior Software Engineer

---

# ⚠️ Security Notes

* Do NOT store secrets in code
* Use HTTPS in production
* Use KMS for key storage
* Implement rate limiting

---

# ⭐ Summary

This project demonstrates a **real-world fintech architecture** including:

✔ Secure payments
✔ External integrations
✔ Notifications
✔ Export systems
✔ Modern backend practices

---

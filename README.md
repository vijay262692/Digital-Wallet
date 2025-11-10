# 💳 Digital Wallet Demo

A secure digital wallet system built with **Spring Boot**, supporting **RSA Encryption**, **Tokenized Cards**, **Wallet Balances**, and **Transaction Logs** stored in **PostgreSQL**.

This project demonstrates how real card networks (Visa VTS / Mastercard MDES) tokenize card data — implemented with a mock routing engine.

---

## 🚀 Features

| Feature | Description |
|-------|-------------|
| 🔐 **Secure Login & Register** | Credentials encrypted via RSA (Forge.js + Java) |
| 💳 **Add Card (Tokenization)** | PAN is never stored — instead, network token is saved |
| 👁️ **View Saved Cards** | Cards are masked + status-based actions allowed |
| 💸 **Make Payments** | Payments processed using stored tokens |
| 📜 **Transaction History** | All payments stored in PostgreSQL |
| 👤 **User Session** | Browser localStorage-based login persistence |
| 🧱 **PostgreSQL + JPA** | Persistent relational storage for all data |

---

## 🏛️ System Architecture

Browser (HTML + JS)
↓ RSA Encrypt (Forge.js)
Spring Boot API
↓ Decrypt (RSA Private Key)
Tokenization Router (Mock VTS / MDES)
↓
PostgreSQL (users, wallets, cards, transactions)




---

## 🗂️ Database Tables

| Table | Purpose |
|-------|---------|
| `users` | Stores registered user accounts |
| `wallets` | One wallet per user, tracks balance |
| `cards` | Tokenized card entries (masked PAN + token) |
| `transactions` | Payments processed via tokenized cards |

---

## ⚙️ Setup Instructions

### 1️⃣ Clone Repo
```bash
git clone https://github.com/vijay262692/Digital-Wallet.git
cd Digital-Wallet




2️⃣ Configure Database (application.properties)
spring.datasource.url=jdbc:postgresql://localhost:5432/walletdb
spring.datasource.username=postgres
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=create
spring.jpa.show-sql=true

3️⃣ Build & Run
mvn clean install
mvn spring-boot:run


App Runs On → http://localhost:8080

🌐 Available Pages (UI)
Page	File	Description
Login	login.html	User login (RSA encrypted)
Register	register.html	Sign up new user
Dashboard	dashboard.html	App navigation home
Add Card	add-card.html	Encrypts PAN + Tokenizes
View Cards	view-cards.html	Displays tokenized cards
Make Payment	make-payment.html	Pay using token
Transactions	transactions.html	View transaction history

All UI files are served from:

src/main/resources/static/

🔑 REST API Reference

Action	Method	Endpoint

Get Public Key	GET	/api/wallet/publicKey
Register User	POST	/api/user/register
Login User	POST	/api/user/login
Add Card	POST	/api/wallet/addCard/{username}
List Cards	GET	/api/wallet/cards/{username}
Make Payment	POST	/api/wallet/pay
List Transactions	GET	/api/wallet/transactions/{username}


Sample Payment Request:
{
  "username": "vijay",
  "token": "VISA-TOKEN-82kdn1x",
  "amount": 249.50,
  "merchant": "Amazon"
}



🧩 Tech Stack

Layer	Technology
Backend	Java 17, Spring Boot 2.7, Spring Data JPA
Encryption	RSA (Java Crypto + Forge.js)
Frontend	HTML5, CSS3, Vanilla JS, Axios
Database	PostgreSQL
Build Tool	Maven



🛣️ Roadmap

✅ PostgreSQL DB Persistence

✅ Status update on Cards (ACTIVE/SUSPENDED/TERMINATED)

🔥 JWT Authentication + Secure Sessions


👤 Author

BaluRaju P V

MIT License © 2025

Notes:
- This is a demo. Do NOT use this code in production without proper security review.

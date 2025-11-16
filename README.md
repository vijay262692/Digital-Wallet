# Digital Wallet Demo

A secure digital wallet demo built with Spring Boot that demonstrates RSA encryption for client → server communication, tokenized cards (mock Visa/ Mastercard tokenization), wallet balances, and transaction logs persisted to PostgreSQL. This repository is intended as an educational demo — do not use in production without a full security review.

Table of contents
- Overview
- Features
- Architecture
- Database model
- Setup & run
- REST API reference
- MailService (new)
- Tech stack
- Roadmap
- Author & license
- Security notes

---

## Overview

This project shows how a browser-based client can securely send credentials or PAN (Primary Account Number) to a backend using RSA (client-side Forge.js encryption + server-side Java decryption). Card PANs are never persisted — the application simulates a tokenization router (VTS/MDES) and stores only masked PANs and network tokens.

---

## Features

- Secure login and registration using RSA encryption for transport
- Tokenized cards: PAN is exchanged for a network token, PAN is not stored
- Masked card display and card lifecycle status (ACTIVE / SUSPENDED / TERMINATED)
- Wallet per user with balance tracking
- Payments using stored tokens and transaction logging
- Browser localStorage-based lightweight session persistence (demo)
- PostgreSQL persistence via Spring Data JPA

---

## System Architecture

Browser (HTML + JS with Forge.js)
↓ (RSA-encrypted payload)
Spring Boot API (RSA private key decrypt)
↓
Mock Tokenization Router (VTS / MDES)
↓
PostgreSQL (users, wallets, cards, transactions)

All static UI pages are served from src/main/resources/static.

---

## Database tables (high-level)

- users — Registered user accounts (username, passwordHash, email, ...)
- wallets — One per user; stores balance
- cards — Tokenized card entries (masked PAN, token, status)
- transactions — Payment events, amount, merchant, timestamp

---

## Setup & Run

1. Clone
   git clone https://github.com/vijay262692/Digital-Wallet.git
   cd Digital-Wallet

2. Configure the database in src/main/resources/application.properties (or override via environment variables)
   spring.datasource.url=jdbc:postgresql://localhost:5432/walletdb
   spring.datasource.username=postgres
   spring.datasource.password=root
   spring.jpa.hibernate.ddl-auto=create
   spring.jpa.show-sql=true

   Recommended: use environment variables or a secure secrets manager for credentials in non-demo deployments.

3. Build & run
   mvn clean install
   mvn spring-boot:run

4. App URL
   http://localhost:8080

UI pages (served from src/main/resources/static/):
- login.html — User login (client-side RSA encryption)
- register.html — New user signup
- dashboard.html — App home
- add-card.html — Add card (PAN encrypted on client)
- view-cards.html — View tokenized cards (masked)
- make-payment.html — Make payment using a stored token
- transactions.html — View transaction history

---

## REST API Reference

- GET /api/wallet/publicKey — Fetch RSA public key for client encryption
- POST /api/user/register — Register a new user
- POST /api/user/login — Login
- POST /api/wallet/addCard/{username} — Add a tokenized card for a user
- GET /api/wallet/cards/{username} — List cards for a user
- POST /api/wallet/pay — Make a payment
- GET /api/wallet/transactions/{username} — List user transactions

Sample payment request body:
{
  "username": "vijay",
  "token": "VISA-TOKEN-82kdn1x",
  "amount": 249.50,
  "merchant": "Amazon"
}

---

## MailService (NEW)

This demo includes a lightweight MailService concept to support email notifications (e.g., registration confirmation, payment receipts, alerts). The following describes recommended configuration and usage patterns for a simple yet robust MailService.

Purpose
- Send transactional emails (registration welcome, password reset links, payment receipts, account alerts)
- Use templated email bodies (Thymeleaf, FreeMarker, or simple text templates)
- Support async sending and basic retry/error handling in the demo

Configuration (application.properties or environment variables)
- spring.mail.host=smtp.example.com
- spring.mail.port=587
- spring.mail.username=your-smtp-username
- spring.mail.password=your-smtp-password
- spring.mail.properties.mail.smtp.auth=true
- spring.mail.properties.mail.smtp.starttls.enable=true
- spring.mail.default-from=no-reply@example.com

Security note: Do not store plaintext credentials in version control. Use environment variables, a vault, or CI/CD secrets.

Suggested Java MailService implementation (concept)
- A Spring @Service class (MailService) that depends on JavaMailSender
- Send methods:
  - sendSimpleEmail(to, subject, body)
  - sendTemplateEmail(to, subject, templateName, model)
  - sendAsync(...) — annotated with @Async or using a TaskExecutor
- Optional: a lightweight retry policy for transient SMTP failures (e.g., RetryTemplate or Spring Retry)

Example usage (pseudo)
- After successful registration, queue a welcome email:
  mailService.sendTemplateEmail(user.getEmail(), "Welcome to Digital Wallet", "welcome.html", model);

Testing
- Use a test SMTP server (GreenMail, MailHog) for integration tests
- In unit tests, mock JavaMailSender and assert that compose/send methods are called
- For local development, direct emails to MailHog or stdout

Templates
- Place email templates in src/main/resources/templates/
- Use Thymeleaf/FreeMarker or simple string templates for demo

Delivery & fallback
- For production-quality projects, add delivery retries, dead-lettering, or DB-backed queueing for failed sends
- Track message send status in a lightweight table if required

Example environment variable names for container deployments
- MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD, MAIL_FROM

---

## Tech stack

- Java 17
- Spring Boot 2.7.x
- Spring Data JPA (Hibernate)
- PostgreSQL
- RSA encryption (Java Crypto + Forge.js on the frontend)
- Frontend: HTML5, CSS3, Vanilla JS, Axios
- Build tool: Maven

---

## Roadmap

Planned / in-demo
- PostgreSQL DB persistence — done
- Card status lifecycle (ACTIVE / SUSPENDED / TERMINATED) — done (demo)
- MailService (transactional emails, README describes configuration) — added to README (implementation can be added on request)

Proposed / future
- JWT authentication & secure sessions
- Production-ready secure key management (KMS/HSM)
- Rate limiting and fraud detection hooks
- Proper logging and observability (metrics/tracing)

---

## Author

BaluRaju P V

MIT License © 2025

---

## Security & usage notes

- This project is a demo and is NOT intended for production without a security review.
- Do not store private keys, SMTP credentials, or database passwords in public repos.
- Use HTTPS and secure key storage in real deployments.

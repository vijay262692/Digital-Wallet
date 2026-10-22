# Digital Wallet System — Project Documentation

## 1. Project Overview

The Digital Wallet System is a Spring Boot-based fintech demonstration platform that models secure wallet and card-payment workflows. It supports encrypted client-to-server authentication, tokenized-card storage, wallet/card payments, Razorpay payment flows, OTP-based activation, notifications, transaction history, and CSV/PDF statement generation.

> **Project status:** Demonstration/academic project. It should not be treated as production-ready without the security, compliance, observability, and operational controls listed in this document.

## 2. Objectives

- Provide a digital-wallet style user experience.
- Protect sensitive card data by avoiding storage of raw PAN/CVV.
- Demonstrate card tokenization and provider routing concepts.
- Support wallet/card payments and an external payment-gateway flow.
- Provide transaction history and downloadable statements.
- Demonstrate email and WhatsApp payment notifications.

## 3. Technology Stack

| Area | Technology |
|---|---|
| Backend | Java 17, Spring Boot 2.7.14 |
| API | Spring MVC / REST |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Security | RSA encryption, BCrypt, JWT libraries |
| Payment gateway | Razorpay Java SDK |
| Email | Spring Mail / SMTP |
| PDF | iText/OpenPDF-compatible library used by the application |
| Frontend | HTML, CSS, JavaScript |
| Build | Maven |

The Maven build defines Spring Web, Spring Data JPA, PostgreSQL, mail, Razorpay, JWT, BCrypt-related security crypto, Jackson, Lombok and PDF dependencies. fileciteturn5file0

## 4. High-Level Architecture

```text
Browser / HTML + JavaScript
          |
          | REST + encrypted payloads
          v
Spring Boot REST Controllers
          |
          +--> Authentication / User management
          +--> Wallet & Card management
          +--> Payment processing
          +--> Transaction reporting
          |
          v
Service / Broker Layer
          |
          +--> Tokenization / provider routing
          +--> Email notification
          +--> WhatsApp notification
          +--> Razorpay integration
          |
          v
Spring Data JPA / Hibernate
          |
          v
PostgreSQL
```

## 5. Core Modules

### 5.1 User & Authentication

- User registration and login.
- RSA-encrypted client/server credential exchange.
- OTP-based account activation.
- BCrypt-protected wallet PIN.
- JWT-related dependencies are present for token-based security evolution.

### 5.2 Card Management

- Add a card through an encrypted request.
- Tokenization through the provider-routing layer.
- Store a masked PAN rather than the raw PAN.
- Card states include `ACTIVE`, `SUSPENDED`, and `TERMINATED`.
- CVV is intentionally not persisted.

### 5.3 Wallet & Payments

- Wallet creation/lookup per user.
- Card-token based payment processing.
- Wallet PIN validation before payment.
- Card status and available-balance checks.
- Transaction record creation with merchant, amount, provider, reference and channel metadata.

### 5.4 Razorpay Flow

```text
Client -> Create Order API -> Razorpay
Client -> Razorpay checkout/payment
Client -> Verify Payment API -> Application
Application -> Save transaction -> Notify user
```

### 5.5 Notifications

Payment completion can trigger:

- Email payment receipt.
- CSV/PDF transaction statement attachment.
- WhatsApp payment notification.

### 5.6 Transaction Reporting

The application supports transaction retrieval, pagination/date filtering in the UI, CSV export and PDF export/receipt generation.

## 6. Important APIs

### Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/user/register` | Register a user |
| POST | `/api/user/login` | Login |
| GET | `/api/user/activate` | Activate account |

### Wallet / Cards

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/wallet/publicKey` | Obtain RSA public key |
| POST | `/api/wallet/addCard/{username}` | Add/tokenize a card |
| GET | `/api/wallet/cards/{username}` | List user cards |
| POST | `/api/wallet/cardStatus` | Change card lifecycle state |
| POST | `/api/wallet/pay` | Process card/wallet payment |
| POST | `/api/wallet/topup` | Wallet/card top-up flow |

### Razorpay

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/wallet/create-order` | Create Razorpay order |
| POST | `/api/wallet/verify-payment` | Record/verify payment result |

### Transactions

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/wallet/transactions/{username}` | Transaction history |
| GET | `/api/wallet/transactions/{username}/export` | CSV export |
| GET | `/api/wallet/transactions/{username}/export/pdf` | PDF export |

The repository README documents the primary authentication, wallet, Razorpay and transaction endpoints. fileciteturn1file0

## 7. Card Tokenization Flow

```text
User enters card details
        |
        v
Client encrypts payload with RSA public key
        |
        v
Wallet API decrypts payload
        |
        v
Tokenization / Provider Routing
        |
        v
Token + provider + masked PAN + expiry stored
        |
        v
Raw PAN/CVV are not persisted
```

The wallet controller explicitly ignores CVV for persistence and stores token/provider/masked-PAN information. fileciteturn7file0

## 8. Payment Flow

1. Validate user.
2. Ensure a wallet exists.
3. Validate wallet PIN using BCrypt.
4. Resolve the card by token.
5. Confirm the card is active.
6. Check available card balance.
7. Create the transaction record.
8. Deduct the payment amount.
9. Send receipt/notification asynchronously or defensively so notification failure does not invalidate the payment result.
10. Return transaction details and remaining balance.

## 9. Data Model

Core business entities represented by the application include:

### Users

Identity, login, activation and wallet-PIN information.

### Wallets

User wallet relationship and balance.

### Cards

User relationship, token, masked PAN, expiry, provider, status and balance-related payment state.

### Transactions

Transaction identifier, amount, merchant, provider, status, token/reference information, user/wallet relationships, timestamp and payment channel.

The project README summarizes the principal `users`, `wallets`, `cards` and `transactions` fields. fileciteturn1file0

## 10. Local Setup

### Prerequisites

- JDK 17
- Maven
- PostgreSQL
- Razorpay test credentials for gateway testing
- SMTP credentials for email testing
- Meta WhatsApp Cloud API configuration if WhatsApp notifications are enabled

### Database

Create a PostgreSQL database named `walletdb`, then configure the datasource using environment-specific credentials.

### Application

```bash
mvn clean install
mvn spring-boot:run
```

Default application port: `8080`.

The repository README provides the baseline database, mail, WhatsApp and run configuration. fileciteturn1file0

## 11. Configuration & Secret Management

**Never commit credentials, API keys, SMTP passwords, WhatsApp tokens, or payment-gateway secrets to Git.** Use environment variables or a secret manager, for example:

```properties
SPRING_DATASOURCE_URL=...
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
SPRING_MAIL_USERNAME=...
SPRING_MAIL_PASSWORD=...
RAZORPAY_KEY_ID=...
RAZORPAY_KEY_SECRET=...
WHATSAPP_TOKEN=...
WHATSAPP_PHONE_NUMBER_ID=...
```

The current repository configuration contains credentials/secrets in `application.properties`; these should be rotated immediately and removed from source control. fileciteturn6file0

## 12. Security Considerations

Current concepts:

- RSA encryption for client/server payload protection.
- BCrypt wallet-PIN verification.
- OTP activation.
- Masked card display.
- Token-based card representation.

Required production hardening:

- Use HTTPS everywhere.
- Move all secrets to a secret manager.
- Rotate any credentials that were committed to Git.
- Implement strong authentication/authorization and role-based access control.
- Add rate limiting and account lockout for authentication/PIN attempts.
- Avoid returning sensitive tokens unnecessarily in API responses.
- Validate ownership and authorization consistently on every card/wallet operation.
- Add idempotency for payment APIs.
- Add transaction locking/atomicity around balance deductions.
- Use a production-supported PDF library/version and review licensing/security.
- Add audit logs and fraud monitoring.
- Review PCI DSS scope and compliance before handling real payment data.

## 13. Testing Strategy

Recommended test layers:

1. Unit tests for services and validation.
2. Repository tests using an isolated database.
3. Controller/API integration tests.
4. Payment gateway mock tests.
5. Tokenization-provider mock tests.
6. Security tests for authentication, authorization and input validation.
7. Negative tests for duplicate payments, insufficient balance and inactive cards.
8. Export tests for CSV/PDF correctness.

## 14. Deployment Model

```text
Load Balancer / API Gateway
          |
       HTTPS
          |
Spring Boot Application
          |
   PostgreSQL Database
          |
  External Payment APIs
  SMTP / WhatsApp APIs
```

For production, add centralized logging, metrics, tracing, secrets management, database backups, health checks, alerting and CI/CD.

## 15. Future Enhancements

- Full JWT/OAuth2 security model.
- Redis caching.
- Kafka/event-driven payment notifications.
- Fraud/risk engine.
- Push notifications.
- Mobile application.
- Stronger payment idempotency and reconciliation.
- Production tokenization provider integrations.
- Automated CI/CD with security scanning.

## 16. Project Summary

This project demonstrates a broad fintech application architecture covering secure authentication, tokenized card management, wallet payments, external payment-gateway integration, transaction reporting and customer notifications. The codebase is suitable as a learning/prototype platform and as a foundation for a more hardened production architecture.

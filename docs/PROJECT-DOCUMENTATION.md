# Digital Wallet & Digital Issuance Platform — Project Documentation

## 1. Project Overview

The Digital Wallet System is a Spring Boot-based fintech demonstration platform that now includes a Digital Card Issuance and Token Lifecycle module. It models secure card onboarding, network-token provisioning, wallet/card payments, Razorpay payment flows, OTP-based activation, notifications, transaction history, and CSV/PDF statement generation.

> **Project status:** Demonstration/PoC. VTS and MDES adapters currently simulate network calls. Production network integration requires the appropriate provider credentials, certificates, APIs, certification and compliance controls.

## 2. Digital Issuance Scope

The new issuance module implements three requested capabilities:

1. **Digital Card Issuance** — issue a digital card from an RSA-encrypted card payload.
2. **Token Provisioning** — route Visa cards to VTS and Mastercard cards to MDES through the existing `PNOBroker`.
3. **Token Lifecycle Management** — activate, suspend, resume and terminate provisioned tokens.

The existing wallet card flow remains available, while the new `/api/issuance` APIs provide a cleaner Digital Issuance-oriented interface.

## 3. Technology Stack

| Area | Technology |
|---|---|
| Backend | Java 17, Spring Boot 2.7.14 |
| API | Spring MVC / REST |
| Persistence | Spring Data JPA / Hibernate |
| Database | PostgreSQL |
| Security | RSA encryption, BCrypt, JWT libraries |
| Token routing | PNOBroker, VTS adapter, MDES adapter |
| Payment gateway | Razorpay Java SDK |
| Email | Spring Mail / SMTP |
| Frontend | HTML, CSS, JavaScript, Node Forge |
| Build | Maven |

## 4. High-Level Architecture

```text
Browser / Wallet UI
        |
        | RSA-encrypted card payload
        v
DigitalIssuanceController
        |
        v
DigitalIssuanceService
        |
        +--> User validation
        +--> RSA decryption
        +--> Card-data validation
        +--> PNOBroker
                  |
             +----+----+
             |         |
            VTS       MDES
             |         |
             +----+----+
                  |
             Network Token
                  |
                  v
              Card Entity
                  |
                  v
             PostgreSQL

Lifecycle:
Token -> ACTIVE -> SUSPENDED -> ACTIVE
   |                    |
   +--------------------+--> TERMINATED
```

## 5. Core Modules

### 5.1 Digital Card Issuance

Endpoint:

```http
POST /api/issuance/cards
```

Request:

```json
{
  "username": "john",
  "encryptedCardPayload": "<RSA_BASE64_PAYLOAD>"
}
```

The encrypted payload contains the demo card format:

```text
PAN|MM/YY|CVV
```

The backend decrypts it, validates the PAN/expiry, routes it to the appropriate token provider, and persists only the token, provider, masked PAN, expiry and lifecycle status.

Initial lifecycle state:

```text
PENDING
```

### 5.2 Token Provisioning

The existing `PNOBroker` determines the provider from the PAN network prefix:

```text
4xxxxxxxxxxxxxxx -> VTS
5xxxxxxxxxxxxxxx -> MDES
```

The broker already provided the tokenization abstraction; the Digital Issuance service now uses that same broker rather than creating a second tokenization path.

Current implementation:

- Visa → `VTSService.tokenize()`
- Mastercard → `MDESService.tokenize()`
- Unsupported network → provisioning failure

The VTS and MDES classes currently return generated demo tokens. Replace these adapters with the real provider API calls when production credentials and certification are available.

### 5.3 Token Lifecycle Management

The new lifecycle APIs are:

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/issuance/tokens/{token}/activate` | Activate a pending token |
| POST | `/api/issuance/tokens/{token}/suspend` | Suspend an active token |
| POST | `/api/issuance/tokens/{token}/resume` | Resume a suspended token |
| POST | `/api/issuance/tokens/{token}/terminate` | Permanently terminate a token |
| GET | `/api/issuance/tokens/{token}` | Retrieve token/card status |

Valid transitions:

```text
PENDING  -> ACTIVE
ACTIVE   -> SUSPENDED
SUSPENDED -> ACTIVE
ACTIVE   -> TERMINATED
SUSPENDED -> TERMINATED
```

A terminated token cannot be reactivated, suspended or resumed.

Lifecycle requests are routed back through `PNOBroker`, which delegates to the same provider that provisioned the token. The provider adapters currently simulate lifecycle calls and return a successful provider response.

## 6. Card Management

The existing card model stores:

- User relationship
- Token
- Masked PAN
- Expiry
- Provider
- Status
- Demo balance

Raw PAN and CVV are not persisted by the issuance service.

The card repository ID type has also been aligned with the `Card.id` `Long` primary key.

## 7. Existing Wallet & Payment Modules

The platform continues to support:

- Wallet creation/lookup per user.
- Card-token based payment processing.
- Wallet PIN validation.
- Card status and balance checks.
- Transaction record creation.
- Razorpay order/payment flow.
- Email receipts.
- WhatsApp notifications.
- CSV/PDF transaction reporting.

## 8. Frontend Changes

### Digital Card Issuance

`add-card.html` now acts as the Digital Card Issuance screen:

```text
Enter Card Details
       |
       v
RSA encrypt in browser
       |
       v
POST /api/issuance/cards
       |
       v
Digital Card + Token
       |
       v
PENDING
```

### Token Lifecycle UI

`view-cards.html` now displays lifecycle-aware actions:

- PENDING → Activate
- ACTIVE → Suspend / Terminate
- SUSPENDED → Resume / Terminate
- TERMINATED → No actions

## 9. Card Tokenization Flow

```text
User enters card details
        |
        v
Browser fetches RSA public key
        |
        v
Browser encrypts PAN|expiry|CVV
        |
        v
POST /api/issuance/cards
        |
        v
DigitalIssuanceService
        |
        v
RSA decrypt
        |
        v
PNOBroker
     /       \
   VTS       MDES
     \       /
      Network Token
           |
           v
     Store Card record
           |
           v
        PENDING
```

## 10. Lifecycle Flow

```text
              +-----------+
              |  PENDING  |
              +-----+-----+
                    |
                 ACTIVATE
                    |
                    v
              +-----------+
        +---->|   ACTIVE  |----+
        |     +-----+-----+    |
        |           |          |
      RESUME      SUSPEND    TERMINATE
        |           |          |
        |           v          v
        |     +-----------+ +-----------+
        +-----| SUSPENDED | | TERMINATED|
              +-----------+ +-----------+
                    |
                  TERMINATE
                    |
                    +---------> TERMINATED
```

## 11. Existing Important APIs

### Authentication

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/user/register` | Register a user |
| POST | `/api/user/login` | Login |
| GET | `/api/user/activate` | Activate account |

### Digital Issuance

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/issuance/cards` | Issue card + provision token |
| GET | `/api/issuance/tokens/{token}` | Get token status |
| POST | `/api/issuance/tokens/{token}/activate` | Activate |
| POST | `/api/issuance/tokens/{token}/suspend` | Suspend |
| POST | `/api/issuance/tokens/{token}/resume` | Resume |
| POST | `/api/issuance/tokens/{token}/terminate` | Terminate |

### Existing Wallet / Cards

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/wallet/publicKey` | Obtain RSA public key |
| POST | `/api/wallet/addCard/{username}` | Existing add/tokenize card flow |
| GET | `/api/wallet/cards/{username}` | List user cards |
| POST | `/api/wallet/cardStatus` | Existing card status API |
| POST | `/api/wallet/pay` | Process card/wallet payment |
| POST | `/api/wallet/topup` | Top-up flow |

### Razorpay

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/wallet/create-order` | Create Razorpay order |
| POST | `/api/wallet/verify-payment` | Record/verify payment result |

## 12. Data Model

The Digital Issuance PoC currently reuses the existing `cards` table rather than introducing a duplicate card table.

```text
users
  |
  +----< cards
            |
            +-- token
            +-- provider
            +-- masked_pan
            +-- expiry
            +-- status
            +-- balance

cards
  |
  +----< transactions
```

For a production TSP-style platform, the next evolution would be separate `digital_cards`, `tokens`, `devices`, `issuers` and `token_events` tables.

## 13. Local Setup

Prerequisites:

- JDK 17
- Maven
- PostgreSQL
- Razorpay test credentials for gateway testing
- SMTP credentials if email is enabled
- WhatsApp Cloud API configuration if WhatsApp is enabled

Run:

```bash
mvn clean install
mvn spring-boot:run
```

Default port: `8080`.

## 14. Testing the New APIs

### Step 1 — Get RSA public key

```http
GET /api/wallet/publicKey
```

### Step 2 — Encrypt card payload

Encrypt:

```text
4111111111111111|12/29|123
```

with the returned RSA public key.

### Step 3 — Issue digital card

```http
POST /api/issuance/cards
Content-Type: application/json
```

```json
{
  "username": "john",
  "encryptedCardPayload": "<BASE64_RSA_CIPHERTEXT>"
}
```

Expected response includes:

```json
{
  "status": "SUCCESS",
  "cardId": 1,
  "provider": "VTS",
  "token": "VISA-TOKEN-XXXXXXXX",
  "tokenStatus": "PENDING"
}
```

### Step 4 — Activate

```http
POST /api/issuance/tokens/{token}/activate
```

### Step 5 — Suspend

```http
POST /api/issuance/tokens/{token}/suspend
```

### Step 6 — Resume

```http
POST /api/issuance/tokens/{token}/resume
```

### Step 7 — Terminate

```http
POST /api/issuance/tokens/{token}/terminate
```

## 15. Security Considerations

Current concepts:

- RSA encryption for card payload transport.
- Masked PAN storage.
- CVV is not persisted.
- BCrypt wallet-PIN verification.
- Token rather than raw PAN used by payment flows.
- Lifecycle transition validation.

Production hardening still required:

- HTTPS everywhere.
- Secrets management and credential rotation.
- Strong authentication/authorization and RBAC.
- Rate limiting and account lockout.
- Provider request signing and certificate management.
- Idempotency for issuance and payment APIs.
- Audit logging for every token lifecycle event.
- Device binding and risk checks.
- PCI DSS and applicable network/tokenization compliance.
- Real VTS/MDES API integration and certification.

## 16. Future Digital Issuance Enhancements

- Dedicated issuer management module.
- Credential Manager.
- Device registration/binding.
- Token event/audit table.
- Risk and eligibility engine.
- Webhook/event-driven provider callbacks.
- TSP/TSM abstraction.
- Android wallet/HCE PoC.
- Redis/Kafka integration.
- Production VTS and MDES adapters.

## 17. Project Summary

The project now demonstrates the core Digital Issuance journey:

```text
Issuer/User
    -> Digital Card Issuance
    -> Eligibility & Validation
    -> PNO Routing
    -> VTS / MDES Token Provisioning
    -> Digital Card Record
    -> Token Lifecycle Management
    -> Wallet
    -> Tokenized Payment
```

This provides a practical foundation for expanding the existing Digital Wallet into a broader Digital Issuance and Tokenization PoC aligned with the target architecture.

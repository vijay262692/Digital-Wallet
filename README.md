# digital-wallet-demo (single-module)

Quick-start:

1. mvn spring-boot:run
2. GET http://localhost:8080/api/wallet/publicKey  -> returns Base64 RSA public key
3. Encrypt a plaintext like: PAN|MM/YY|CVV using that public key (RSA/ECB/PKCS1Padding, Base64 output)
4. POST encrypted Base64 string (raw body, text/plain) to:
   POST http://localhost:8080/api/wallet/addCard
5. Response: JSON with token and provider

Notes:
- This is a demo. Do NOT use this code in production without proper security review.

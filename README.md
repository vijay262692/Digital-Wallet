# digital-wallet-demo (single-module)


Spring Boot • RSA Encryption • Tokenization • Payments

A secure Digital Wallet built with Spring Boot that encrypts card data using RSA keys, tokenizes it via mock Visa (VTS) or Mastercard (MDES) services, and allows payments using stored tokens.


⚙️ Quick Setup
1️⃣ Clone the repo
git clone https://github.com/your-username/digital-wallet-demo.git
cd digital-wallet-demo

2️⃣ Build & Run
mvn clean install
mvn spring-boot:run


Runs on → http://localhost:8080

🧠 Core Features

✅ RSA encryption of card data (Forge.js + Java)
✅ Tokenization via mock VTS / MDES
✅ Add, view, and pay using tokenized cards
✅ Transaction history
✅ Simple user flow (admin & users)
✅ Modular services & repositories

🔗 API Endpoints
Action	Method	Endpoint
Get Public Key	GET	/api/wallet/publicKey
Add Card	POST	/api/wallet/addCard
View Cards	GET	/api/wallet/cards
Make Payment	POST	/api/wallet/pay
View Transactions	GET	/api/wallet/transactions

Sample /pay request:

{
  "token": "VISA-TOKEN-26eda081",
  "amount": "249.50",
  "merchant": "Amazon"
}

🖥️ Frontend Pages
Page	File	Description
Add Card	add-card.html	Encrypts card info using RSA
View Cards	view-cards.html	Displays masked cards
Make Payment	make-payment.html	Pay using stored token
Transactions	transactions.html	View transaction logs

👉 Place these in src/main/resources/static/ to serve via browser.

🔐 Encryption Flow
Frontend (Forge.js)
     ↓ RSA Encrypt
Backend (Spring Boot)
     ↓ Decrypt → Tokenize (VTS/MDES)
     ↓ Save Masked Card + Token


Example:

Input Card	Route	Token
5123…	MDES	MC-TOKEN-xxxxxx
4123…	VTS	VISA-TOKEN-xxxxxx
🧾 Example Response

Payment Response:

{
  "status": "SUCCESS",
  "message": "[VTS] Payment of ₹111.0 processed at Zomato via token VISA-TOKEN-26eda081",
  "merchant": "Zomato",
  "amount": 111.0
}

🧩 Tech Stack

Java 17, Spring Boot 2.7

RSA Encryption (Java Cipher + Forge.js)

HTML / JS Frontend

In-Memory Storage (Card + Transaction Repository)

Maven Build

💡 Future Enhancements

H2 / MySQL persistence

JWT authentication

Email notifications

Real Visa/MC sandbox integration

🧑‍💻 Author

BaluRaju P V
MIT License © 2025

Notes:
- This is a demo. Do NOT use this code in production without proper security review.

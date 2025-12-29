
🔐 Spring Boot Nonce + JWT + Signature Security (Fintech-Grade)

This project demonstrates a real-world, production-grade API security model used in fintech, wallet, and payment systems.

It combines:
	•	✅ JWT authentication (identity)
	•	✅ Session-based HMAC signature (request integrity)
	•	✅ Nonce with Redis (replay attack prevention)
	•	✅ Device binding
	•	✅ Centralized JSON error handling
	•	✅ Spring Security 6 / Spring Boot 3 compatible design

⸻

🧠 Why This Project Exists

JWT alone is not enough for secure payment or money-moving APIs.

Real systems require defense in depth:

Layer	Purpose
JWT	Who is calling
Session Key	Can this device sign requests
Nonce	Is this request new
Signature	Was the request tampered
Redis	Scale + TTL + replay tracking

This project implements all of the above correctly.

⸻

🏗️ Architecture Overview

Client (Mobile / Postman)
 ├─ Authorization: Bearer JWT
 ├─ X-Device-Id
 ├─ X-Nonce
 ├─ X-Timestamp
 ├─ X-Signature
 ↓
Spring Security Filter Chain
 ├─ JwtAuthenticationFilter      (identity)
 ├─ NonceSecurityFilter          (replay + integrity)
 ↓
Controllers
 ↓
Redis (nonce + session keys)


⸻

🔑 Security Flow (Step-by-Step)

1️⃣ Login (Bootstrap)

POST /auth/login
X-Device-Id: device-abc

Response:

{
  "accessToken": "JWT",
  "sessionKey": "temporary-session-key",
  "expiresIn": 900
}

	•	JWT → identity
	•	Session key → used for signing requests
	•	Session key stored in Redis (TTL)

⸻

2️⃣ Secure API Call

POST /api/payments/execute
Authorization: Bearer <JWT>
X-Device-Id: device-abc
X-Nonce: uuid
X-Timestamp: epoch_seconds
X-Signature: base64_hmac


⸻

3️⃣ Backend Validation Order
	1.	Validate JWT
	2.	Fetch session key from Redis
	3.	Validate timestamp drift
	4.	Check nonce replay (Redis)
	5.	Verify HMAC signature
	6.	Store nonce (TTL)
	7.	Execute business logic

⸻

✍️ Signature Payload Format

Canonical payload (must match exactly):

METHOD
PATH
TIMESTAMP
NONCE

Example:

POST
/api/payments/execute
1703801200
550e8400-e29b-41d4-a716-446655440000

Signature:

Base64( HMAC_SHA256(sessionKey, payload) )


⸻

🧰 Tech Stack
	•	Java 17+
	•	Spring Boot 3.x
	•	Spring Security 6
	•	Redis
	•	JWT (jjwt)
	•	Lombok

⸻

🚀 How to Run Locally

1️⃣ Start Redis

docker run -d --name redis -p 6379:6379 redis:7.2-alpine


⸻

2️⃣ Configure Application

spring:
  data:
    redis:
      host: localhost
      port: 6379


⸻

3️⃣ Run Application

./mvnw spring-boot:run


⸻

🧪 Testing with Postman

Login

POST http://localhost:8080/auth/login
X-Device-Id: device-abc

Save:
	•	accessToken
	•	sessionKey (for signing)

⸻

Generate Signature (POC Only)

POST /internal/signature/generate
Authorization: Bearer <JWT>


⸻

Secure API Call

POST /api/payments/execute
Authorization: Bearer <JWT>
X-Device-Id: device-abc
X-Nonce: uuid
X-Timestamp: epoch
X-Signature: signature


⸻

❌ Replay Attack Example

Reusing the same nonce:

{
  "code": "REPLAY_ATTACK",
  "message": "Replay attack detected",
  "timestamp": "2025-01-10T19:45:22Z",
  "path": "/api/payments/execute"
}


⸻

⚠️ Important Notes
	•	/internal/** endpoints are for POC/testing only
	•	Never expose signature-generation APIs in production
	•	Mobile apps must store session keys in:
	•	Android Keystore
	•	iOS Secure Enclave
	•	Session keys are short-lived
	•	JWT access tokens must expire

⸻

🔐 Error Handling

All errors return consistent JSON:

{
  "code": "INVALID_SIGNATURE",
  "message": "Signature verification failed",
  "timestamp": "2025-01-10T19:45:22Z",
  "path": "/api/payments/execute"
}

Handled via:
	•	Custom SecurityViolationException
	•	@RestControllerAdvice
	•	HandlerExceptionResolver for filters

⸻

🧱 Real-World Use Cases

This pattern is used in:
	•	Wallet backends
	•	Payment gateways
	•	Banking APIs
	•	Open Banking / PSD2
	•	Internal fintech microservices

⸻

🚀 Possible Enhancements
	•	Idempotency-Key for payments
	•	Request body hash in signature
	•	Refresh token flow
	•	Rate limiting with Redis
	•	Key rotation
	•	Device fingerprinting

⸻

📜 License

MIT (or update as needed)

⸻

🙌 Author

Built as a learning + reference implementation
for real-world API security, not tutorials.


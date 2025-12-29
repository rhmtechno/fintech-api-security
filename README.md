🔐 Spring Boot Nonce + JWT + Signature + Idempotency Security (Fintech-Grade)

This project demonstrates a real-world, production-grade API security model used in fintech, wallet, and payment systems.

It combines:
•	✅ JWT authentication (identity)
•	✅ Session-based HMAC signature (request integrity)
•	✅ Nonce with Redis (replay attack prevention)
•	✅ Idempotency with Redis (duplicate execution prevention)
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
Idempotency	Was this operation already executed
Redis	Scale, TTL, replay & idempotency tracking

This project implements all of the above correctly, following real fintech patterns.

⸻

🏗️ Architecture Overview

Client (Mobile / Postman)
├─ Authorization: Bearer JWT
├─ X-Device-Id
├─ X-Nonce
├─ X-Timestamp
├─ X-Signature
├─ Idempotency-Key
↓
Spring Security Filter Chain
├─ JwtAuthenticationFilter        (identity)
├─ NonceSecurityFilter            (replay + integrity)
├─ IdempotencyFilter              (duplicate execution prevention)
↓
Controllers
↓
Redis (session keys + nonces + idempotency keys)


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

2️⃣ Secure & Idempotent API Call

POST /api/payments/execute
Authorization: Bearer <JWT>
X-Device-Id: device-abc
X-Nonce: uuid
X-Timestamp: epoch_seconds
X-Signature: base64_hmac
Idempotency-Key: uuid


⸻

3️⃣ Backend Validation Order
1.	Validate JWT
2.	Fetch session key from Redis
3.	Validate timestamp drift
4.	Check nonce replay (Redis)
5.	Verify HMAC signature
6.	Check idempotency key
7.	Acquire idempotency lock
8.	Execute business logic
9.	Store final response for idempotency
10.	Store nonce (TTL)

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

🔁 Idempotency (Very Important for Payments)

Idempotency ensures:

Retrying the same request does NOT cause duplicate execution.

How it works
•	Client generates a unique Idempotency-Key per business operation
•	Backend stores execution state in Redis
•	Same key → same response
•	Concurrent requests → only one executes

Redis key format

idem:{userId}:{endpoint}:{idempotencyKey}

Redis values

Value	Meaning
IN_PROGRESS	Request is being processed
JSON response	Request already completed

TTL:
•	24 hours (configurable)

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
•	sessionKey

⸻

Generate Signature (POC Only)

POST /internal/signature/generate
Authorization: Bearer <JWT>


⸻

Secure & Idempotent API Call

POST /api/payments/execute
Authorization: Bearer <JWT>
X-Device-Id: device-abc
X-Nonce: uuid
X-Timestamp: epoch
X-Signature: signature
Idempotency-Key: uuid


⸻

❌ Replay & Duplicate Examples

Replay attack (same nonce)

{
"code": "REPLAY_ATTACK",
"message": "Replay attack detected",
"timestamp": "2025-01-10T19:45:22Z",
"path": "/api/payments/execute"
}

Duplicate execution attempt (same Idempotency-Key)

{
"code": "IDEMPOTENT_REQUEST_IN_PROGRESS",
"message": "Request is already being processed",
"timestamp": "2025-01-10T19:45:22Z",
"path": "/api/payments/execute"
}


⸻

⚠️ Important Notes
•	/internal/** endpoints are POC/testing only
•	Never expose signature-generation APIs in production
•	Mobile apps must store session keys in:
•	Android Keystore
•	iOS Secure Enclave
•	Session keys are short-lived
•	JWT access tokens must expire
•	Idempotency-Key must be reused on retries

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
•	SecurityViolationException
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
•	Request body hash in signature
•	Refresh token flow
•	Rate limiting with Redis
•	Key rotation
•	Device fingerprinting
•	Risk-based authentication

⸻

📜 License


⸻

🙌 Author

Built as a learning + reference implementation
for real-world API security, not tutorials.

# 🛡️ Security Policy & Architecture

The **Library Management System** project takes security, data integrity, and authentication seriously. This document details our security practices, vulnerability disclosure policy, and architectural safeguards.

---

## 📋 Supported Versions

Only the latest release version on the `main` branch is actively supported with security updates and patches.

| Version | Supported | Notes |
|---|---|---|
| 1.0.x | ✅ Yes | Current production release |
| < 1.0.0 | ❌ No | Legacy pre-release versions |

---

## 🚨 Reporting a Vulnerability

If you discover a security vulnerability or potential exposure within this repository:

1. **Do NOT open a public GitHub issue.**
2. Send an email directly to the maintainer:
   - **Contact**: `gargnikunj991@gmail.com` / GitHub: [@gargnikunj991-ux](https://github.com/gargnikunj991-ux)
   - Include a detailed description of the vulnerability, attack vector, steps to reproduce, and sample payload if applicable.
3. We will acknowledge receipt within 48 hours and work on a prompt remediation.

---

## 🔒 Security Best Practices Implemented

### 1. Stateless Authentication & JWT Security
- **No Server Session State**: All user authentication is stateless via `SessionCreationPolicy.STATELESS`.
- **HMAC-SHA256 Cryptographic Signatures**: Access tokens are signed using JJWT (`0.12.7`) with high-entropy 256-bit secret keys.
- **Short-lived Access Tokens**: JWT access tokens are short-lived, minimizing exposure if a token is intercepted.
- **Dynamic Key Injection**: Secrets are injected dynamically from environment variables (`@Value("${jwt.secret}")`) and never hardcoded in source code.

### 2. Secure Refresh Token Rotation & Revocation
- Refresh tokens are generated as high-entropy cryptographically secure UUID strings.
- Stored in the PostgreSQL `refresh_tokens` table with strict foreign key linkage to the `User`.
- Active refresh tokens can be immediately revoked on logout (`POST /auth/logout` sets `revoked = true`), neutralizing compromised tokens immediately.
- Automatic verification of expiration timestamps (`Instant.now().isAfter(token.getExpiryDate())`).

### 3. Role-Based Access Control (RBAC)
- Role definitions: `ADMIN`, `LIBRARIAN`, `ASSISTANT`.
- Administrative privileges (user registration, destructive deletions) are restricted strictly to `ADMIN` accounts.
- Modifying inventory/member records is restricted to `ADMIN` and `LIBRARIAN`.
- Standard read/borrow operations are permitted for all authenticated staff roles.

### 4. Password Security
- Passwords are never stored in plaintext.
- All credentials are encrypted using **BCrypt Password Hashing** (`BCryptPasswordEncoder` with standard salt rounds) before database persistence.

### 5. Sanitized Error Handling & Information Leakage Prevention
- `server.error.include-stacktrace=never` and `server.error.include-message=never` are enforced in `application.properties` to ensure raw JVM stack traces and database schema exceptions are never returned to external API consumers.
- Centralized `GlobalExceptionHandler` and Spring Security `AuthenticationEntryPoint`/`AccessDeniedHandler` return structured, sanitized JSON error responses.

### 6. SQL Injection & Parameter Tampering Protection
- All database operations are executed through **Spring Data JPA** and **Hibernate ORM**, using parameterized queries under the hood to completely prevent SQL injection attacks.
- Strict DTO request validation (`@Valid`, `@NotBlank`, `@Email`) ensures malformed or unauthorized fields cannot bypass API boundaries.

---

## ⚙️ Environment Variables & Secret Hygiene

1. **Never commit `.env` files**: The `.env` file is explicitly ignored in `.gitignore`.
2. **Use `.env.example`**: Always distribute template files without real passwords or production keys.
3. **Continuous Secret Scanning**: Maintain automated GitHub secret scanning to prevent accidental credential leakage.

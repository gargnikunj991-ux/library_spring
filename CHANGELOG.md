# 📜 Changelog

All notable changes to the **Library Management System** project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-08-12

### 🚀 Added
- **JWT Refresh Token Rotation & Session Management**:
  - Persisted `RefreshToken` entity in PostgreSQL (`refresh_tokens` table) with UUID tokens and expiry tracking.
  - `POST /auth/refresh` endpoint for zero-friction access token renewal.
  - `POST /auth/logout` endpoint with instantaneous token revocation (`revoked = true`).
- **Role-Based Access Control (RBAC)**:
  - Granular role hierarchy (`ADMIN`, `LIBRARIAN`, `ASSISTANT`).
  - Spring Security authorization rules restricting dangerous operations (book/member deletions) exclusively to `ADMIN`.
- **Environment & Secrets Hardening**:
  - Integrated `dotenv-java` for automatic `.env` variable loading.
  - Replaced hardcoded JWT secrets with dynamic `@Value("${jwt.secret}")` injection.
  - Parameterized database credentials (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`).
- **Custom Security Entry Points**:
  - `AuthenticationEntryPoint` returning standardized `401 Unauthorized` JSON.
  - `AccessDeniedHandler` returning standardized `403 Forbidden` JSON.
- **Enterprise Documentation Suite**:
  - Comprehensive `README.md`, `ARCHITECTURE.md`, `DATABASE.md`, `SETUP.md`, `TESTING.md`, `PROJECT_STATUS.md`, `CONTRIBUTING.md`, `SECURITY.md`, and `LICENSE`.

### 🛡️ Changed
- Refactored `JwtAuthenticationFilter` logging from `System.out.println` to standard `SLF4J` logger.
- Configured `JwtAuthenticationFilter` manual instantiation in `SecurityConfig` to eliminate redundant filter executions.
- Enforced `@Transactional` boundaries on all multi-entity write operations in `BorrowService`.
- Sanitized HTTP error responses with `server.error.include-stacktrace=never`.

---

## [0.3.0] - 2026-07-28

### 🚀 Added
- **Spring Security & Stateless JWT Integration**:
  - Stateless session creation policy (`SessionCreationPolicy.STATELESS`).
  - `CustomUserDetailsService` implementing Spring Security's `UserDetailsService`.
  - `User` entity mapped to `users` PostgreSQL table with BCrypt password hashing.
  - User registration endpoint `POST /auth/register` (admin-restricted).
  - User authentication endpoint `POST /auth/login` returning signed JWT access token.
- **Custom JWT Utilities**:
  - `JwtService` implementing HMAC-SHA256 signature verification, claims parsing, and expiration validation.

---

## [0.2.0] - 2026-07-25

### 🚀 Added
- **Borrowing & Return State Engine (`/api/borrow`)**:
  - `BorrowRecord` entity with `@ManyToOne` foreign key associations to `Book` and `Member`.
  - `POST /api/borrow` endpoint with automated 14-day due date calculation and availability state flip (`available = false`).
  - `POST /api/borrow/return/{borrowId}` endpoint resetting book availability (`available = true`) and stamping return date.
  - Domain validation throwing `BookUnavailableException` when attempting to borrow checked-out inventory.

---

## [0.1.0] - 2026-07-20

### 🚀 Added
- **Core Layered Architecture Foundation**:
  - Spring Boot 4.1.0 project initialized with Java 21, Spring Data JPA, and PostgreSQL driver.
  - Separation of layers: Controller → Service → Repository → Model.
- **Book Inventory Management (`/api/books`)**:
  - Full CRUD operations (`GET`, `POST`, `PUT`, `DELETE`).
  - DTO request/response mapping (`CreateBookRequest`, `BookResponse`).
- **Member Registry Management (`/api/members`)**:
  - Full CRUD operations (`GET`, `POST`, `PUT`, `DELETE`).
  - DTO request/response mapping (`CreateMemberRequest`, `MemberResponse`).
- **Validation & Exception Handling**:
  - Jakarta Validation constraints (`@NotBlank`, `@Email`, `@NotNull`).
  - Global exception advice (`GlobalExceptionHandler`) handling `BookNotFoundException`, `MemberNotFoundException`, and `MethodArgumentNotValidException`.

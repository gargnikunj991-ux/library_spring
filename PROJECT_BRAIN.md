# PROJECT_BRAIN.md -- Single Source of Truth

> This file is the single source of truth for the Library Management System project. It tracks the current state, technical decisions, roadmap, and project documentation index.

---

## 🎯 1. Project Vision & Goals

Build a production-quality **Library Management System** backend using **Spring Boot 4.1.0** and **Java 21**, following clean backend architecture standards.

The goal is to master Spring Boot concepts ground-up:
* Layered Architecture (Controller → Service → Repository → PostgreSQL)
* REST API standards & ResponseEntity design
* DTO separation & Request Validation
* Global Exception Handling (`@ControllerAdvice`)
* JPA Entity Relationships (`@ManyToOne`, `@JoinColumn`)
* Stateless JWT Security with HMAC-SHA256 & Refresh Token Rotation
* Database persistence with PostgreSQL
* Recruiter-ready, enterprise-grade documentation and repository structure

---

## 🏗️ 2. Current Architecture & Tech Stack

- **Java 21**
- **Spring Boot 4.1.0** (Spring Web MVC, Spring Data JPA, Spring Security, Jakarta Validation)
- **PostgreSQL 16+** (Database)
- **Maven** (Dependency & Build Management)
- **JJWT 0.12.7** (JWT generation and token parsing)
- **Dotenv Java 3.1.0** (Environment variables configuration)

---

## 📈 3. Current Progress & Status

### ✅ Completed Modules & Milestones (v1.0.0 Stable)

1. **Database & Infrastructure**:
   - [x] PostgreSQL connection (`jdbc:postgresql://localhost:5432/library`)
   - [x] Environment variables configuration (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `JWT_REFRESH_EXPIRATION_MS`) via `.env` and `.env.example`
   - [x] Hibernate DDL auto-update (`update`)
   - [x] Clean package structuring (`controller`, `service`, `repository`, `model`, `dto`, `exception`, `config`)

2. **Book Module** (`/api/books`):
   - [x] `Book` entity with `id` (`IDENTITY`), `title`, `author`, `available`
   - [x] `BookRepository` extending `JpaRepository`
   - [x] `BookService` with DTO mapping (`CreateBookRequest`, `BookResponse`)
   - [x] `BookController` with full CRUD (GET all, GET by ID, POST, PUT, DELETE)

3. **Member Module** (`/api/members`):
   - [x] `Member` entity with `memberId` (`IDENTITY`), `name`, `email`, `phoneNumber`
   - [x] `MemberRepository` extending `JpaRepository`
   - [x] `MemberService` with DTO mapping (`CreateMemberRequest`, `MemberResponse`)
   - [x] `MemberController` with full CRUD (GET all, GET by ID, POST, PUT, DELETE)

4. **Borrow & Return Module** (`/api/borrow`):
   - [x] `BorrowRecord` entity with `@ManyToOne` relationships (`Book`, `Member`), `borrowDate`, `dueDate`, `returnDate`, `returned`
   - [x] `BorrowRecordRepository` extending `JpaRepository`
   - [x] `BorrowService.borrowBook(...)`:
     - Member & Book lookup validation
     - Availability check (`book.isAvailable()`)
     - Automatic 14-day due date calculation
     - Sets `book.setAvailable(false)` and saves record
   - [x] `Borrowcontroller` REST endpoint `POST /api/borrow`
   - [x] `BorrowService.returnBook(...)` & `POST /api/borrow/return/{borrowId}`:
     - Record lookup validation (`BorrowRecordNotFoundException`)
     - Sets `returned=true`, `returnDate=now()`, and resets `book.setAvailable(true)`

5. **Validation & Exception Handling**:
   - [x] Input validation annotations (`@NotBlank`, `@Email`, `@NotNull`, `@Valid`)
   - [x] Custom exceptions: `BookNotFoundException`, `MemberNotFoundException`, `BookUnavailableException`, `BorrowRecordNotFoundException`, `TokenRefreshException`
   - [x] Centralized `@ControllerAdvice` in `GlobalExceptionHandler`
   - [x] Response sanitization with `server.error.include-stacktrace=never`

6. **Security, User Model & Refresh Token Rotation**:
   - [x] `spring-boot-starter-security` added to build dependencies
   - [x] `SecurityConfig.java` enforcing request authentication, disabling CSRF, and using stateless session management (`SessionCreationPolicy.STATELESS`)
   - [x] `JwtAuthenticationFilter` with SLF4J logging
   - [x] `User` entity with `username`, `password` (BCrypt), and `Role` (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
   - [x] `RefreshToken` entity mapped to `refresh_tokens` table with UUID tokens, expiry date, user linkage, and revocation flag
   - [x] `POST /auth/login`, `POST /auth/register`, `POST /auth/refresh`, and `POST /auth/logout` endpoints
   - [x] `AuthenticationEntryPoint` (401) and `AccessDeniedHandler` (403) custom JSON handlers

7. **Interactive OpenAPI 3.0 & Swagger UI Integration**:
   - [x] Added `springdoc-openapi-starter-webmvc-ui:2.5.0`
   - [x] Configured `OpenApiConfig.java` with metadata and JWT Bearer Security Scheme
   - [x] Configured `SecurityConfig.java` route access for `/swagger-ui/**`, `/v3/api-docs/**`, and `/swagger-ui.html`

8. **Comprehensive Automated Test Suite (28 Tests)**:
   - [x] Configured H2 in-memory test database in `src/test/resources/application.properties`
   - [x] `BorrowServiceTest`: Unit tests covering borrow, return, availability locks, and custom exceptions
   - [x] `RefreshTokenServiceTest`: Unit tests covering token creation, verification, revocation, and rotation
   - [x] `BookServiceTest`: Unit tests covering book CRUD operations and not found exceptions
   - [x] `MemberServiceTest`: Unit tests covering member registry and lookup validation
   - [x] `AuthServiceTest`: Unit tests covering registration, login, token generation, and logout

9. **Enterprise Documentation Suite & Repository Standardization**:
   - [x] `README.md` — Master recruiter-ready project overview
   - [x] `docs/API_DOCUMENTATION.md` — Complete REST API specification
   - [x] `docs/ARCHITECTURE.md` — Deep dive into layers, patterns & sequence flows
   - [x] `docs/DATABASE.md` — Database design, ERD diagrams & table schemas
   - [x] `docs/SETUP.md` — Step-by-step fresh machine setup & troubleshooting guide
   - [x] `docs/TESTING.md` — Comprehensive test matrices & cURL test suite
   - [x] `docs/PROJECT_STATUS.md` — Milestones, retrospective & lessons learned
   - [x] `docs/images/README.md` — Architecture diagrams & demo walkthrough assets
   - [x] `.env.example` — Environment template for local development
   - [x] `CHANGELOG.md` — Keep-a-Changelog semver history
   - [x] `LICENSE` — MIT License
   - [x] `CONTRIBUTING.md` — Contribution workflow & coding standards
   - [x] `SECURITY.md` — Security policy & vulnerability reporting

---

## 📑 4. Documentation Index

- 📘 [README.md](file:///D:/library/library/README.md) -- Master portfolio overview.
- 🔌 [docs/API_DOCUMENTATION.md](file:///D:/library/library/docs/API_DOCUMENTATION.md) -- Complete REST API reference.
- 🏛️ [docs/ARCHITECTURE.md](file:///D:/library/library/docs/ARCHITECTURE.md) -- Clean architecture, design patterns & sequence diagrams.
- 🗄️ [docs/DATABASE.md](file:///D:/library/library/docs/DATABASE.md) -- Relational ERD diagram and database schemas.
- 🚀 [docs/SETUP.md](file:///D:/library/library/docs/SETUP.md) -- Step-by-step setup guide.
- 🧪 [docs/TESTING.md](file:///D:/library/library/docs/TESTING.md) -- Testing strategy and test matrix.
- 📊 [docs/PROJECT_STATUS.md](file:///D:/library/library/docs/PROJECT_STATUS.md) -- Project status & retrospective.
- 📜 [CHANGELOG.md](file:///D:/library/library/CHANGELOG.md) -- Version history.
- 🤝 [CONTRIBUTING.md](file:///D:/library/library/CONTRIBUTING.md) -- Contribution guidelines.
- 🛡️ [SECURITY.md](file:///D:/library/library/SECURITY.md) -- Security policy.
- 📄 [LICENSE](file:///D:/library/library/LICENSE) -- MIT License.

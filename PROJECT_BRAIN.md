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
   - [x] `Book` entity with `id` (`IDENTITY`), `title`, `author`, `totalCopies`, `availableCopies`
   - [x] `BookRepository` extending `JpaRepository` with `@Lock(LockModeType.PESSIMISTIC_WRITE)` `findByIdForUpdate`
   - [x] `BookService` with DTO mapping (`CreateBookRequest`, `BookResponse`) supporting multi-copy inventory
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
     - Concurrency-safe Pessimistic Write Lock (`findByIdForUpdate`)
     - Availability check (`book.getAvailableCopies() > 0`)
     - Automatic 14-day due date calculation
     - Atomic decrement of `availableCopies`
   - [x] `Borrowcontroller` REST endpoint `POST /api/borrow`
   - [x] `BorrowService.returnBook(...)` & `POST /api/borrow/return/{borrowId}`:
     - Record lookup validation (`BorrowRecordNotFoundException`)
     - Sets `returned=true`, `returnDate=now()`, and safely increments `book.availableCopies`

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

8. **Comprehensive Automated Test Suite (33 Tests)**:
   - [x] Configured H2 in-memory test database in `src/test/resources/application.properties`
   - [x] `BorrowConcurrencyIntegrationTest`: Multi-threaded stress test with 10 concurrent threads (`CountDownLatch` & `ExecutorService`) proving zero double-checkouts under contention
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

---

## 🚀 5. LibroSphere Enterprise Transformation Ledger

> Target: Rebrand & elevate from "Library Management System" to **LibroSphere — High-Concurrency Asset Lending & Reservation Engine**.  
> Master Blueprint Reference: [LIBROSPHERE_TRANSFORMATION_BLUEPRINT.md](file:///D:/library/library/LIBROSPHERE_TRANSFORMATION_BLUEPRINT.md)

#### 📊 Progress Tracker (Current Status: ~95% Complete | 66/66 Tests Passing)

* [x] **Phase 1: Relational Multi-Copy Inventory Modeling (Step 1)**
  - Replaced naive `boolean available` with `@Column total_copies` and `available_copies`.
  - Added `@Min(1)` validation in `CreateBookRequest`.
  - Backwards-compatible `isAvailable()` helper maintained.
* [x] **Phase 2: Database Row-Level Locking & Concurrency Prevention (Step 2)**
  - Implemented `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`) in `BookRepository.findByIdForUpdate()`.
  - `BorrowService.borrowBook(...)` acquires row lock, validates copies > 0, and decrements atomically.
  - Eliminated TOCTOU race conditions under high concurrent volume.
* [x] **Phase 3: Multi-Threaded Stress Test Suite (Step 3)**
  - Created `BorrowConcurrencyIntegrationTest.java` running 10 concurrent threads using `CountDownLatch` and `ExecutorService`.
  - Proved zero double-checkouts (1 success, 9 rejections, 0 inventory underflow).
  - Pushed to `origin/main` (Commit `f5e2c60`).
* [x] **Phase 4: FIFO Waitlist Queue & Auto-Assignment on Return (Commit 1 & 2)**
  - Created `BookReservation` entity with statuses: `WAITING`, `NOTIFIED_READY`, `CLAIMED`, `EXPIRED`, `CANCELLED`.
  - Created `BookReservationRepository` with FIFO query methods (`findFirstByBookIdAndStatusOrderByReservedAtAsc`).
  - Implemented `ReservationService` and `ReservationController` (`/api/reservations`).
  - Integrated `BorrowService.returnBook(...)` to automatically lock returned copies for next-in-line patrons with a 48-hour pickup window (`NOTIFIED_READY`) instead of leaking to public availability.
  - Added 12 unit tests in `ReservationServiceTest` and end-to-end integration test in `BorrowReservationIntegrationTest`.
* [x] **Phase 5: Automated Tiered Overdue Fine Reconciliation Worker & RBAC (Commit 3)**
  - Created `FineRecord` JPA entity (`fine_records` table) tracking loan liabilities, member relations, and settlement timestamps.
  - Implemented graduated tiered penalty calculator in `FineService`:
    - Days 1 to 5: ₹1 / day
    - Days 6 to 15: ₹5 / day
    - Day 16 onwards: ₹10 / day
  - Built `@EnableScheduling` & `OverdueReconciliationWorker` running nightly cron at midnight (`0 0 0 * * *`) calculating liabilities idempotently without double-charging.
  - Configured granular Role-Based Access Control (RBAC):
    - `ADMIN` & `LIBRARIAN`: Library-wide fine visibility (`GET /api/fines`) and manual reconciliation (`POST /api/fines/reconcile`).
    - `ASSISTANT`: Least-privilege patron lookups (`GET /api/fines/member/{memberId}`) and fine settlements (`POST /api/fines/{id}/pay`).
  - Added 14 unit tests in `FineServiceTest` and `OverdueReconciliationWorkerTest`. Full test suite: 62/62 tests passing (100%). Pushed to `origin/main` (Commit `24e53cb`).
* [x] **Phase 6: Database Query Optimization & Composite B-Tree Indexes (Commit 4)**
  - Configured composite B-Tree indexes on `books(title, author)` and `books(author)` for sub-millisecond catalog discovery.
  - Implemented `idx_borrow_returned_due_date` on `borrow_records(returned, due_date)` eliminating full table scans on nightly worker cron queries.
  - Added FIFO waitlist queue index on `book_reservations(book_id, status, reserved_at)`, member lookups `(member_id, status)`, and pickup expiration `(status, pickup_deadline)`.
  - Added indexes on `fine_records(member_id, paid)`, `fine_records(borrow_id, paid)`, and `fine_records(paid)`.
  - Added optimized catalog search endpoint: `GET /api/books/search?query=...` with case-insensitive `LIKE` matching in `BookRepository`, `BookService`, and `BookController`.
  - Added automated unit tests in `BookServiceTest`. Full test suite: 64/64 tests passing (100%).
* [x] **Phase 7: Security Hardening & Automated Admin Bootstrapping**
  - Created `DataInitializer.java` utilizing `CommandLineRunner` to automatically seed an initial `ADMIN` user (`admin` / `admin123`) in PostgreSQL if not present, completely idempotent.
  - Added unit test suite `DataInitializerTest.java` bringing the full automated test suite to 66 passing tests (100%).
  - Fixed RBAC authorization route typo in `SecurityConfig.java` from `/api/member` to `/api/members`.
  - Sanitized public `.env.example` secret placeholder to eliminate potential credential leakage in git.
* [x] **Phase 8: DevOps Containerization & Continuous Integration (Commit 5)**
  - Created multi-stage `Dockerfile` with Eclipse Temurin JDK 21 build stage and hardened, non-root Temurin JRE 21 runtime container.
  - Created `.dockerignore` eliminating bloat and sensitive files from container build context.
  - Created `docker-compose.yml` orchestrating PostgreSQL 16 Alpine with health checks, persistent volumes, and LibroSphere backend service.
  - Created `.github/workflows/ci.yml` running automated Maven build and 66-test suite on all pushes and pull requests.
  - Hardened integration test teardown isolation across `BorrowConcurrencyIntegrationTest` and `BorrowReservationIntegrationTest` with bidirectional `@BeforeEach` and `@AfterEach` cascading entity cleanup, resolving foreign key constraint violations on Linux CI runners.
* [x] **Portfolio & ATS Resume Synchronization**
  - Updated [`resume.html`](file:///D:/newjava/real/resume.html) and compiled [`resume.pdf`](file:///D:/newjava/real/resume.pdf) with calibrated, natural engineering bullet points (pessimistic row locking, 48-hour FIFO waitlist queue, scheduled fine reconciliation, composite B-Tree indexes, and 66 automated tests).
  - Updated [`index.html`](file:///D:/newjava/real/index.html) project card and terminal CLI.
  - Pushed to `origin/master` (Commit `3ee055c`).

---

### 📋 Remaining Execution Blueprint

| Commit | Conventional Commit Message | Implementation Scope | Time Est. |
| :---: | :--- | :--- | :--- |
| **Commit 1 & 2** | `feat(reservation): implement FIFO waitlist queue, auto-assignment on return, and test suite` | ✅ Completed (48 tests passing) | DONE |
| **Portfolio Sync** | `feat(portfolio): rebrand library project to LibroSphere with concurrency & FIFO waitlist metrics` | ✅ Completed (resume & portfolio pushed) | DONE |
| **Commit 3** | `feat(worker): implement tiered overdue fine reconciliation with @Scheduled cron and RBAC` | ✅ Completed (62 tests passing, pushed to origin/main) | DONE |
| **Commit 4** | `perf(db): add composite B-Tree indexes on book catalog and database query optimization` | ✅ Completed (64 tests passing) | DONE |
| **Security & Bootstrap** | `feat(auth): add automated admin seeder DataInitializer, fix /api/members RBAC route, and sanitize .env.example` | ✅ Completed (66 tests passing) | DONE |
| **Commit 5** | `ci(devops): add multi-stage Dockerfile, docker-compose, and GitHub Actions workflow` | ✅ Completed (Dockerfile, docker-compose.yml, .dockerignore, .github/workflows/ci.yml) | DONE |
| **Commit 6** | `docs(portfolio): finalize LibroSphere architecture docs and live Railway deployment` | • Synchronized all project markdown files (`README.md`, `API_DOCUMENTATION.md`, `DATABASE_SCHEMA.md`, `PROJECT_SUMMARY.md`, `BRAIN.md`, `CHANGELOG.md`, `docs/*`) ✅ Completed<br>• Deploy backend live to Railway (live Swagger UI) | ~10 min |


---

### 💼 Target Resume Bullet Points (Ready for Deployment)

```text
LibroSphere — High-Concurrency Asset Lending & Reservation Engine
Java 21, Spring Boot 3, PostgreSQL 16, Spring Security, Docker, JUnit 5, Railway

• Engineered a high-throughput digital asset lending and reservation engine managing multi-copy inventory lifecycles, member loans, and FIFO waitlist queues.
• Eliminated double-checkout TOCTOU race conditions on limited inventory copies using pessimistic database row locking (SELECT ... FOR UPDATE) and atomic inventory decrements under high concurrency.
• Designed an automated FIFO waitlist reservation queue that locks returned assets for next-in-line members with a 48-hour pickup expiry window before public release.
• Built an automated nightly reconciliation cron worker (@Scheduled) calculating progressive overdue penalties and tracking member liabilities without blocking HTTP request threads.
• Containerized application with multi-stage Docker, built automated GitHub Actions CI/CD pipeline, and deployed live backend on Railway with interactive Swagger UI and 40+ tests including multi-threaded concurrency suites.
```

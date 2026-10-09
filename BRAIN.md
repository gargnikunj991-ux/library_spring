# 🧠 BRAIN.md -- Project Knowledge & State Index

> **Notice**: The authoritative, detailed Single Source of Truth for this project is maintained in [PROJECT_BRAIN.md](file:///D:/library/library/PROJECT_BRAIN.md). This file summarizes key learning milestones, architectural principles, and the current operational state.

---

## 🎯 1. Project Goal & Philosophy

Build a production-grade **High-Concurrency Asset Lending & Waitlist Reservation Engine (LibroSphere)** using **Java 21**, **Spring Boot 4.1.0**, and **PostgreSQL 16**.

The objective is to master real-world enterprise backend challenges:
* **Clean Layered Architecture**: Controller → Service → Repository → PostgreSQL.
* **Concurrency Control**: Eliminate TOCTOU race conditions via pessimistic row locking (`SELECT ... FOR UPDATE`).
* **Waitlist Queuing Engine**: FIFO queue management for out-of-stock items with automated 48-hour pickup allocation.
* **Scheduled Background Jobs**: Nightly `@Scheduled` cron worker calculating tiered overdue fines idempotently.
* **Query Optimization**: High-performance composite B-Tree indexes for sub-millisecond catalog lookups.
* **Stateless Security**: JWT authentication with cryptographic refresh token rotation and RBAC (`ADMIN`, `LIBRARIAN`, `ASSISTANT`).
* **DevOps**: Multi-stage Docker packaging, Docker Compose orchestration, and GitHub Actions CI.

---

## 🏗️ 2. Current Architecture

```
[ Client / Postman / Swagger UI ]
               ↓
[ Spring Security Filter Chain ] (Stateless JWT, BCrypt, 401/403 handlers)
               ↓
      [ Controller Layer ]       (AuthController, BookController, MemberController,
                                  Borrowcontroller, ReservationController, FineController)
               ↓
        [ Service Layer ]         (Transactions @Transactional, DTO Mapping, Business Invariants)
               ↓
      [ Repository Layer ]       (Spring Data JPA, Pessimistic Locking, Custom Queries)
               ↓
     [ PostgreSQL Database ]     (7 Tables: users, refresh_tokens, books, members,
                                  borrow_records, book_reservations, fine_records)
```

**Background Async Workers**:
```
[ OverdueReconciliationWorker ] (@Scheduled nightly cron at 00:00) ──► [ FineService ] ──► PostgreSQL
```

---

## 📈 3. Completed Modules & Implementation Status

### ✅ All 8 Enterprise Phases Complete (66/66 Automated Tests Passing)

1. **Database & Infrastructure**:
   - [x] PostgreSQL 16 connection with dynamic environment configuration (`.env` via `dotenv-java`).
   - [x] Multi-stage `Dockerfile` and `docker-compose.yml`.
   - [x] Automated GitHub Actions CI pipeline (`.github/workflows/ci.yml`).
   - [x] Automated admin bootstrapping on startup via `DataInitializer` (`admin` / `admin123`).

2. **Book Catalog & Concurrency Engine** (`/api/books`):
   - [x] Multi-copy inventory modeling (`totalCopies`, `availableCopies`).
   - [x] Pessimistic Write Lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) in `BookRepository.findByIdForUpdate`.
   - [x] Sub-millisecond catalog search endpoint `GET /api/books/search?query=...`.
   - [x] Composite B-Tree indexes (`idx_books_title_author`, `idx_books_author`).

3. **Member Registry** (`/api/members`):
   - [x] Full CRUD with validation (`@NotBlank`, `@Email`).
   - [x] Isolated DTO contracts (`CreateMemberRequest`, `MemberResponse`).

4. **Borrow & Return Engine** (`/api/borrow`):
   - [x] Concurrency-safe checkout with atomic inventory decrement.
   - [x] 14-day automatic due date calculation.
   - [x] Return processing with automated FIFO waitlist detection: held copy locked directly for next waiting member rather than leaked to public stock.

5. **FIFO Waitlist Reservation Queue** (`/api/reservations`):
   - [x] `BookReservation` entity with statuses: `WAITING`, `NOTIFIED_READY`, `CLAIMED`, `EXPIRED`, `CANCELLED`.
   - [x] FIFO priority sorting by `reservedAt ASC`.
   - [x] 48-hour exclusive pickup window (`NOTIFIED_READY`).
   - [x] Automatic reallocation to next patron on cancellation.

6. **Tiered Overdue Fine Reconciliation** (`/api/fines`):
   - [x] `FineRecord` entity tracking member liabilities and payment status.
   - [x] Graduated tiered formula: Days 1–5 (₹1/day), Days 6–15 (₹5/day), Day 16+ (₹10/day).
   - [x] Nightly midnight cron worker (`OverdueReconciliationWorker`).
   - [x] Idempotent calculations (no double-charging regardless of restarts).
   - [x] Granular RBAC (`ADMIN` ledger audit & manual reconcile, `LIBRARIAN` & `ASSISTANT` patron payment settlement).

7. **Security, User Model & Refresh Token Rotation** (`/auth`):
   - [x] Stateless Spring Security (`SessionCreationPolicy.STATELESS`).
   - [x] JJWT 0.12.7 access tokens + persisted UUID refresh tokens with rotation and revocation.
   - [x] Role hierarchy: `ADMIN`, `LIBRARIAN`, `ASSISTANT`.

8. **Automated Testing Suite**:
   - [x] 66 automated tests across unit, integration, multi-threaded stress concurrency, and worker suites.
   - [x] `BorrowConcurrencyIntegrationTest`: 10 concurrent threads validating zero double-checkouts under high contention.

---

## 📂 4. Current Package Structure

```
com.nikunj.library
├── LibraryApplication.java           # Entry Point (@EnableScheduling)
├── config/                           # SecurityConfig, JwtAuthFilter, OpenApiConfig, DataInitializer
├── controller/                       # Auth, Book, Member, Borrow, Reservation, Fine Controllers
├── service/                          # AuthService, BookService, MemberService, BorrowService,
│                                     # ReservationService, FineService, RefreshTokenService, JwtService
├── repository/                       # 7 Spring Data JPA Repositories
├── model/                            # 7 JPA Entities (Book, Member, BorrowRecord, User,
│                                     # RefreshToken, BookReservation, FineRecord)
├── dto/                              # Request & Response DTOs
├── exception/                        # Custom Exceptions & GlobalExceptionHandler
└── worker/                           # OverdueReconciliationWorker (@Scheduled cron)
```

---

## 📑 5. Key Documentation Links

- 📘 [PROJECT_SUMMARY.md](file:///D:/library/library/PROJECT_SUMMARY.md) — Comprehensive technical overview.
- 🧠 [PROJECT_BRAIN.md](file:///D:/library/library/PROJECT_BRAIN.md) — Authoritative transformation ledger & milestones.
- 🔌 [API_DOCUMENTATION.md](file:///D:/library/library/API_DOCUMENTATION.md) — Complete REST API reference.
- 🗄️ [DATABASE_SCHEMA.md](file:///D:/library/library/DATABASE_SCHEMA.md) — Relational schema & composite indexes.

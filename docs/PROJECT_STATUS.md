# 📊 Project Status & Retrospective -- LibroSphere

> **Status**: ✅ **Complete / Stable (v1.1.0)**  
> **Maintainer**: Nikunj Garg ([@gargnikunj991-ux](https://github.com/gargnikunj991-ux))  
> **Last Updated**: October 2026

---

## 🎯 Executive Summary

The **Library Management System (LibroSphere)** backend has evolved into an enterprise-grade digital asset lending and waitlist reservation engine. The project demonstrates clean backend architecture, concurrency-safe inventory locking, automated FIFO waitlist queues, scheduled background fine reconciliation, composite B-Tree query optimizations, and containerized DevOps with Java 21, Spring Boot 4.1.0, and PostgreSQL 16.

---

## ✅ Completed Milestones & Capabilities

### 1. Architectural Foundation & Data Layer
- [x] Clean Layered Architecture (`Controller` → `Service` → `Repository` → `PostgreSQL`).
- [x] Decoupled DTO contracts with strict validation; internal entities are never exposed across HTTP boundaries.
- [x] PostgreSQL relational schema with 7 production tables (`users`, `refresh_tokens`, `books`, `members`, `borrow_records`, `book_reservations`, `fine_records`).
- [x] Composite B-Tree database indexes for sub-millisecond query optimization.
- [x] Automated schema synchronization (`ddl-auto=update`).

### 2. Authentication & Authorization Security
- [x] Stateless Spring Security filter chain with `SessionCreationPolicy.STATELESS`.
- [x] Custom `JwtAuthenticationFilter` with SLF4J logging and HMAC-SHA256 signature verification.
- [x] Dynamic environment configuration (`.env`) loaded via `dotenv-java`.
- [x] Cryptographic refresh token rotation with persisted UUID tokens and instant revocation (`POST /auth/logout`).
- [x] Role-Based Access Control (`ADMIN`, `LIBRARIAN`, `ASSISTANT`) guarding sensitive actions.
- [x] Automated initial `ADMIN` user seeder on startup via `DataInitializer` (`admin` / `admin123`).
- [x] Standardized security error dispatching (`AuthenticationEntryPoint` -> `401 Unauthorized`, `AccessDeniedHandler` -> `403 Forbidden`).

### 3. High-Concurrency Asset Lending & Inventory
- [x] Multi-copy relational inventory tracking (`totalCopies`, `availableCopies`).
- [x] Concurrency safety guaranteed via `@Lock(LockModeType.PESSIMISTIC_WRITE)` (`SELECT ... FOR UPDATE`), eliminating TOCTOU race conditions.
- [x] Multi-threaded concurrency stress test (`BorrowConcurrencyIntegrationTest`) proving zero double-checkouts under high contention.
- [x] Automated 14-day checkout duration calculation.

### 4. FIFO Waitlist & Reservation Engine
- [x] `BookReservation` entity managing statuses: `WAITING`, `NOTIFIED_READY`, `CLAIMED`, `EXPIRED`, `CANCELLED`.
- [x] FIFO priority dispatching ordered by `reservedAt ASC`.
- [x] Return workflow auto-locks available copies exclusively for next waiting patrons with a 48-hour pickup window (`NOTIFIED_READY`) instead of releasing to public stock.
- [x] Automatic reallocation on patron cancellation.

### 5. Automated Tiered Overdue Fine Worker
- [x] `FineRecord` entity tracking member liabilities and payment statuses.
- [x] Tiered overdue penalty formula (₹1/day for days 1–5, ₹5/day for days 6–15, ₹10/day for day 16+).
- [x] Background cron scheduler (`OverdueReconciliationWorker`) running nightly at midnight (`0 0 0 * * *`) via Spring `@Scheduled`.
- [x] Idempotent liability calculation preventing duplicate charges across restarts.
- [x] Granular RBAC supporting library-wide audits, member liability queries, and payments.

### 6. Interactive OpenAPI & DevOps
- [x] SpringDoc OpenAPI 3.0 / Swagger UI integrated at `/swagger-ui/index.html` with JWT Bearer authorization support.
- [x] Multi-stage `Dockerfile` with Eclipse Temurin JRE 21.
- [x] `docker-compose.yml` for multi-container orchestration with PostgreSQL 16 Alpine.
- [x] GitHub Actions automated CI workflow (`.github/workflows/ci.yml`) executing all 66 tests on pushes and PRs.

---

## ⚠️ Known Limitations & Scope Boundaries

While fully functional and robust, future iterations can address the following areas:

1. **Pagination & Sorting**: Book and member listings return full lists. For catalogs with 100k+ records, Spring Data `Pageable` is recommended.
2. **External Email / SMS Notifications**: Patrons currently query their reservation status or fines via REST APIs. In the future, this can be paired with an asynchronous notification bus (Spring ApplicationEvents / Kafka / JavaMailSender) to send emails when a book becomes ready for pickup.
3. **Cloud Production Deployment**: Can be deployed live to managed cloud providers such as Railway or AWS ECS.

---

## 💡 Key Engineering Takeaways & Lessons Learned

1. **Pessimistic Locking Eliminates TOCTOU Races**: Applying database-level row locking (`SELECT ... FOR UPDATE`) guarantees serialization of checkout attempts at the database engine level, preventing inventory underflow when multiple threads contend for the last copy.
2. **Reservation Invariants Prevent Inventory Leaks**: When returning a book, verifying the waitlist queue before incrementing `availableCopies` prevents the item from leaking to walk-in patrons while a reserved patron has priority.
3. **Idempotent Background Jobs Guarantee Consistency**: Calculating liabilities deterministically from `dueDate` to current date ensures the nightly cron worker can restart or run repeatedly without generating double fines.

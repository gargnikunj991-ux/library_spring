# 🚀 Project Summary -- Library Management System

This document provides a comprehensive overview of the **Library Management System** project built with **Spring Boot** and **Java 21**. It serves as an architectural index for developers and AI agents to quickly understand the project structure, design patterns, domain logic, and coding standards without rescanning the entire repository.

---

## 🧭 Project Architecture Overview

The application follows standard **Clean Layered Backend Architecture**:

```
[ HTTP Client / Postman ]
        │
        ▼
   Controller Layer      ── (HTTP Handling, REST Endpoints, DTO Request Validation)
        │
        ▼
    Service Layer         ── (Business Logic, DTO Mapping, Entity State Management)
        │
        ▼
   Repository Layer       ── (Spring Data JPA Repositories, Database Access)
        │
        ▼
   PostgreSQL Database    ── (Relational Database Storage)
```

Centralized Exception Handling is managed across all controllers using `@ControllerAdvice`:
```
   Exceptions thrown in Service / Controller
        │
        ▼
   GlobalExceptionHandler  ── (Catches custom & validation exceptions, returns formatted ResponseEntity)
```

---

## 🛠️ Technology Stack & Dependencies

- **Language**: Java 21
- **Framework**: Spring Boot 4.1.0
- **Web**: Spring MVC (`spring-boot-starter-webmvc`)
- **Security**: Spring Security (`spring-boot-starter-security`)
- **API Documentation**: SpringDoc OpenAPI 3.0 / Swagger UI (`springdoc-openapi-starter-webmvc-ui:2.5.0`)
- **Environment**: Dotenv Java (`dotenv-java`)
- **Data Persistence**: Spring Data JPA (`spring-boot-starter-data-jpa`), Hibernate
- **Database**: PostgreSQL (Driver: `org.postgresql.Driver`), H2 In-Memory (Test scope)
- **Validation**: Jakarta Validation (`spring-boot-starter-validation`)
- **Testing**: JUnit 5, Mockito (62 automated Unit, Concurrency, Waitlist Integration, and Worker Tests)
- **Build Tool**: Maven

---

## 📂 Package Directory & File Map

Base package: `com.nikunj.library`

```
com.nikunj.library
├── LibraryApplication.java           # Main Spring Boot Application Entry Point (Loads .env properties via Dotenv, @EnableScheduling)
├── config/                           # Security & Application Configuration
│   ├── OpenApiConfig.java            # Swagger 3.0 OpenAPI metadata & JWT Bearer Security Scheme configuration
│   ├── SecurityConfig.java           # Spring Security filter chain setup (stateless JWT, 401 AuthenticationEntryPoint, 403 AccessDeniedHandler, RBAC)
│   └── JwtAuthenticationFilter.java  # JWT token validation filter (OncePerRequestFilter, no @Component)
├── controller/                       # REST Controller Layer
│   ├── AuthController.java           # REST Endpoints for /auth (login & register integration)
│   ├── BookController.java           # REST Endpoints for /api/books
│   ├── MemberController.java         # REST Endpoints for /api/members
│   ├── Borrowcontroller.java         # REST Endpoints for /api/borrow (POST /api/borrow & POST /api/borrow/return/{borrowId})
│   ├── FineController.java           # REST Endpoints for /api/fines (RBAC audit, member lookup, fine payments)
│   └── ReservationController.java    # REST Endpoints for /api/reservations (join waitlist, cancel, queue queries)
├── service/                          # Business Logic & Service Layer
│   ├── AuthService.java              # User registration logic with PasswordEncoder & login support
│   ├── BookService.java              # Book CRUD logic & DTO mapping
│   ├── BorrowService.java            # Book borrowing & return transaction logic (with auto-reservation allocation)
│   ├── CustomUserDetailsService.java# Spring Security UserDetailsService implementation
│   ├── FineService.java              # Tiered overdue calculation, idempotent reconciliation, and payment settlement
│   ├── JwtService.java               # JWT token generation and validation service
│   ├── MemberService.java            # Member CRUD logic & DTO mapping
│   ├── RefreshTokenService.java      # Refresh token creation, expiry verification, and rotation logic
│   └── ReservationService.java       # FIFO waitlist queueing, 48h pickup deadline, and reallocation logic
├── worker/                           # Background Cron Schedulers
│   └── OverdueReconciliationWorker.java # Nightly midnight @Scheduled cron auditing overdue loans and accruing fines
├── repository/                       # Data Access Layer (Spring Data JPA)
│   ├── BookRepository.java           # JpaRepository<Book, Long> with pessimistic write lock (findByIdForUpdate)
│   ├── BookReservationRepository.java# JpaRepository<BookReservation, Long> with FIFO query methods
│   ├── BorrowRecordRepository.java   # JpaRepository<BorrowRecord, Long> with unreturned overdue lookups
│   ├── FineRecordRepository.java     # JpaRepository<FineRecord, Long> with member & loan lookups
│   ├── MemberRepository.java         # JpaRepository<Member, Long>
│   ├── RefreshTokenRepository.java   # JpaRepository<RefreshToken, Long>
│   └── UserRepository.java           # JpaRepository<User, Long>
├── model/                            # JPA Database Entities
│   ├── Book.java                     # "books" table entity (totalCopies, availableCopies)
│   ├── BookReservation.java          # "book_reservations" table entity (FIFO waitlist queue, pickupDeadline)
│   ├── BorrowRecord.java             # "borrow_records" table entity with Foreign Keys
│   ├── FineRecord.java               # "fine_records" table entity tracking accrued liabilities & payments
│   ├── Member.java                   # "members" table entity
│   ├── RefreshToken.java             # "refresh_tokens" table entity for JWT refresh token rotation (@ManyToOne User relation)
│   ├── ReservationStatus.java        # Enum (WAITING, NOTIFIED_READY, CLAIMED, EXPIRED, CANCELLED)
│   └── User.java                     # "users" table entity for authentication (Role: ADMIN, LIBRARIAN, ASSISTANT)
├── dto/                              # Data Transfer Objects (API Contracts)
│   ├── BookResponse.java             # Outbound DTO for Book responses (totalCopies, availableCopies, available)
│   ├── BorrowResponse.java           # Outbound DTO for borrow transactions
│   ├── CreateBookRequest.java        # Inbound DTO for creating/updating Books (totalCopies validation)
│   ├── CreateBorrowRequest.java      # Inbound DTO for borrowing a book
│   ├── CreateMemberRequest.java      # Inbound DTO for creating/updating Members
│   ├── CreateReservationRequest.java # Inbound DTO for placing a reservation
│   ├── FineResponse.java             # Outbound DTO exposing fine details, book title, and payment status
│   ├── LoginRequest.java             # Inbound DTO for authentication
│   ├── LoginResponse.java            # Outbound DTO containing accessToken, refreshToken, tokenType
│   ├── MemberResponse.java           # Outbound DTO for Member responses
│   ├── RefreshTokenRequest.java      # Inbound DTO for token renewal (/auth/refresh)
│   ├── RegisterRequest.java          # Inbound DTO for user registration
│   └── ReservationResponse.java      # Outbound DTO for reservation status & pickup deadlines
└── exception/                        # Custom Exceptions & Global Handler
    ├── BookNotFoundException.java    # Thrown when Book ID is not found (HTTP 404)
    ├── BookUnavailableException.java # Thrown when Book is already borrowed (HTTP 404)
    ├── BorrowRecordNotFoundException.java # Thrown when Borrow Record ID is not found (HTTP 404)
    ├── DuplicateReservationException.java# Thrown when Member already has active reservation (HTTP 409)
    ├── FineNotFoundException.java    # Thrown when Fine Record ID is not found (HTTP 404)
    ├── MemberNotFoundException.java  # Thrown when Member ID is not found (HTTP 404)
    ├── ReservationNotFoundException.java # Thrown when Reservation ID is not found (HTTP 404)
    ├── TokenRefreshException.java    # Thrown when Refresh Token is expired or invalid (HTTP 401)
    └── GlobalExceptionHandler.java   # Centralized @ControllerAdvice handling all exceptions
```

---

## 📌 Core Domain Rules & Business Logic

1. **Book Inventory & Concurrency-Safe Borrowing (`POST /api/borrow`)**:
   - Every `Book` maintains `totalCopies` and `availableCopies`.
   - When a book is added, `availableCopies` defaults to `totalCopies` (at least 1).
   - When a book is borrowed via `POST /api/borrow`, `Borrowcontroller` delegates to `BorrowService.borrowBook()`.
   - The transaction acquires a **Pessimistic Write Lock (`SELECT ... FOR UPDATE`)** via `BookRepository.findByIdForUpdate(bookId)` to eliminate TOCTOU race conditions.
   - If a member has an active `NOTIFIED_READY` reservation within its 48-hour pickup window, the reservation is transitioned to `CLAIMED` and loan created without double-decrementing inventory.
   - Otherwise, the system verifies `book.getAvailableCopies() > 0`. If copies are exhausted, `BookUnavailableException` is thrown.
   - Upon successful borrow, `availableCopies` is decremented atomically by 1, and a `BorrowRecord` is created with `borrowDate` (today) and `dueDate` (today + 14 days).

2. **Book Return & FIFO Waitlist Auto-Assignment (`POST /api/borrow/return/{borrowId}`)**:
   - When a book is returned via `POST /api/borrow/return/{borrowId}`, `BorrowService.returnBook()` finds the record.
   - If missing, `BorrowRecordNotFoundException` is thrown.
   - Sets `returned = true` and `returnDate = LocalDate.now()`.
   - Locks the associated `Book` entity with `findByIdForUpdate`.
   - Checks if there is a `WAITING` reservation in the FIFO queue via `BookReservationRepository.findFirstByBookIdAndStatusOrderByReservedAtAsc`.
   - **If a reservation is found**: Sets its status to `NOTIFIED_READY` and `pickupDeadline = LocalDateTime.now().plusHours(48)`. Public `availableCopies` is **not** incremented, holding the physical copy exclusively for that member!
   - **If no reservation is waiting**: Safely increments `availableCopies` by 1 (capped at `totalCopies`).

3. **FIFO Waitlist Queue Engine (`/api/reservations`)**:
   - Members can queue for out-of-stock books via `POST /api/reservations`.
   - Enforces `book.availableCopies == 0` (patrons must borrow directly if copies exist).
   - Rejects duplicate active reservations for the same member & book (`409 Conflict`).
   - If a patron cancels a `NOTIFIED_READY` reservation, the held copy is automatically reallocated to the next waiting patron, or restored to general inventory if the queue is exhausted.

4. **Security, User Registration & Refresh Token Rotation**:
   - `SecurityConfig` configures `SecurityFilterChain` to disable CSRF, enforce stateless session management (`SessionCreationPolicy.STATELESS`), and configure explicit exception handling:
     - `AuthenticationEntryPoint`: Returns `401 Unauthorized` for missing/invalid JWT tokens.
     - `AccessDeniedHandler`: Returns `403 Forbidden` for insufficient roles/permissions.
   - The `JwtAuthenticationFilter` is manually instantiated inside `securityFilterChain()` (not registered as a `@Component`) to prevent double filter registration.
   - `User` entity maps to the `users` table with fields `id`, `username` (unique, non-null), `password`, and `role` (`EnumType.STRING` with roles `ADMIN`, `LIBRARIAN`, `ASSISTANT`).
   - `AuthService.registerUser(RegisterRequest request)` handles user registration with BCrypt hashing.
   - `AuthService.loginUser(LoginRequest request)` authenticates credentials and returns both an access token (JWT) and a persisted refresh token (UUID).
   - `RefreshTokenService` validates expiration and issues new access tokens on `POST /auth/refresh`. If token expired or revoked, `TokenRefreshException` is thrown.
   - `POST /auth/logout` requires authentication and a valid role (`ADMIN`, `LIBRARIAN`, `ASSISTANT`), delegating to `RefreshTokenService.revokeByUsername()` to set `revoked = true` in PostgreSQL.

5. **Tiered Overdue Fine Reconciliation & Least-Privilege RBAC (`/api/fines`)**:
   - `OverdueReconciliationWorker` runs nightly at midnight (`@Scheduled(cron = "0 0 0 * * *")`).
   - Reconciles unreturned loans where `dueDate < LocalDate.now()` using a **graduated tiered penalty**:
     - **Days 1 to 5**: ₹1 / day (Max ₹5)
     - **Days 6 to 15**: ₹5 / day (Max ₹50; cumulative ₹55)
     - **Day 16 onwards**: ₹10 / day
   - **Idempotency**: Calculates total days overdue from `dueDate` to current date and updates or creates the `FineRecord` deterministically. Never duplicates charges regardless of worker restart or re-runs.
   - **Role-Based Access Control (RBAC)**:
     - `GET /api/fines`: Library-wide ledger overview restricted to `ADMIN` and `LIBRARIAN`.
     - `GET /api/fines/member/{memberId}`: Least-privilege patron lookups permitted for `ADMIN`, `LIBRARIAN`, and `ASSISTANT`.
     - `POST /api/fines/{fineId}/pay`: Fine settlements permitted for `ADMIN`, `LIBRARIAN`, and `ASSISTANT`.
     - `POST /api/fines/reconcile`: Manual on-demand worker trigger restricted to `ADMIN`.

6. **DTO Isolation**:
   - Entities (`Book`, `BookReservation`, `FineRecord`, `Member`, `BorrowRecord`, `User`) are **never** exposed directly to API callers.
   - Controllers accept `@Valid` Request DTOs and return Response DTOs inside `ResponseEntity`.
   - Services perform mapping between Entities and DTOs.

7. **Validation Rules**:
   - `CreateBookRequest`: `title` (not blank), `author` (not blank), `totalCopies` (min 1).
   - `CreateMemberRequest`: `name` (not blank), `email` (not blank, valid email format), `phoneNumber` (not blank).
   - `CreateBorrowRequest`: `bookId` (not null), `memberId` (not null).
   - `CreateReservationRequest`: `bookId` (not null), `memberId` (not null).

---

## ⚙️ Configuration Summary (`application.properties`)

- **Database URL**: `${DB_URL:jdbc:postgresql://localhost:5432/library}` (Supports environment variable override)
- **Database Credentials**: Username: `${DB_USERNAME:postgres}`, Password: `${DB_PASSWORD:postgres}` (Safe for public GitHub repositories)
- **DDL Auto**: `update` (Hibernate automatically creates/updates database tables on startup)
- **SQL Logging**: `spring.jpa.show-sql=true`, `spring.jpa.properties.hibernate.format_sql=true`
- **Dialect**: `org.hibernate.dialect.PostgreSQLDialect`
- **JWT Secret**: `${JWT_SECRET:...}` (Injected dynamically via `@Value("${jwt.secret}")` in `JwtService`)
- **Error Stack Trace**: `server.error.include-stacktrace=never` (Prevents exposing Java stack traces in HTTP responses)


---

## 📜 Development & Coding Guidelines (`AGENTS.md`)

- **Layer Boundaries**: Never bypass the Service layer. Controllers only handle HTTP; Services handle business logic; Repositories handle persistence.
- **Dependency Injection**: Use Constructor Injection exclusively across all controllers, services, and components (zero `@Autowired` field injection).
- **Exception Handling**: Always throw specific runtime exceptions (`BookNotFoundException`, `MemberNotFoundException`, `BookUnavailableException`) instead of returning null or generic errors.
- **Before Editing Code Rule**: Read relevant files → Explain problem → Suggest solution → Wait for approval before modifying files.
- **Documentation Maintenance Rule**: Read project `.md` files when changes are requested, and update them at the end of each session.

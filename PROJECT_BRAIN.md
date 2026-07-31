# PROJECT_BRAIN.md -- Single Source of Truth

> This file is the single source of truth for the Library Management System project. It tracks the current state, technical decisions, roadmap, and pending tasks.

---

## 🎯 1. Project Vision & Goals

Build a production-quality **Library Management System** backend using **Spring Boot 4.1.0** and **Java 21**, following clean backend architecture standards.

The goal is to master Spring Boot concepts ground-up:
* Layered Architecture (Controller → Service → Repository)
* REST API standards & ResponseEntity design
* DTO separation & Request Validation
* Global Exception Handling (`@ControllerAdvice`)
* JPA Entity Relationships (`@ManyToOne`, `@JoinColumn`)
* Database persistence with PostgreSQL

---

## 🏗️ 2. Current Architecture & Tech Stack

- **Java 21**
- **Spring Boot 4.1.0** (Spring Web MVC, Spring Data JPA, Jakarta Validation)
- **PostgreSQL** (Database)
- **Maven** (Dependency & Build Management)

---

## 📈 3. Current Progress & Status

### ✅ Completed Setup & Core Modules

1. **Database & Infrastructure**:
   - [x] PostgreSQL connection (`jdbc:postgresql://localhost:5432/library`)
   - [x] Environment variables configuration (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD` with default fallbacks) for GitHub security
   - [x] Hibernate DDL auto-update (`update`)
   - [x] Package structuring (`controller`, `service`, `repository`, `model`, `dto`, `exception`)

2. **Book Module** (`/api/books`):
   - [x] `Book` entity with `id` (`IDENTITY`), `title`, `author`, `available`
   - [x] `BookRepository` extending `JpaRepository`
   - [x] `BookService` with DTO mapping (`CreateBookRequest`, `BookResponse`)
   - [x] `BookController` with full CRUD:
     - `GET /api/books`
     - `GET /api/books/{id}`
     - `POST /api/books`
     - `PUT /api/books/{id}`
     - `DELETE /api/books/{id}`

3. **Member Module** (`/api/members`):
   - [x] `Member` entity with `memberId` (`IDENTITY`), `name`, `email`, `phoneNumber`
   - [x] `MemberRepository` extending `JpaRepository`
   - [x] `MemberService` with DTO mapping (`CreateMemberRequest`, `MemberResponse`)
   - [x] `MemberController` with full CRUD:
     - `GET /api/members`
     - `GET /api/members/{memberId}`
     - `POST /api/members`
     - `PUT /api/members/{memberId}`
     - `DELETE /api/members/{memberId}`

4. **Borrow & Return Module** (`/api/borrow`):
   - [x] `BorrowRecord` entity with `@ManyToOne` relationships (`Book`, `Member`), `borrowDate`, `dueDate`, `returnDate`, `returned`
   - [x] `BorrowRecordRepository` extending `JpaRepository`
   - [x] `BorrowService.borrowBook(...)`:
     - Member & Book lookup validation
     - Availability check (`book.isAvailable()`)
     - Automatic 14-day due date calculation
     - Sets `book.setAvailable(false)` and saves record
   - [x] `Borrowcontroller` REST endpoint `POST /api/borrow` calling `BorrowService.borrowBook` returning `ResponseEntity<BorrowResponse>`.
   - [x] `BorrowService.returnBook(...)` & `POST /api/borrow/return/{borrowId}`:
     - Record lookup validation (`BorrowRecordNotFoundException`)
     - Sets `returned=true`, `returnDate=now()`, and resets `book.setAvailable(true)`

5. **Validation & Exception Handling**:
   - [x] Input validation annotations (`@NotBlank`, `@Email`, `@NotNull`, `@Valid`)
   - [x] Custom exceptions: `BookNotFoundException`, `MemberNotFoundException`, `BookUnavailableException`, `BorrowRecordNotFoundException`
   - [x] Centralized `@ControllerAdvice` in `GlobalExceptionHandler`

6. **Security & User Model**:
   - [x] `spring-boot-starter-security` added to build dependencies
   - [x] `SecurityConfig.java` enforcing request authentication, disabling CSRF, and using stateless session management (`SessionCreationPolicy.STATELESS`)
   - [x] `JwtAuthenticationFilter` (no `@Component`) manually instantiated inside `SecurityConfig.securityFilterChain()` to prevent double filter registration
   - [x] `User` entity mapped to `users` database table with `username`, `password`, and `Role` (`ADMIN`, `LIBRARIAN`, `ASSISTANT`) with setters (`setUsername`, `setPassword`, `setRole`)
   - [x] `AuthService.registerUser(RegisterRequest request)` implemented with `PasswordEncoder` hashing and database persistence via `UserRepository`
   - [x] `CustomUserDetailsService.loadUserByUsername()` implemented — queries `UserRepository.findByUsername()` and throws `UsernameNotFoundException` if user not found
   - [x] JWT login endpoint (`POST /auth/login`) fully functional — authenticates via `AuthenticationManager`, generates JWT token via `JwtService`

---

## 📑 4. Quick Documentation Index

- 📘 [PROJECT_SUMMARY.md](file:///D:/library/library/PROJECT_SUMMARY.md) -- Architecture, package breakdown, technology stack, and domain rules.
- 🔌 [API_DOCUMENTATION.md](file:///D:/library/library/API_DOCUMENTATION.md) -- Complete REST API reference, request/response DTO schemas, and error codes.
- 🗄️ [DATABASE_SCHEMA.md](file:///D:/library/library/DATABASE_SCHEMA.md) -- Relational ERD diagram, table definitions, columns, and foreign keys.
- 📋 [AGENTS.md](file:///D:/library/library/AGENTS.md) -- Coding standards, approval requirements, and documentation maintenance rules for AI agents.

---

## 🚀 5. Next Steps & Roadmap

1. **Advanced Book & Member Operations**:
   - Search books by title / author / category.
   - Pagination and Sorting support (`Pageable`).

2. **Advanced Authentication & JWT Authorization**:
   - JWT token authentication filter (validate token on protected endpoints).
   - Fine-grained role-based access control (Librarian vs Member).



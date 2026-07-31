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
- **Data Persistence**: Spring Data JPA (`spring-boot-starter-data-jpa`), Hibernate
- **Database**: PostgreSQL (Driver: `org.postgresql.Driver`)
- **Validation**: Jakarta Validation (`spring-boot-starter-validation`)
- **Build Tool**: Maven

---

## 📂 Package Directory & File Map

Base package: `com.nikunj.library`

```
com.nikunj.library
├── LibraryApplication.java           # Main Spring Boot Application Entry Point
├── config/                           # Security & Application Configuration
│   ├── SecurityConfig.java           # Spring Security filter chain setup (stateless JWT, manual filter registration)
│   └── JwtAuthenticationFilter.java  # JWT token validation filter (OncePerRequestFilter, no @Component)
├── controller/                       # REST Controller Layer
│   ├── AuthController.java           # REST Endpoints for /auth (login & register integration)
│   ├── BookController.java           # REST Endpoints for /api/books
│   ├── MemberController.java         # REST Endpoints for /api/members
│   └── Borrowcontroller.java         # REST Endpoints for /api/borrow (POST /api/borrow & POST /api/borrow/return/{borrowId})
├── service/                          # Business Logic & Service Layer
│   ├── AuthService.java              # User registration logic with PasswordEncoder & login support
│   ├── BookService.java              # Book CRUD logic & DTO mapping
│   ├── BorrowService.java            # Book borrowing & return transaction logic
│   ├── CustomUserDetailsService.java# Spring Security UserDetailsService implementation
│   ├── JwtService.java               # JWT token generation and validation service
│   └── MemberService.java            # Member CRUD logic & DTO mapping
├── repository/                       # Data Access Layer (Spring Data JPA)
│   ├── BookRepository.java           # JpaRepository<Book, Long>
│   ├── BorrowRecordRepository.java   # JpaRepository<BorrowRecord, Long>
│   ├── MemberRepository.java         # JpaRepository<Member, Long>
│   └── UserRepository.java           # JpaRepository<User, Long>
├── model/                            # JPA Database Entities
│   ├── Book.java                     # "books" table entity
│   ├── Member.java                   # "members" table entity
│   ├── BorrowRecord.java             # "borrow_records" table entity with Foreign Keys
│   └── User.java                     # "users" table entity for authentication (Role: ADMIN, LIBRARIAN, ASSISTANT)
├── dto/                              # Data Transfer Objects (API Contracts)
│   ├── BookResponse.java             # Outbound DTO for Book responses
│   ├── BorrowResponse.java           # Outbound DTO for borrow transactions
│   ├── CreateBookRequest.java        # Inbound DTO for creating/updating Books
│   ├── CreateBorrowRequest.java      # Inbound DTO for borrowing a book
│   ├── CreateMemberRequest.java      # Inbound DTO for creating/updating Members
│   ├── LoginRequest.java             # Inbound DTO for authentication
│   ├── LoginResponse.java            # Outbound DTO containing JWT token
│   ├── MemberResponse.java           # Outbound DTO for Member responses
│   └── RegisterRequest.java          # Inbound DTO for user registration
└── exception/                        # Custom Exceptions & Global Handler
    ├── BookNotFoundException.java    # Thrown when Book ID is not found (HTTP 404)
    ├── MemberNotFoundException.java  # Thrown when Member ID is not found (HTTP 404)
    ├── BookUnavailableException.java# Thrown when Book is already borrowed (HTTP 404)
    ├── BorrowRecordNotFoundException.java # Thrown when Borrow Record ID is not found (HTTP 404)
    └── GlobalExceptionHandler.java   # Centralized @ControllerAdvice handling all exceptions
```

---

## 📌 Core Domain Rules & Business Logic

1. **Book Availability & Borrowing (`POST /api/borrow`)**:
   - Every `Book` has an `available` boolean flag.
   - When a book is added, `available` defaults to `true` (unless set in request).
   - When a book is borrowed via `POST /api/borrow`, `Borrowcontroller` delegates to `BorrowService.borrowBook()`.
   - The system verifies `book.isAvailable()`. If false, `BookUnavailableException` is thrown.
   - Upon successful borrow, `book.setAvailable(false)` is saved, and a `BorrowRecord` is created with a `borrowDate` (today) and `dueDate` (today + 14 days).

2. **Book Return (`POST /api/borrow/return/{borrowId}`)**:
   - When a book is returned via `POST /api/borrow/return/{borrowId}`, `BorrowService.returnBook()` finds the record.
   - If missing, `BorrowRecordNotFoundException` is thrown.
   - Sets `returned = true` and `returnDate = LocalDate.now()`.
   - Resets the associated `Book` entity's `available` flag to `true` (`book.setAvailable(true)`).

3. **Security & User Registration**:
   - `SecurityConfig` configures `SecurityFilterChain` to disable CSRF, enforce stateless session management (`SessionCreationPolicy.STATELESS`), and enforce authentication on incoming endpoints (`.anyRequest().authenticated()`). The `JwtAuthenticationFilter` is manually instantiated inside `securityFilterChain()` (not registered as a `@Component`) to prevent double filter registration.
   - `User` entity maps to the `users` table with fields `id`, `username` (unique, non-null), `password`, and `role` (`EnumType.STRING` with roles `ADMIN`, `LIBRARIAN`, `ASSISTANT`).
   - `AuthService.registerUser(RegisterRequest request)` handles user registration:
     - Instantiates a new `User` entity.
     - Sets username and role from `RegisterRequest`.
     - Encodes the raw password using `PasswordEncoder.encode(...)` before storing.
     - Saves user to PostgreSQL via `UserRepository.save(...)`.

4. **DTO Isolation**:
   - Entities (`Book`, `Member`, `BorrowRecord`, `User`) are **never** exposed directly to API callers.
   - Controllers accept `@Valid` Request DTOs and return Response DTOs inside `ResponseEntity`.
   - Services perform mapping between Entities and DTOs.

5. **Validation Rules**:
   - `CreateBookRequest`: `title` (not blank), `author` (not blank).
   - `CreateMemberRequest`: `name` (not blank), `email` (not blank, valid email format), `phoneNumber` (not blank).
   - `CreateBorrowRequest`: `bookId` (not null), `memberId` (not null).

---

## ⚙️ Configuration Summary (`application.properties`)

- **Database URL**: `${DB_URL:jdbc:postgresql://localhost:5432/library}` (Supports environment variable override)
- **Database Credentials**: Username: `${DB_USERNAME:postgres}`, Password: `${DB_PASSWORD:postgres}` (Safe for public GitHub repositories)
- **DDL Auto**: `update` (Hibernate automatically creates/updates database tables on startup)
- **SQL Logging**: `spring.jpa.show-sql=true`, `spring.jpa.properties.hibernate.format_sql=true`
- **Dialect**: `org.hibernate.dialect.PostgreSQLDialect`


---

## 📜 Development & Coding Guidelines (`AGENTS.md`)

- **Layer Boundaries**: Never bypass the Service layer. Controllers only handle HTTP; Services handle business logic; Repositories handle persistence.
- **Dependency Injection**: Use `@Autowired` or Constructor Injection.
- **Exception Handling**: Always throw specific runtime exceptions (`BookNotFoundException`, `MemberNotFoundException`, `BookUnavailableException`) instead of returning null or generic errors.
- **Before Editing Code Rule**: Read relevant files → Explain problem → Suggest solution → Wait for approval before modifying files.
- **Documentation Maintenance Rule**: Read project `.md` files when changes are requested, and update them at the end of each session.

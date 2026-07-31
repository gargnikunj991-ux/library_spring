# 📚 Library Management System Backend

> A production-grade RESTful Library Management System backend built with **Java 21**, **Spring Boot 4.1.0**, **Spring Data JPA**, **Spring Security with JWT**, and **PostgreSQL**.

---

## 🎯 Overview

The Library Management System provides a clean, layered backend architecture (`Controller` → `Service` → `Repository`) for managing books, library members, user registration/authentication, borrowing, and returning books.

Key Highlights:
- **Clean Architecture & Isolation**: Entities are never exposed directly; all API communication uses validated DTOs.
- **Security & Stateless JWT Auth**: Protected REST endpoints using Spring Security and stateless JWT authentication filter.
- **Atomic Transactions**: Book borrowing and returning workflows managed with `@Transactional` guarantees.
- **Robust Exception Handling**: Global exception handling via `@ControllerAdvice` and sanitized HTTP error responses (`server.error.include-stacktrace=never`).

---

## 🛠️ Tech Stack & Dependencies

| Layer / Component | Technology |
|---|---|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.1.0 |
| **Security** | Spring Security, JJWT (`io.jsonwebtoken` 0.12.7) |
| **ORM / Data Persistence** | Spring Data JPA, Hibernate ORM |
| **Database** | PostgreSQL |
| **Validation** | Jakarta Validation (`spring-boot-starter-validation`) |
| **Build Tool** | Maven |
| **Logging** | SLF4J / Logback |

---

## 📂 Project Structure

```
com.nikunj.library
├── LibraryApplication.java           # Main Spring Boot Application Entry Point
├── config/                           # Security & Application Configuration
│   ├── SecurityConfig.java           # Spring Security filter chain setup
│   └── JwtAuthenticationFilter.java  # Custom JWT filter (OncePerRequestFilter with SLF4J logging)
├── controller/                       # REST Controller Layer
│   ├── AuthController.java           # Endpoints for /auth/register & /auth/login
│   ├── BookController.java           # Endpoints for /api/books
│   ├── MemberController.java         # Endpoints for /api/members
│   └── Borrowcontroller.java         # Endpoints for /api/borrow
├── service/                          # Business Logic & Service Layer
│   ├── AuthService.java              # Registration logic with PasswordEncoder & login support
│   ├── BookService.java              # Book CRUD logic & DTO mapping
│   ├── BorrowService.java            # Borrow & Return transactional workflows (@Transactional)
│   ├── CustomUserDetailsService.java# UserDetailsService implementation for Spring Security
│   ├── JwtService.java               # JWT generation & validation (@Value("${jwt.secret}"))
│   └── MemberService.java            # Member CRUD logic & DTO mapping
├── repository/                       # Data Access Layer (Spring Data JPA)
│   ├── BookRepository.java           # JpaRepository<Book, Long>
│   ├── BorrowRecordRepository.java   # JpaRepository<BorrowRecord, Long>
│   ├── MemberRepository.java         # JpaRepository<Member, Long>
│   └── UserRepository.java           # JpaRepository<User, Long>
├── model/                            # JPA Database Entities
│   ├── Book.java                     # "books" table entity
│   ├── Member.java                   # "members" table entity
│   ├── BorrowRecord.java             # "borrow_records" table entity
│   └── User.java                     # "users" table entity (ADMIN, LIBRARIAN, ASSISTANT)
├── dto/                              # Data Transfer Objects
│   ├── BookResponse.java
│   ├── BorrowResponse.java
│   ├── CreateBookRequest.java
│   ├── CreateBorrowRequest.java
│   ├── CreateMemberRequest.java
│   ├── LoginRequest.java
│   ├── LoginResponse.java
│   ├── MemberResponse.java
│   └── RegisterRequest.java
└── exception/                        # Custom Exception Handling
    ├── BookNotFoundException.java
    ├── BookUnavailableException.java
    ├── BorrowRecordNotFoundException.java
    ├── MemberNotFoundException.java
    └── GlobalExceptionHandler.java   # Centralized @ControllerAdvice
```

---

## 🗄️ Database Schema

The database uses 4 relational tables in PostgreSQL:

1. **`users`**: Authentication credentials (`username`, BCrypt-hashed `password`, `role`).
2. **`members`**: Library members (`member_id`, `name`, `email`, `phone_number`).
3. **`books`**: Book inventory (`id`, `title`, `author`, `available`).
4. **`borrow_records`**: Tracking borrowing transactions (`borrow_id`, FK `book_id`, FK `member_id`, `borrow_date`, `due_date`, `return_date`, `returned`).

---

## 🔌 REST API Summary

### 🔑 Authentication Endpoints
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/auth/register` | Register a new user | ❌ No |
| `POST` | `/auth/login` | Authenticate user & receive JWT token | ❌ No |

### 📚 Book Endpoints
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/books` | Get list of all books | ✅ Yes |
| `GET` | `/api/books/{id}` | Get book by ID | ✅ Yes |
| `POST` | `/api/books` | Add a new book | ✅ Yes |
| `PUT` | `/api/books/{id}` | Update book details | ✅ Yes |
| `DELETE` | `/api/books/{id}` | Delete a book | ✅ Yes |

### 👤 Member Endpoints
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `GET` | `/api/members` | Get list of all members | ✅ Yes |
| `GET` | `/api/members/{memberId}` | Get member by ID | ✅ Yes |
| `POST` | `/api/members` | Add a new member | ✅ Yes |
| `PUT` | `/api/members/{memberId}` | Update member details | ✅ Yes |
| `DELETE` | `/api/members/{memberId}` | Delete a member | ✅ Yes |

### 📖 Borrow & Return Endpoints
| Method | Endpoint | Description | Auth Required |
|---|---|---|---|
| `POST` | `/api/borrow` | Borrow an available book | ✅ Yes |
| `POST` | `/api/borrow/return/{borrowId}` | Return a borrowed book | ✅ Yes |

---

## ⚙️ How to Setup & Run

### 1. Prerequisites
- Java 21 SDK
- PostgreSQL database server running on `localhost:5432`

### 2. Configure Environment / `application.properties`
Update database credentials in `src/main/resources/application.properties` or set environment variables:
```properties
spring.datasource.url=${DB_URL:jdbc:postgresql://localhost:5432/library}
spring.datasource.username=${DB_USERNAME:postgres}
spring.datasource.password=${DB_PASSWORD:postgres}
jwt.secret=${JWT_SECRET:5F05gEkmG5Gxi6GHqehXUFlsusNdoO0tXnwuK1iUVpQ=}
```

### 3. Build & Run
```bash
# Compile and test
mvn clean test-compile

# Run application
mvn spring-boot:run
```
The server will start on `http://localhost:8080`.

---

## 🚀 Future Roadmap
- [ ] Add pagination and sorting support (`Pageable`) for Book and Member APIs.
- [ ] Fine-grained role-based endpoint authorization (`hasRole('ADMIN')`).
- [ ] Swagger / OpenAPI 3.0 documentation integration.
- [ ] Docker & Docker Compose setup for database and application containerization.

---

## 👤 Author
**Nikunj Garg**  
GitHub: [gargnikunj991-ux](https://github.com/gargnikunj991-ux)

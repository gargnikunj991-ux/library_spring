# 🧠 BRAIN.md

# Library Management System (Spring Boot)

## Project Goal

Build a production-style Library Management System while learning Spring Boot from the ground up.

The objective is **not just to complete the project**, but to understand:

* Spring Boot
* REST APIs
* JPA & Hibernate
* PostgreSQL
* Clean Architecture
* Professional Backend Development

---

# Current Architecture

```
Client
    ↓
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

---

# Current Progress

## Project Setup

* [x] Spring Boot Project Created
* [x] PostgreSQL Connected
* [x] Hibernate Configured
* [x] Automatic Table Creation
* [x] Project Connected Successfully

---

## Book Module

### Entity

* [x] Book Entity
* [x] JPA Annotations
* [x] ID Generation using IDENTITY

### Repository

* [x] BookRepository
* [x] JpaRepository Integration

### Service

* [x] BookService Created
* [x] Business Logic Moved From Controller

### Controller

Implemented APIs

* [x] GET /api/books
* [x] GET /api/books/{id}
* [x] POST /api/books
* [x] DELETE /api/books/{id}

---

# Concepts Learned

## Spring Boot

* Spring Initializr
* Dependency Injection
* @Autowired
* @Service
* @RestController
* @RequestMapping
* @GetMapping
* @PostMapping
* @DeleteMapping
* @PathVariable
* @RequestBody

---

## JPA / Hibernate

* @Entity
* @Table
* @Id
* @GeneratedValue
* JpaRepository
* findAll()
* findById()
* save()
* deleteById()

---

## Backend Concepts

* REST API
* CRUD Operations
* JSON Request
* JSON Response
* HTTP Methods
* Controller Layer
* Service Layer
* Repository Layer
* Business Logic
* Dependency Injection
* Optional
* Auto Increment IDs

---

# Current Folder Structure

```
config/
    SecurityConfig

controller/
    BookController
    MemberController
    Borrowcontroller

service/
    BookService
    MemberService
    BorrowService

repository/
    BookRepository
    MemberRepository
    BorrowRecordRepository

model/
    Book
    Member
    BorrowRecord
    User

dto/
    CreateBookRequest
    BookResponse
    CreateMemberRequest
    MemberResponse
    CreateBorrowRequest
    BorrowResponse

exception/
    BookNotFoundException
    MemberNotFoundException
    BookUnavailableException
    BorrowRecordNotFoundException
    GlobalExceptionHandler
```

---

# Completed Features

## Book Module

* [x] PUT /api/books/{id}
* [x] GET /api/books
* [x] GET /api/books/{id}
* [x] POST /api/books
* [x] DELETE /api/books/{id}
* [x] Validation
* [x] Exception Handling
* [x] DTO Isolation

---

## Member Module

* [x] Member Entity
* [x] Repository
* [x] Service
* [x] Controller
* [x] CRUD APIs

---

## Borrow Module

* [x] Borrow Book (`POST /api/borrow`)
* [x] Return Book (`POST /api/borrow/return/{borrowId}`)
* [x] Due Date Calculation (14 Days)
* [x] Book Availability State Synchronization

---

## Security & Validation & Exception Handling

* [x] Spring Security filter chain (`SecurityConfig`)
* [x] User Entity (`users` table) with Role enum
* [x] `@Valid`, `@NotBlank`, `@Email`, `@NotNull`
* [x] `@ControllerAdvice` in `GlobalExceptionHandler`
* [x] Custom exceptions

---

# Future Features

## Authentication & Security

* [ ] JWT Authentication
* [ ] Password Encryption (BCrypt)
* [ ] Fine-grained Role-Based Access Control (Admin / Librarian / Member)

---

## Database & Querying

* [ ] Pagination & Sorting (`Pageable`)
* [ ] Book Search API (by title, author, category)
* [ ] `@ManyToMany` / `@OneToMany` cascade mappings

---

## Documentation

* [ ] Swagger / OpenAPI Integration

---

## Testing & Deployment

* [ ] Unit Testing & Integration Testing
* [ ] Dockerization
* [ ] Production Deployment (Railway / Render)


---

# Learning Rules

* Never copy code without understanding it.
* Learn one concept before moving to the next.
* Always ask "Why?" before "How?"
* Build first, then optimize.
* Every feature should teach a Spring Boot concept.

---

# Coding Rules

* Controller only handles HTTP requests.
* Service contains business logic.
* Repository only accesses the database.
* Keep methods small and readable.
* Follow Single Responsibility Principle.

---

# Milestones

## Phase 1 (Current)

Learn Spring Boot Fundamentals.

Status:

🟢 In Progress

---

## Phase 2

Professional Backend Architecture.

Status:

⚪ Not Started

---

## Phase 3

Authentication & Security.

Status:

⚪ Not Started

---

## Phase 4

Production Ready Backend.

Status:

⚪ Not Started

---

# Long-Term Goal

Build a backend project that demonstrates professional Spring Boot development and provides a strong portfolio project for internships and software engineering interviews.

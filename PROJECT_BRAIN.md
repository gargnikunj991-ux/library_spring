# PROJECT_BRAIN.md

# Library Management System -- Project Brain

> This file is the single source of truth for the project. It tracks the
> current state, important decisions, roadmap, and future direction.

------------------------------------------------------------------------

# 1. Project Vision

## Goal

Build a professional Library Management System backend using **Spring
Boot** that demonstrates industry-standard backend architecture and is
suitable for internships and backend developer interviews.

The project is intentionally being built to **learn backend
engineering**, not just to complete CRUD features.

------------------------------------------------------------------------

# 2. Primary Users

### Current User

**Librarian (Admin)**

Responsibilities: - Add books - Update books - Delete books - Search
books - Register members - Manage borrow records - Return books

### Future User

**Library Member**

Future features: - Search books - View available books - Borrow books -
Return books - Borrow history

------------------------------------------------------------------------

# 3. Technology Stack

Backend - Java - Spring Boot - Spring MVC - Spring Data JPA -
Hibernate - PostgreSQL - Maven

Future - Spring Security - JWT - Swagger/OpenAPI - Docker -
Railway/Render

------------------------------------------------------------------------

# 4. Architecture

Request Flow

Client

↓

Controller

↓

Validation

↓

Service

↓

Repository

↓

PostgreSQL

Exception Flow

Exception

↓

@ControllerAdvice

↓

ResponseEntity

↓

Client

------------------------------------------------------------------------

# 5. Current Progress

## Completed

### Foundation

-   Spring Boot Project
-   PostgreSQL Configuration
-   Hibernate
-   JPA
-   Entity
-   Repository
-   Controller
-   Service

### CRUD

-   Create Book
-   Get All Books
-   Get Book By ID
-   Update Book
-   Delete Book

### Validation

-   @NotBlank
-   @Valid
-   Custom validation messages

### Exception Handling

-   BookNotFoundException
-   @ControllerAdvice
-   @ExceptionHandler
-   MethodArgumentNotValidException
-   Custom validation response
-   400 Bad Request
-   404 Not Found

------------------------------------------------------------------------

# 6. Current Sprint

## Sprint

DTO (Data Transfer Object)

Current Goal

Separate Entity from API communication.

Learn: - Request DTO - Response DTO - Why entities should not be exposed
directly

------------------------------------------------------------------------

# 7. Planned Features

## Book Module

-   ISBN
-   Category
-   Publication Year
-   Search
-   Pagination
-   Sorting

## Member Module

-   Register Member
-   Update Member
-   Delete Member
-   Search Member

## Borrow Module

-   Borrow Book
-   Return Book
-   Borrow History
-   Due Date

## Authentication

-   Register
-   Login
-   BCrypt
-   JWT
-   Roles

------------------------------------------------------------------------

# 8. Important Design Decisions

-   Use Controller → Service → Repository architecture.
-   Business logic belongs in Service.
-   Repository only communicates with the database.
-   Validation should happen before Service.
-   Use @ControllerAdvice for centralized exception handling.
-   Never use Optional.get() when absence is expected.
-   Prefer orElseThrow() with custom exceptions.
-   Future APIs should use DTOs instead of exposing entities.

------------------------------------------------------------------------

# 9. Lessons Learned

-   Difference between 400, 404 and 500 HTTP status codes.
-   Why Service layer exists.
-   Why Validation belongs before business logic.
-   Why custom exceptions improve API design.
-   Why ResponseEntity gives full control over HTTP responses.
-   Why Spring automatically routes exceptions to @ControllerAdvice.

------------------------------------------------------------------------

# 10. Future Improvements

-   DTO Mapping
-   Entity Relationships
-   Authentication
-   Authorization
-   Swagger
-   Logging
-   Testing
-   Docker
-   Deployment
-   API Documentation

------------------------------------------------------------------------

# 11. Project Goal

The finished project should demonstrate:

-   Professional Spring Boot architecture
-   Clean code
-   REST API best practices
-   Validation
-   Exception handling
-   Authentication
-   Database relationships
-   Production-ready backend concepts

It should be strong enough to confidently explain during technical
interviews and include in a professional portfolio.

# 📊 Project Status & Retrospective

> **Status**: ✅ **Complete / Stable (v1.0.0)**  
> **Maintainer**: Nikunj Garg ([@gargnikunj991-ux](https://github.com/gargnikunj991-ux))  
> **Last Updated**: August 2026

---

## 🎯 Executive Summary

The **Library Management System** backend has achieved all target engineering objectives for its initial **v1.0.0 production milestone**. The project is designed as an enterprise-grade reference architecture demonstrating clean backend layering, stateless JWT authentication, cryptographic refresh token rotation, transaction management, and relational data modeling with Spring Boot 4.1.0, Java 21, and PostgreSQL.

---

## ✅ Completed Milestones & Capabilities

### 1. Architectural Foundation & Data Layer
- [x] Clean Layered Architecture (`Controller` → `Service` → `Repository` → `PostgreSQL`).
- [x] Strict DTO decoupling; database entities are never exposed across HTTP boundaries.
- [x] PostgreSQL relational schema with 5 core tables (`users`, `refresh_tokens`, `books`, `members`, `borrow_records`).
- [x] Hibernate `@ManyToOne` foreign key mappings and automated schema synchronization (`ddl-auto=update`).

### 2. Authentication & Authorization Security
- [x] Stateless Spring Security filter chain with `SessionCreationPolicy.STATELESS`.
- [x] Custom `JwtAuthenticationFilter` with SLF4J logging and HMAC-SHA256 signature verification.
- [x] Dynamic `@Value("${jwt.secret}")` configuration backed by `.env` loading.
- [x] Refresh token rotation with persisted UUID tokens, expiration timestamps, and instant logout revocation (`POST /auth/logout`).
- [x] Role-Based Access Control (`ADMIN`, `LIBRARIAN`, `ASSISTANT`) guarding administrative deletions and modifications.
- [x] Custom security error dispatching (`AuthenticationEntryPoint` -> `401 Unauthorized`, `AccessDeniedHandler` -> `403 Forbidden`).

### 3. Business Modules
- [x] **Book Management**: Full REST CRUD with validation and availability flags.
- [x] **Member Registry**: Full REST CRUD with email validation.
- [x] **Borrowing & Return State Engine**:
  - Availability validation throwing `BookUnavailableException` if already checked out.
  - Automated 14-day due date computation.
  - `@Transactional` multi-table state updates on checkout and return.

### 4. Resiliency & Error Handling
- [x] Centralized `@ControllerAdvice` handling custom domain exceptions and validation errors.
- [x] Sanitized client responses with `server.error.include-stacktrace=never`.

---

## ⚠️ Known Limitations & Scope Boundaries

While fully functional and feature-complete for its v1.0.0 design, the current iteration intentionally scoped out the following elements:

1. **Pagination & Sorting**: Book and member listings currently return full result sets. For production catalogs with 100k+ records, Spring Data `Pageable` is recommended.
2. **Automated Overdue Fine Calculation**: Overdue calculations are performed by querying records rather than an automated daily scheduled background cron job.
3. **External Email Notifications**: Due date reminder notifications are currently surfaced via API queries rather than an asynchronous SMTP / Kafka email queue.
4. **OpenAPI / Swagger UI**: Documentation is maintained via markdown specifications in `docs/` rather than dynamic Swagger annotations.

---

## 💡 Key Engineering Takeaways & Lessons Learned

1. **DTO Separation Prevents Security Leaks**: Decoupling entities from API contracts prevents unwanted exposure of internal fields (e.g. password hashes or internal foreign key references).
2. **Spring Security Filter Chain Precision**: Manually instantiating `JwtAuthenticationFilter` inside `SecurityFilterChain` prevents double registration issues often caused by combining `@Component` with `http.addFilterBefore()`.
3. **Transaction Boundaries on Multi-Entity Mutations**: Annotating `BorrowService` methods with `@Transactional` guarantees atomicity, ensuring that if creating a `BorrowRecord` fails, the `Book` availability flag is rolled back automatically.
4. **Secrets Management Hygiene**: Keeping secrets out of Git repositories via `.env.example` templates and `dotenv-java` enables reproducible setups across different deployment environments.

---

## 🚀 Future Roadmap

If development resumes for v2.0, the recommended feature roadmap includes:

- [ ] **Pagination & Filtering**: Integrate `Pageable`, `Sort`, and Spring Data JPA Specifications for multi-criteria book searching.
- [ ] **Docker Containerization**: Provide a single `docker-compose.yml` defining the Spring Boot backend and PostgreSQL database for one-command startup.
- [ ] **Overdue Background Scheduler**: Implement `@Scheduled` cron jobs to check due dates and calculate overdue fines.
- [ ] **OpenAPI 3.0 / Swagger UI**: Integrate `springdoc-openapi` for live interactive API documentation.
- [ ] **CI/CD Pipeline**: Add GitHub Actions workflow for automated test execution on every pull request.

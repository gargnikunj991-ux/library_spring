# 📚 Library Management System Backend (LibroSphere)

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI%203.0-green.svg?style=flat&logo=swagger)](http://localhost:8080/swagger-ui/index.html)
[![JUnit 5](https://img.shields.io/badge/JUnit5-66%20Passed-success.svg?style=flat&logo=junit5)](docs/TESTING.md)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg?style=flat&logo=docker)](docker-compose.yml)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Complete%20v1.1.0-success.svg)](docs/PROJECT_STATUS.md)

> A production-grade RESTful Library Lending & Reservation Engine backend built with **Java 21**, **Spring Boot 4.1.0**, **Spring Data JPA**, **Spring Security (Stateless JWT + Refresh Token Rotation)**, **OpenAPI Swagger 3.0 UI**, and **PostgreSQL**.
> 
> **Interactive Swagger UI**: `http://localhost:8080/swagger-ui/index.html` (Supports JWT Bearer Token Authorization)

---

## 📑 Table of Contents
- [🎯 What Problem It Solves](#-what-problem-it-solves)
- [✨ Key Features](#-key-features)
- [🛠️ Tech Stack](#️-tech-stack)
- [🧭 Architecture Overview](#-architecture-overview)
- [📂 Project Directory Structure](#-project-directory-structure)
- [📋 Prerequisites](#-prerequisites)
- [⚙️ Environment Variables](#️-environment-variables)
- [🗄️ Database Setup](#️-database-setup)
- [🚀 How to Run Locally](#-how-to-run-locally)
- [🐳 Running with Docker](#-running-with-docker)
- [🔌 REST API Overview](#-rest-api-overview)
- [💡 Example API Requests & Responses](#-example-api-requests--responses)
- [⚠️ Validation & Error Handling](#️-validation--error-handling)
- [🧪 Testing Instructions](#-testing-instructions)
- [📈 Future Improvements](#-future-improvements)
- [📚 Full Documentation Index](#-full-documentation-index)
- [👤 Author & License](#-author--license)

---

## 🎯 What Problem It Solves

Traditional library management often suffers from manual ledger errors, double-checkout race conditions, lost inventory, uncollected overdue fines, untracked loan durations, and unauthorized data access. 

This backend solves these challenges by providing:
1. **Concurrency-Safe Multi-Copy Inventory**: Prevents Time-of-Check to Time-of-Use (TOCTOU) race conditions on multi-copy inventory using pessimistic write row locking (`SELECT ... FOR UPDATE`).
2. **Automated FIFO Waitlist & Reservation Engine**: When books are exhausted, patrons join a FIFO queue. Returned books automatically lock for next-in-line patrons with a 48-hour pickup window (`NOTIFIED_READY`) instead of leaking to public availability.
3. **Automated Midnight Overdue Fine Worker**: Background cron scheduler (`@Scheduled`) evaluates overdue loans nightly, calculating deterministic and idempotent tiered fines without blocking HTTP request threads.
4. **Optimized Catalog Search**: Composite B-Tree indexes enable sub-millisecond keyword discovery over book titles and authors (`GET /api/books/search`).
5. **Role-Based Access Control (RBAC)**: Protects administrative operations (`ADMIN`, `LIBRARIAN`, `ASSISTANT`) with stateless JWT security and database-persisted refresh token rotation.
6. **Decoupled & Secure API Contracts**: Prevents internal data leakage by insulating database entities behind strict Data Transfer Objects (DTOs).

---

## ✨ Key Features

- **🔐 Stateless JWT Authentication & Refresh Token Rotation**:
  - Secure login issuing signed HMAC-SHA256 JWT access tokens and database-persisted refresh tokens.
  - Seamless access token renewal via `POST /auth/refresh`.
  - Instant session invalidation via `POST /auth/logout` (`revoked = true`).
  - Automated admin bootstrapping on startup via `DataInitializer` (`admin` / `admin123`).
- **🛡️ Granular Role-Based Authorization (RBAC)**:
  - Distinct permission tiers for `ADMIN`, `LIBRARIAN`, and `ASSISTANT`.
- **📖 Multi-Copy Inventory & Concurrency Locking**:
  - `totalCopies` and `availableCopies` inventory tracking.
  - Concurrency safety guaranteed via `@Lock(LockModeType.PESSIMISTIC_WRITE)`.
- **🎟️ FIFO Waitlist Reservation Engine**:
  - Automatic queueing when `availableCopies == 0`.
  - Automatic asset reservation for queue head on return with 48h pickup window (`NOTIFIED_READY`).
  - Automated reallocation on cancellation.
- **💰 Scheduled Overdue Fine Reconciliation**:
  - Tiered overdue penalty formula (₹1/day for days 1–5, ₹5/day for days 6–15, ₹10/day for day 16+).
  - Nightly midnight `@Scheduled` cron worker (`OverdueReconciliationWorker`).
  - Idempotent liabilities tracking with support for on-demand manual triggers (`POST /api/fines/reconcile`).
- **⚡ High-Performance Composite B-Tree Database Indexes**:
  - `idx_books_title_author` and `idx_books_author` for sub-millisecond catalog discovery.
  - `idx_borrow_returned_due_date` eliminating full table scans during nightly worker reconciliation.
  - `idx_reservation_book_status_fifo` for instant FIFO waitlist queue dispatch.
- **🛡️ Enterprise Error Handling & Sanitization**:
  - Centralized `@ControllerAdvice` in `GlobalExceptionHandler` mapping custom exceptions to clean HTTP responses with `server.error.include-stacktrace=never`.
- **🐳 Containerized DevOps & CI**:
  - Multi-stage `Dockerfile` (Eclipse Temurin JRE 21).
  - Orchestration via `docker-compose.yml` with PostgreSQL 16 Alpine.
  - Automated GitHub Actions CI workflow running 66 automated tests on pushes and PRs.

---

## 🛠️ Tech Stack

| Category | Technology |
|---|---|
| **Language** | Java 21 (LTS) |
| **Framework** | Spring Boot 4.1.0 (Spring MVC, Spring Data JPA, Spring Security) |
| **Database** | PostgreSQL 16+ |
| **ORM / Persistence** | Hibernate ORM, Spring Data JPA |
| **Security & Tokens** | Spring Security, JJWT (`io.jsonwebtoken` 0.12.7) |
| **Validation** | Jakarta Bean Validation (`hibernate-validator`) |
| **Documentation** | SpringDoc OpenAPI 3.0 / Swagger UI (`2.5.0`) |
| **Configuration** | Dotenv Java (`io.github.cdimascio:dotenv-java`) |
| **Testing** | JUnit 5, Mockito, H2 In-Memory DB (66 Automated Tests) |
| **Containerization** | Docker, Docker Compose |
| **Build & Tooling** | Maven 3.9+ (Maven Wrapper included) |

---

## 🧭 Architecture Overview

The system strictly implements **Clean Layered Backend Architecture**:

```
[ HTTP Client / Postman / Swagger UI ]
               │
               ▼
[ Spring Security Filter Chain ]  ── (Stateless JWT, RBAC, 401/403 Handling)
               │
               ▼
      [ Controller Layer ]        ── (REST Endpoints, @Valid DTO Validation, ResponseEntity)
               │
               ▼
        [ Service Layer ]          ── (Business Rules, @Transactional, DTO Mapping, Row Locks)
               │
               ▼
      [ Repository Layer ]        ── (Spring Data JPA, Pessimistic Locking, Custom Queries)
               │
               ▼
     [ PostgreSQL Database ]      ── (7 Tables: users, refresh_tokens, books, members,
                                       borrow_records, book_reservations, fine_records)
```

Additionally, background maintenance is handled asynchronously:
```
[ Cron Scheduler (@Scheduled) ] ──► [ OverdueReconciliationWorker ] ──► [ FineService ] ──► PostgreSQL
```

---

## 📂 Project Directory Structure

```
library/
├── .dockerignore                       # Docker context exclusions
├── .env.example                       # Template for local environment variables
├── .github/workflows/ci.yml           # GitHub Actions automated build and test pipeline
├── .gitignore                         # Git exclusion rules
├── AGENTS.md                          # AI development rules and guidelines
├── API_DOCUMENTATION.md               # Primary REST API reference specification
├── CHANGELOG.md                       # Version history and release notes
├── CONTRIBUTING.md                    # Contribution workflow and style guide
├── DATABASE_SCHEMA.md                 # PostgreSQL schemas, ERD, tables & composite indexes
├── docker-compose.yml                 # Multi-container orchestration (App + PostgreSQL 16)
├── Dockerfile                         # Multi-stage container build (Temurin JRE 21)
├── LICENSE                            # MIT License
├── pom.xml                            # Maven dependencies and build plugins
├── PROJECT_BRAIN.md                   # Single source of truth & transformation ledger
├── PROJECT_SUMMARY.md                 # Architecture summary and developer index
├── README.md                          # Master project documentation
├── SECURITY.md                        # Security policy and vulnerability disclosure
├── mvnw / mvnw.cmd                    # Cross-platform Maven Wrapper scripts
│
├── docs/                              # Extended Documentation Suite
│   ├── API_DOCUMENTATION.md           # REST API specifications
│   ├── ARCHITECTURE.md                # System design, layer deep-dive & sequence flows
│   ├── DATABASE.md                    # Database design & schemas
│   ├── PROJECT_STATUS.md              # Milestones, retrospective & limitations
│   ├── SETUP.md                       # Local installation & Docker setup guide
│   ├── TESTING.md                     # Automated test suites, test matrix & cURL examples
│   └── images/                        # Visual diagrams and demo assets
│
└── src/
    ├── main/
    │   ├── java/com/nikunj/library/
    │   │   ├── LibraryApplication.java# Application entry point (@EnableScheduling)
    │   │   ├── config/                # SecurityConfig, JwtAuthFilter, OpenApiConfig, DataInitializer
    │   │   ├── controller/            # Auth, Book, Member, Borrow, Reservation, Fine Controllers
    │   │   ├── dto/                   # Inbound Request & Outbound Response DTOs
    │   │   ├── exception/             # Custom Domain Exceptions & GlobalExceptionHandler
    │   │   ├── model/                 # JPA Database Entities (7 tables)
    │   │   ├── repository/            # Spring Data JPA Repositories (Locks & Custom Queries)
    │   │   ├── service/               # Business Logic, Locking & Transactional Services
    │   │   └── worker/                # OverdueReconciliationWorker (@Scheduled nightly cron)
    │   └── resources/
    │       └── application.properties # Main application properties configuration
    └── test/
        ├── java/com/nikunj/library/   # 66 Automated Unit, Concurrency, and Worker Test Suites
        └── resources/
            └── application.properties # H2 in-memory test database configuration
```

---

## 📋 Prerequisites

Before running the application, ensure you have:
- **Java 21 JDK** installed (`java -version`)
- **PostgreSQL 15+** installed and running on `localhost:5432` (or use Docker)
- **Git** installed

---

## ⚙️ Environment Variables

The application reads properties dynamically from a root `.env` file via `dotenv-java`.

| Variable | Description | Default Fallback |
|---|---|---|
| `DB_URL` | JDBC Connection URL to PostgreSQL | `jdbc:postgresql://localhost:5432/library` |
| `DB_USERNAME` | PostgreSQL database username | `postgres` |
| `DB_PASSWORD` | PostgreSQL database password | *(None / Required)* |
| `JWT_SECRET` | 256-bit Base64 secret key for HMAC-SHA256 signing | `5F05gEkmG5Gxi6GHqehXUFlsusNdoO0tXnwuK1iUVpQ=` |
| `JWT_REFRESH_EXPIRATION_MS`| Refresh token lifetime in milliseconds | `604800000` (7 days) |
| `SERVER_PORT` | HTTP port for the web server | `8080` |

---

## 🗄️ Database Setup

1. Connect to your PostgreSQL instance:
   ```bash
   psql -U postgres
   ```
2. Create the application database:
   ```sql
   CREATE DATABASE library;
   ```
3. Hibernate automatically creates and synchronizes all tables and indexes on startup (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🚀 How to Run Locally

### 1. Clone & Configure
```bash
git clone https://github.com/gargnikunj991-ux/library.git
cd library

# Copy environment template
cp .env.example .env
```
*Edit `.env` and configure your `DB_PASSWORD`.*

### 2. Build & Run
```bash
# Using Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Using Maven Wrapper (Linux / macOS)
./mvnw spring-boot:run
```
The server will start on **`http://localhost:8080`**.  
Interactive Swagger UI is live at: **`http://localhost:8080/swagger-ui/index.html`**.

> **Default Seeded Admin User**:
> - Username: `admin`
> - Password: `admin123`

---

## 🐳 Running with Docker

Run the complete multi-container stack (PostgreSQL 16 + Spring Boot App) with a single command:

```bash
docker-compose up -d --build
```
To stop the stack:
```bash
docker-compose down
```

---

## 🔌 REST API Overview

### 🔑 Authentication Endpoints (`/auth`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `POST` | `/auth/login` | Authenticate user & receive JWT and Refresh Token | Public (`permitAll`) |
| `POST` | `/auth/register` | Register new system operator | `ADMIN` |
| `POST` | `/auth/refresh` | Renew access token via refresh token | Public (`permitAll`) |
| `POST` | `/auth/logout` | Revoke active refresh token | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |

### 📚 Book Catalog Endpoints (`/api/books`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `GET` | `/api/books` | Retrieve all books | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/books/search?query=...`| Sub-millisecond indexed keyword search | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/books/{id}` | Retrieve book by ID | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/books` | Register new book (with `totalCopies`) | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `PUT` | `/api/books/{id}` | Update book details | `ADMIN`, `LIBRARIAN` |
| `DELETE` | `/api/books/{id}` | Delete book | `ADMIN` |

### 👤 Member Registry Endpoints (`/api/members`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `GET` | `/api/members` | Retrieve all members | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/members/{memberId}` | Retrieve member by ID | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/members` | Register new member | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `PUT` | `/api/members/{memberId}` | Update member details | `ADMIN`, `LIBRARIAN` |
| `DELETE` | `/api/members/{memberId}` | Delete member | `ADMIN` |

### 📖 Borrow & Return Endpoints (`/api/borrow`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `POST` | `/api/borrow` | Checkout an available book (Pessimistic lock) | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/borrow/return/{borrowId}` | Process book return & FIFO auto-allocation | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |

### 🎟️ Reservation & Waitlist Endpoints (`/api/reservations`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `POST` | `/api/reservations` | Join FIFO waitlist queue for out-of-stock book | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/reservations/{id}/cancel` | Cancel reservation (auto-reallocates held copy)| `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/reservations/{id}` | Retrieve reservation details by ID | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/reservations/book/{bookId}`| Retrieve FIFO waitlist queue for a book | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/reservations/member/{memberId}`| Retrieve all reservations for a member | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |

### 💰 Fine Management Endpoints (`/api/fines`)
| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `GET` | `/api/fines` | System-wide fine ledger overview | `ADMIN`, `LIBRARIAN` |
| `GET` | `/api/fines/member/{memberId}` | Patron unpaid liability lookup | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/fines/{fineId}/pay` | Settle / pay outstanding fine | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/fines/reconcile` | Manually trigger overdue fine reconciliation | `ADMIN` |

👉 *For detailed request/response schemas and HTTP codes, refer to [API_DOCUMENTATION.md](API_DOCUMENTATION.md).*

---

## 💡 Example API Requests & Responses

### 1. User Login (`POST /auth/login`)
**Request Body**:
```json
{
  "username": "admin",
  "password": "admin123"
}
```
**Response (`200 OK`)**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890",
  "tokenType": "Bearer"
}
```

### 2. Concurrency-Safe Borrow (`POST /api/borrow`)
**Request Headers**: `Authorization: Bearer <ACCESS_TOKEN>`  
**Request Body**:
```json
{
  "bookId": 1,
  "memberId": 1
}
```
**Response (`200 OK`)**:
```json
{
  "borrowId": 101,
  "bookId": 1,
  "memberName": "Alice Johnson",
  "bookTitle": "Clean Code",
  "borrowDate": "2026-10-09",
  "dueDate": "2026-10-23",
  "returned": false
}
```

### 3. Join Waitlist Queue (`POST /api/reservations`)
**Request Body**:
```json
{
  "bookId": 1,
  "memberId": 2
}
```
**Response (`201 Created`)**:
```json
{
  "reservationId": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 2,
  "memberName": "Bob Smith",
  "status": "WAITING",
  "reservedAt": "2026-10-09T20:00:00",
  "pickupDeadline": null
}
```

---

## ⚠️ Validation & Error Handling

All incoming DTOs are validated using Jakarta Validation constraints. When a rule is violated, the API returns a structured HTTP 400 response:

```json
[
  "Email must be a well-formed email address",
  "Title cannot be blank"
]
```

Centralized Exception Mapping ([`GlobalExceptionHandler`](src/main/java/com/nikunj/library/exception/GlobalExceptionHandler.java)):
- `BookNotFoundException` ➔ `404 Not Found` (`"Book Not Found"`)
- `BookUnavailableException` ➔ `404 Not Found` (`"Book Not available"`)
- `MemberNotFoundException` ➔ `404 Not Found` (`"Member Not Found"`)
- `BorrowRecordNotFoundException` ➔ `404 Not Found` (`"Borrow Record Not Found"`)
- `ReservationNotFoundException` ➔ `404 Not Found` (`"Reservation Not Found"`)
- `FineNotFoundException` ➔ `404 Not Found` (`"Fine Record Not Found"`)
- `DuplicateReservationException` ➔ `409 Conflict` (`"Member already has active reservation..."`)
- `TokenRefreshException` ➔ `401 Unauthorized` (`"Refresh token was expired..."`)
- `AuthenticationEntryPoint` ➔ `401 Unauthorized` (`{"error": "Unauthorized", ...}`)
- `AccessDeniedHandler` ➔ `403 Forbidden` (`{"error": "Forbidden", ...}`)

---

## 🧪 Testing Instructions

Run the automated test suite (66 tests across unit, integration, stress concurrency, and worker suites):

```bash
# Validate compilation of test sources
mvn clean test-compile

# Execute all 66 automated tests
mvn test
```

### Key Test Suites:
- `BorrowConcurrencyIntegrationTest`: 10-thread stress test proving zero double-checkouts under high contention.
- `BorrowReservationIntegrationTest`: End-to-end integration verifying FIFO auto-assignment on return.
- `ReservationServiceTest`: 12 unit tests verifying waitlist queue transitions and 48h pickup windows.
- `FineServiceTest` & `OverdueReconciliationWorkerTest`: 14 unit tests validating tiered penalty calculations and idempotent cron execution.
- `AuthServiceTest` & `RefreshTokenServiceTest`: 10 unit tests verifying JWT generation, expiration, and revocation.

For a comprehensive test matrix, see [docs/TESTING.md](docs/TESTING.md).

---

## 📈 Future Improvements

- [ ] Add pagination and dynamic sorting (`Pageable`) for high-volume catalogs.
- [ ] Asynchronous event-driven notifications (Spring ApplicationEvent / Kafka / JavaMailSender) alerting members when a reserved book is ready for pickup.
- [ ] Production Cloud Deployment (Railway / AWS ECS / Render).

---

## 📚 Full Documentation Index

- 📘 **[PROJECT_SUMMARY.md](PROJECT_SUMMARY.md)** — Architectural summary, package index & developer guidelines.
- 🔌 **[API_DOCUMENTATION.md](API_DOCUMENTATION.md)** — Comprehensive REST API reference and contracts.
- 🗄️ **[DATABASE_SCHEMA.md](DATABASE_SCHEMA.md)** — Relational ERD diagrams, 7 table definitions & B-Tree indexes.
- 🧠 **[PROJECT_BRAIN.md](PROJECT_BRAIN.md)** — Single source of truth & transformation ledger.
- 🏛️ **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — System layering, design patterns & sequence diagrams.
- 🚀 **[docs/SETUP.md](docs/SETUP.md)** — Step-by-step local & Docker installation guide.
- 🧪 **[docs/TESTING.md](docs/TESTING.md)** — Complete test matrices & automated testing documentation.
- 📊 **[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)** — Milestones, achievements & retrospective.
- 🤝 **[CONTRIBUTING.md](CONTRIBUTING.md)** — Contribution standards, Git workflow & branch conventions.
- 🛡️ **[SECURITY.md](SECURITY.md)** — Security policy, threat model & vulnerability disclosure.
- 📜 **[CHANGELOG.md](CHANGELOG.md)** — Version history following Keep a Changelog.

---

## 👤 Author & License

**Nikunj Garg**  
- GitHub: [@gargnikunj991-ux](https://github.com/gargnikunj991-ux)

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

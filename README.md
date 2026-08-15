# 📚 Library Management System Backend

[![Java](https://img.shields.io/badge/Java-21-orange.svg?style=flat&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen.svg?style=flat&logo=springboot)](https://spring.io/projects/spring-boot)
[![OpenAPI](https://img.shields.io/badge/Swagger-OpenAPI%203.0-green.svg?style=flat&logo=swagger)](http://localhost:8080/swagger-ui/index.html)
[![JUnit 5](https://img.shields.io/badge/JUnit5-28%20Passed-success.svg?style=flat&logo=junit5)](docs/TESTING.md)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg?style=flat&logo=postgresql)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Complete%20v1.0.0-success.svg)](docs/PROJECT_STATUS.md)

> A production-grade RESTful Library Management System backend built with **Java 21**, **Spring Boot 4.1.0**, **Spring Data JPA**, **Spring Security (Stateless JWT + Refresh Token Rotation)**, **OpenAPI Swagger 3.0 UI**, and **PostgreSQL**.
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
- [🔌 REST API Overview](#-rest-api-overview)
- [💡 Example API Requests & Responses](#-example-api-requests--responses)
- [⚠️ Validation & Error Handling](#️-validation--error-handling)
- [🧪 Testing Instructions](#-testing-instructions)
- [📈 Future Improvements](#-future-improvements)
- [📚 Full Documentation Index](#-full-documentation-index)
- [👤 Author & License](#-author--license)

---

## 🎯 What Problem It Solves

Traditional library management often suffers from manual ledger errors, double-checkout conflicts, lost inventory, untracked loan durations, and unauthorized data access. 

This backend solves these challenges by providing:
1. **Automated Availability & Conflict Prevention**: Guarantees that borrowed books cannot be checked out simultaneously through atomic database transactions.
2. **Automated Return Due Dates**: Automatically computes standard 14-day checkout windows and tracks real-time return dates.
3. **Role-Based Access Control (RBAC)**: Protects sensitive administrative functions (user creation, inventory deletion) while allowing librarians and assistants to conduct daily operations.
4. **Decoupled & Secure API Contracts**: Eliminates data leakage by insulating database entities behind strict Data Transfer Objects (DTOs).

---

## ✨ Key Features

- **🔐 Stateless JWT Authentication & Refresh Token Rotation**:
  - Secure login issuing signed HMAC-SHA256 JWT access tokens and database-persisted refresh tokens.
  - Seamless access token renewal via `POST /auth/refresh`.
  - Instant session invalidation via `POST /auth/logout` (`revoked = true`).
- **🛡️ Granular Role-Based Authorization**:
  - Distinct permission tiers for `ADMIN`, `LIBRARIAN`, and `ASSISTANT`.
- **📖 Book Inventory Management**:
  - Full CRUD operations with availability state tracking.
- **👤 Member Registry Management**:
  - Registration and profile management with email validation.
- **🔄 Transactional Borrow & Return Engine**:
  - `@Transactional` state mutations linking books and members with checkout timestamps.
- **🛡️ Enterprise Error Handling & Sanitization**:
  - Centralized `@ControllerAdvice` mapping custom exceptions to clean HTTP responses with `server.error.include-stacktrace=never`.
- **🌱 Secure Environment Configuration**:
  - Fully parameterized credentials backed by `dotenv-java` and `.env.example`.

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
| **Configuration** | Dotenv Java (`io.github.cdimascio:dotenv-java`) |
| **Build & Dependency Management** | Maven 3.9+ (Maven Wrapper included) |

---

## 🧭 Architecture Overview

The system strictly implements **Clean Layered Backend Architecture**:

```
[ HTTP Client / Postman / Frontend ]
               │
               ▼
[ Spring Security Filter Chain ]  ── (JWT Validation, RBAC, 401/403 Handling)
               │
               ▼
      [ Controller Layer ]        ── (REST Routing, @Valid DTO Validation, ResponseEntity)
               │
               ▼
       [ Service Layer ]          ── (Business Rules, Transactions @Transactional, DTO Mapping)
               │
               ▼
      [ Repository Layer ]        ── (Spring Data JPA, Hibernate ORM Queries)
               │
               ▼
     [ PostgreSQL Database ]      ── (Relational Persistence: users, tokens, books, members, borrow_records)
```

---

## 📂 Project Directory Structure

```
library/
├── .env.example                       # Template for local environment variables
├── .gitignore                         # Git exclusion rules
├── AGENTS.md                          # AI development rules and guidelines
├── CHANGELOG.md                       # Version history and release notes
├── CONTRIBUTING.md                    # Contribution workflow and style guide
├── LICENSE                            # MIT License
├── README.md                          # Master project documentation
├── SECURITY.md                        # Security policy and vulnerability disclosure
├── pom.xml                            # Maven dependencies and build plugins
├── mvnw / mvnw.cmd                    # Cross-platform Maven Wrapper scripts
│
├── docs/                              # Detailed Documentation Suite
│   ├── API_DOCUMENTATION.md           # Comprehensive REST API specifications
│   ├── ARCHITECTURE.md                # System design, layer deep-dive & sequence flows
│   ├── DATABASE.md                    # PostgreSQL schemas, ERD, tables & constraints
│   ├── PROJECT_STATUS.md              # Milestones, retrospective & limitations
│   ├── SETUP.md                       # Step-by-step local installation guide
│   ├── TESTING.md                     # Automated test suites, test matrix & cURL examples
│   └── images/                        # Visual diagrams and demo assets
│
└── src/
    ├── main/
    │   ├── java/com/nikunj/library/
    │   │   ├── LibraryApplication.java# Application entry point (Loads .env)
    │   │   ├── config/                # SecurityConfig & JwtAuthenticationFilter
    │   │   ├── controller/            # Auth, Book, Member, Borrow REST Controllers
    │   │   ├── dto/                   # Request & Response Data Transfer Objects
    │   │   ├── exception/             # Custom Exceptions & GlobalExceptionHandler
    │   │   ├── model/                 # JPA Database Entities
    │   │   ├── repository/            # Spring Data JPA Repositories
    │   │   └── service/               # Business Logic & Transactional Services
    │   └── resources/
    │       └── application.properties # Application properties configuration
    └── test/
        └── java/com/nikunj/library/   # Unit and integration test suites
```

---

## 📋 Prerequisites

Before running the application, make sure you have:
- **Java 21 JDK** installed (`java -version`)
- **PostgreSQL 15+** installed and running on `localhost:5432`
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
3. Hibernate will automatically create and synchronize all tables on startup (`spring.jpa.hibernate.ddl-auto=update`).

---

## 🚀 How to Run Locally

### 1. Clone & Configure
```bash
git clone https://github.com/gargnikunj991-ux/library.git
cd library

# Copy environment template
cp .env.example .env
```
*Edit `.env` and set your PostgreSQL `DB_PASSWORD`.*

### 2. Build & Run
```bash
# Using Maven Wrapper (Windows)
.\mvnw.cmd spring-boot:run

# Using Maven Wrapper (Linux / macOS)
./mvnw spring-boot:run
```
The server will start on **`http://localhost:8080`**.

---

## 🔌 REST API Overview

| Method | Endpoint | Description | Role Required |
|---|---|---|---|
| `POST` | `/auth/login` | Authenticate user & receive JWT | Public (`permitAll`) |
| `POST` | `/auth/register` | Register new system operator | `ADMIN` |
| `POST` | `/auth/refresh` | Refresh expired access token | Public (`permitAll`) |
| `POST` | `/auth/logout` | Revoke active refresh token | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/books` | Retrieve all books | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/books/{id}` | Retrieve book by ID | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/books` | Register new book | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `PUT` | `/api/books/{id}` | Update book details | `ADMIN`, `LIBRARIAN` |
| `DELETE` | `/api/books/{id}` | Delete book | `ADMIN` |
| `GET` | `/api/members` | Retrieve all members | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `GET` | `/api/members/{memberId}` | Retrieve member by ID | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/members` | Register new member | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `PUT` | `/api/members/{memberId}` | Update member details | `ADMIN`, `LIBRARIAN` |
| `DELETE` | `/api/members/{memberId}` | Delete member | `ADMIN` |
| `POST` | `/api/borrow` | Checkout an available book | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |
| `POST` | `/api/borrow/return/{borrowId}` | Process book return | `ADMIN`, `LIBRARIAN`, `ASSISTANT` |

👉 *For full parameter schemas and error codes, refer to [docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md).*

---

## 💡 Example API Requests & Responses

### 1. User Login (`POST /auth/login`)
**Request Body**:
```json
{
  "username": "admin",
  "password": "AdminPassword123!"
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

### 2. Borrow a Book (`POST /api/borrow`)
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
  "borrowDate": "2026-08-12",
  "dueDate": "2026-08-26",
  "returned": false
}
```

---

## ⚠️ Validation & Error Handling

All incoming DTOs are validated using Jakarta Validation constraints. When a rule is violated, the API returns a structured HTTP 400 response:

```json
[
  "Email must be a well-formed email address",
  "Name cannot be blank"
]
```

Centralized Exception Mapping (`GlobalExceptionHandler`):
- `BookNotFoundException` ➔ `404 Not Found` (`"Book Not Found"`)
- `BookUnavailableException` ➔ `404 Not Found` (`"Book Not available"`)
- `MemberNotFoundException` ➔ `404 Not Found` (`"Member Not Found"`)
- `TokenRefreshException` ➔ `401 Unauthorized` (`"Refresh token was expired..."`)
- `AuthenticationEntryPoint` ➔ `401 Unauthorized` (`{"error": "Unauthorized", ...}`)
- `AccessDeniedHandler` ➔ `403 Forbidden` (`{"error": "Forbidden", ...}`)

---

## 🧪 Testing Instructions

Run automated unit and integration test suites via Maven:

```bash
# Validate compilation of test sources
mvn clean test-compile

# Execute all tests
mvn test
```

For a comprehensive test matrix and manual cURL verification commands, see [docs/TESTING.md](docs/TESTING.md).

---

## 📈 Future Improvements

- [ ] Add pagination and dynamic sorting (`Pageable`) for book and member catalogs.
- [ ] Implement automated overdue fine calculation with Spring `@Scheduled` background cron jobs.
- [ ] Containerize application with Docker & Docker Compose.
- [ ] Integrate Swagger / OpenAPI 3.0 UI for interactive browser documentation.

---

## 📚 Full Documentation Index

- 🔌 **[docs/API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)** — Detailed REST endpoint specifications, schemas & error responses.
- 🏛️ **[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md)** — System layering, design patterns & sequence diagrams.
- 🗄️ **[docs/DATABASE.md](docs/DATABASE.md)** — Relational ERD diagrams, table definitions & column constraints.
- 🚀 **[docs/SETUP.md](docs/SETUP.md)** — Zero-assumption local installation & troubleshooting guide.
- 🧪 **[docs/TESTING.md](docs/TESTING.md)** — Complete test matrices & manual cURL testing scripts.
- 📊 **[docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md)** — Project status, milestone retrospective & engineering takeaways.
- 🤝 **[CONTRIBUTING.md](CONTRIBUTING.md)** — Contribution standards, Git workflow & branch conventions.
- 🛡️ **[SECURITY.md](SECURITY.md)** — Security policy, threat model & vulnerability disclosure.
- 📜 **[CHANGELOG.md](CHANGELOG.md)** — Version history following Keep a Changelog.

---

## 👤 Author & License

**Nikunj Garg**  
- GitHub: [@gargnikunj991-ux](https://github.com/gargnikunj991-ux)

This project is licensed under the **MIT License** - see the [LICENSE](LICENSE) file for details.

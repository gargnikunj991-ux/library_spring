# 🤝 Contributing to Library Management System

Thank you for your interest in contributing to the **Library Management System**! We welcome contributions, bug fixes, enhancements, and documentation improvements.

---

## 📜 Table of Contents
1. [Code of Conduct](#-code-of-conduct)
2. [Getting Started](#-getting-started)
3. [Branching Strategy](#-branching-strategy)
4. [Coding Standards & Guidelines](#-coding-standards--guidelines)
5. [Commit Message Conventions](#-commit-message-conventions)
6. [Pull Request (PR) Workflow](#-pull-request-pr-workflow)
7. [Testing Requirements](#-testing-requirements)

---

## 🕊️ Code of Conduct
- Be respectful, constructive, and professional in all interactions.
- Provide thoughtful, well-documented feedback during code reviews.
- Focus on clean architecture, maintainability, and security.

---

## 🚀 Getting Started

1. **Fork & Clone** the repository:
   ```bash
   git clone https://github.com/gargnikunj991-ux/library.git
   cd library
   ```
2. **Configure Environment**:
   - Copy `.env.example` to `.env` and configure your local PostgreSQL database and JWT secret:
     ```bash
     cp .env.example .env
     ```
3. **Verify Build**:
   ```bash
   mvn clean test-compile
   ```

---

## 🌿 Branching Strategy

- **`main`**: Production-ready branch. All code in `main` must compile and pass tests.
- **Feature Branches**: Branch out from `main` using descriptive prefixes:
  - `feature/user-pagination`
  - `bugfix/token-revocation-leak`
  - `docs/setup-guide-refactor`
  - `refactor/dto-mapping-cleanup`

---

## 📐 Coding Standards & Guidelines

### 1. Architectural Integrity
- **Strict Layering**: Follow `Controller` → `Service` → `Repository`.
- **Never bypass the Service layer** from a Controller.
- **Never expose JPA Entities directly** across REST endpoints; always map through DTOs (`dto/` package).

### 2. Dependency Injection & Clean Code
- Use **Constructor Injection** for all spring components and services.
- Keep methods focused, small, and self-documenting.
- Use explicit Java 21 features where applicable.

### 3. Validation & Exception Handling
- Annotate all incoming request DTOs with Jakarta Validation (`@Valid`, `@NotBlank`, `@NotNull`, `@Email`).
- Do not handle business errors with silent returns or HTTP 500s; throw explicit domain runtime exceptions (e.g., `BookNotFoundException`, `TokenRefreshException`) and handle them in `GlobalExceptionHandler`.

### 4. Database & Transactions
- Wrap state-altering service methods involving multiple operations in `@Transactional`.
- Use Spring Data JPA method naming conventions or explicit JPQL queries.

---

## 💬 Commit Message Conventions

We follow the [Conventional Commits](https://www.conventionalcommits.org/) standard:

| Type | Purpose | Example |
|---|---|---|
| `feat` | Adding a new feature | `feat(borrow): add fine calculation on overdue returns` |
| `fix` | Bug fixes | `fix(security): resolve token expiration check edge case` |
| `docs` | Documentation updates | `docs(readme): add environment variable table` |
| `refactor` | Code refactoring without behavioral change | `refactor(auth): migrate filter logging to SLF4J` |
| `test` | Adding or modifying tests | `test(book): add controller validation test suite` |
| `chore` | Build tasks, dependencies, configs | `chore(deps): update jjwt to 0.12.7` |

---

## 🔄 Pull Request (PR) Workflow

1. Create a descriptive PR title using Conventional Commits.
2. Provide a clear description in your PR template:
   - What problem does this solve?
   - What changes were made?
   - How was this tested (cURL commands / test cases)?
3. Ensure:
   - [ ] Application compiles without errors (`mvn clean compile`).
   - [ ] All unit and integration tests pass (`mvn test`).
   - [ ] No secrets or personal `.env` values are committed.
   - [ ] Project documentation in `docs/` is updated accordingly.

---

## 🧪 Testing Requirements

- Any new endpoint must be accompanied by integration and validation tests.
- When fixing a bug, include a reproduction test verifying the resolution.
- Verify positive (200/201), negative (400 Bad Request), authentication (401), authorization (403), and missing resource (404) scenarios.

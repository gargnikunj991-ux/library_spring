# Project Overview

This is a Java Spring Boot Library Management System.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA
- Hibernate
- PostgreSQL
- Maven

## Architecture

Controller
→ Service
→ Repository

Never bypass the service layer.

## Coding Standards

- Use DTOs.
- Validate all request DTOs.
- Use constructor injection.
- Use ResponseEntity.
- Use Global Exception Handling.
- Never expose entities directly.
- Follow REST API naming conventions.
- Keep methods small and readable.
- Write production-quality code.

## Before Editing Code

1. Read the relevant files first.
2. Explain the problem.
3. Suggest a solution.
4. Wait for my approval before changing files.

## Documentation Maintenance Rule

- Every time a change to the codebase is requested, read the project `.md` files (`PROJECT_SUMMARY.md`, `API_DOCUMENTATION.md`, `DATABASE_SCHEMA.md`, `PROJECT_BRAIN.md`).
- At the end of each session or task, update the project `.md` files to reflect all new/modified endpoints, entities, business logic, and project state so future context is always up to date.
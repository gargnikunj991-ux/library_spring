# 🧪 Testing Strategy & Test Reference -- LibroSphere

This document outlines the testing methodology, automated test suites, test matrix, and verification test cases for the **Library Management System (LibroSphere)**.

---

## 🎯 Testing Architecture & Methodology

The testing strategy follows the standard **Testing Pyramid**:

```
           / \
          /   \
         /  E2E  \       cURL / Postman / Swagger UI Manual Testing
        /─────────\
       /  Integr.  \     @SpringBootTest Multi-Threaded Concurrency & Waitlist Suites
      /─────────────\
     /     Unit      \   JUnit 5 & Mockito Service Layer & Worker Tests (66 Tests Total)
    /─────────────────\
```

1. **Unit Testing**: Tests isolated business logic in `Service` classes using JUnit 5 and Mockito, mocking repository interactions.
2. **Multi-Threaded Concurrency Stress Testing**: Uses `CountDownLatch` and `ExecutorService` with 10 concurrent threads to prove zero double-checkouts under high contention.
3. **Integration Testing**: Verifies multi-step workflows (e.g. checkout, waitlist auto-assignment on return) using real transactions and H2 database.
4. **Worker Testing**: Verifies `@Scheduled` background worker delegation and idempotent overdue fine calculations.

---

## 🚀 Running Automated Tests

### 1. Compile Test Sources
```bash
mvn clean test-compile
```

### 2. Execute Full Test Suite (66 Tests)
```bash
mvn test
```

### 3. Run Specific Test Suites
```bash
# Concurrency stress test
mvn test -Dtest=BorrowConcurrencyIntegrationTest

# Reservation & return integration test
mvn test -Dtest=BorrowReservationIntegrationTest

# Fine reconciliation tests
mvn test -Dtest=FineServiceTest,OverdueReconciliationWorkerTest
```

---

## 📊 Automated Test Suite Breakdown (66 Tests, 100% Pass Rate)

| Test Class | Category | Test Count | Scope & Behaviors Verified |
|---|---|:---:|---|
| [`BorrowConcurrencyIntegrationTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/BorrowConcurrencyIntegrationTest.java) | Concurrency Integration | 1 | 10 concurrent threads contending for single copy; validates exactly 1 success, 9 rejections, 0 inventory underflow. |
| [`BorrowReservationIntegrationTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/BorrowReservationIntegrationTest.java) | Integration | 1 | End-to-end return workflow verifying returned copy is held exclusively for FIFO waitlist patron with 48h deadline. |
| [`ReservationServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/ReservationServiceTest.java) | Unit | 12 | Queue entry, out-of-stock validation, duplicate waitlist rejection (409), cancellations, and FIFO ordering. |
| [`FineServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/FineServiceTest.java) | Unit | 12 | Graduated tiered overdue calculation (Days 1–5, 6–15, 16+), idempotent updates, payment settlement, lookups. |
| [`OverdueReconciliationWorkerTest`](file:///D:/library/library/src/test/java/com/nikunj/library/worker/OverdueReconciliationWorkerTest.java) | Worker Unit | 2 | Scheduled cron audit execution and service delegation without duplicate billing. |
| [`BorrowServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/BorrowServiceTest.java) | Unit | 10 | Concurrency-safe checkout, inventory decrements, return date stamping, exception handling. |
| [`BookServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/BookServiceTest.java) | Unit | 10 | CRUD operations, not found handling, indexed multi-column catalog search queries. |
| [`MemberServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/MemberServiceTest.java) | Unit | 5 | Member registry CRUD, email validation, and lookup error handling. |
| [`AuthServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/AuthServiceTest.java) | Unit | 4 | BCrypt user registration, credential verification, JWT issuance, and logout. |
| [`RefreshTokenServiceTest`](file:///D:/library/library/src/test/java/com/nikunj/library/service/RefreshTokenServiceTest.java) | Unit | 6 | UUID refresh token creation, expiration verification, revocation, and rotation. |
| [`DataInitializerTest`](file:///D:/library/library/src/test/java/com/nikunj/library/config/DataInitializerTest.java) | Unit | 2 | Startup command line runner admin bootstrapping and idempotency. |
| [`LibraryApplicationTests`](file:///D:/library/library/src/test/java/com/nikunj/library/LibraryApplicationTests.java) | Context | 1 | Spring Boot application context load verification. |
| **Total** | | **66** | **All Passing (0 Failures, 0 Errors, 0 Skipped)** |

---

## 📋 Comprehensive REST API Test Matrix

### 📚 1. Book Catalog & Search (`/api/books`)

| Case ID | Scenario | Method | Endpoint | Expected Status |
|---|---|---|---|---|
| `BK-01` | Create book with valid payload | `POST` | `/api/books` | `200 OK` |
| `BK-02` | Create book with blank title / author | `POST` | `/api/books` | `400 Bad Request` |
| `BK-03` | Create book with totalCopies < 1 | `POST` | `/api/books` | `400 Bad Request` |
| `BK-04` | Get all books | `GET` | `/api/books` | `200 OK` |
| `BK-05` | Search books by keyword matching title/author | `GET` | `/api/books/search?query=clean` | `200 OK` |
| `BK-06` | Get book by valid existing ID | `GET` | `/api/books/{id}` | `200 OK` |
| `BK-07` | Get book by non-existent ID | `GET` | `/api/books/999` | `404 Not Found` |
| `BK-08` | Update book by valid ID | `PUT` | `/api/books/{id}` | `200 OK` |
| `BK-09` | Delete book as ADMIN | `DELETE` | `/api/books/{id}` | `200 OK` |
| `BK-10` | Delete book as Non-Admin (LIBRARIAN/ASSISTANT) | `DELETE` | `/api/books/{id}` | `403 Forbidden` |

---

### 👤 2. Member Management (`/api/members`)

| Case ID | Scenario | Method | Endpoint | Expected Status |
|---|---|---|---|---|
| `MB-01` | Register valid member | `POST` | `/api/members` | `200 OK` |
| `MB-02` | Register member with invalid email syntax | `POST` | `/api/members` | `400 Bad Request` |
| `MB-03` | Get member by existing ID | `GET` | `/api/members/{memberId}` | `200 OK` |
| `MB-04` | Get member by missing ID | `GET` | `/api/members/999` | `404 Not Found` |
| `MB-05` | Delete member as ADMIN | `DELETE` | `/api/members/{memberId}` | `200 OK` |
| `MB-06` | Delete member as Non-Admin | `DELETE` | `/api/members/{memberId}` | `403 Forbidden` |

---

### 📖 3. Concurrency Borrow & Return (`/api/borrow`)

| Case ID | Scenario | Method | Endpoint | Expected Status |
|---|---|---|---|---|
| `BW-01` | Borrow available book (copies > 0) | `POST` | `/api/borrow` | `200 OK` (decrements copies) |
| `BW-02` | Borrow exhausted book (copies == 0) | `POST` | `/api/borrow` | `404 Not Found` (`Book Unavailable`) |
| `BW-03` | Return book with no waiting reservations | `POST` | `/api/borrow/return/{borrowId}` | `200 OK` (increments copies) |
| `BW-04` | Return book with active FIFO waitlist | `POST` | `/api/borrow/return/{borrowId}` | `200 OK` (locks copy for next member) |

---

### 🎟️ 4. FIFO Waitlist Reservations (`/api/reservations`)

| Case ID | Scenario | Method | Endpoint | Expected Status |
|---|---|---|---|---|
| `RS-01` | Join waitlist when copies == 0 | `POST` | `/api/reservations` | `201 Created` (`WAITING`) |
| `RS-02` | Join waitlist when copies > 0 | `POST` | `/api/reservations` | `400 Bad Request` |
| `RS-03` | Duplicate active reservation for same member | `POST` | `/api/reservations` | `409 Conflict` |
| `RS-04` | Cancel active reservation | `POST` | `/api/reservations/{id}/cancel` | `200 OK` (`CANCELLED`) |
| `RS-05` | Get waitlist queue for a book (FIFO ordered) | `GET` | `/api/reservations/book/{bookId}` | `200 OK` |
| `RS-06` | Get reservations for a member | `GET` | `/api/reservations/member/{memberId}` | `200 OK` |

---

### 💰 5. Fine Management (`/api/fines`)

| Case ID | Scenario | Method | Endpoint | Expected Status |
|---|---|---|---|---|
| `FN-01` | View library-wide fines as ADMIN/LIBRARIAN | `GET` | `/api/fines` | `200 OK` |
| `FN-02` | View library-wide fines as ASSISTANT | `GET` | `/api/fines` | `403 Forbidden` |
| `FN-03` | View member fines as ASSISTANT | `GET` | `/api/fines/member/{memberId}` | `200 OK` |
| `FN-04` | Settle fine payment | `POST` | `/api/fines/{fineId}/pay` | `200 OK` (`paid = true`) |
| `FN-05` | Pay already settled fine | `POST` | `/api/fines/{fineId}/pay` | `400 Bad Request` |
| `FN-06` | Manually trigger reconciliation as ADMIN | `POST` | `/api/fines/reconcile` | `200 OK` |

# 🧪 Testing Strategy & Test Reference

This document outlines the testing philosophy, automated test execution, test matrices, and manual API verification test cases for the **Library Management System**.

---

## 🎯 Testing Architecture & Methodology

The testing strategy follows the standard **Testing Pyramid**:

```
           / \
          /   \
         /     \
        /  E2E  \       cURL / Postman / Manual Smoke Testing
       /─────────\
      /  Integr.  \     @SpringBootTest / MockMvc REST Controller & JPA Tests
     /─────────────\
    /     Unit      \   JUnit 5 & Mockito Service Layer Business Logic Tests
   /─────────────────\
```

1. **Unit Testing**: Tests isolated business logic in `Service` classes using JUnit 5 and Mockito, mocking repository interactions.
2. **Integration Testing**: Verifies full request lifecycle, Spring Security filter chains, JSON serialization/deserialization, and `@ControllerAdvice` error mapping using `MockMvc`.
3. **Database Testing**: Verifies JPA entity relationships, cascading, constraints, and custom queries using `@DataJpaTest` with test profiles or test containers.
4. **Manual & API Contract Testing**: Validates end-to-end flows with real PostgreSQL instances using Postman or cURL.

---

## 🚀 Running Automated Tests

### 1. Compile and Validate Tests
```bash
mvn clean test-compile
```

### 2. Execute Full Test Suite
```bash
mvn test
```

### 3. Run a Specific Test Class
```bash
mvn test -Dtest=LibraryApplicationTests
```

---

## 📋 Comprehensive Test Cases Matrix

### 📚 1. Book Management Test Matrix (`/api/books`)

| Case ID | Scenario | HTTP Method | Endpoint | Expected Status | Expected Result / Body |
|---|---|---|---|---|---|
| `BK-01` | Create book with valid payload | `POST` | `/api/books` | `200 OK` | Returns created `BookResponse` with generated ID |
| `BK-02` | Create book with blank title | `POST` | `/api/books` | `400 Bad Request` | Returns `["Title cannot be blank"]` |
| `BK-03` | Create book with blank author | `POST` | `/api/books` | `400 Bad Request` | Returns `["Author cannot be blank"]` |
| `BK-04` | Get all books | `GET` | `/api/books` | `200 OK` | Returns JSON array of all books |
| `BK-05` | Get book by valid existing ID | `GET` | `/api/books/1` | `200 OK` | Returns `BookResponse` object |
| `BK-06` | Get book by non-existent ID | `GET` | `/api/books/999` | `404 Not Found` | Returns `"Book Not Found"` |
| `BK-07` | Update book by valid ID | `PUT` | `/api/books/1` | `200 OK` | Returns updated `BookResponse` |
| `BK-08` | Update book with non-existent ID | `PUT` | `/api/books/999` | `404 Not Found` | Returns `"Book Not Found"` |
| `BK-09` | Delete book by valid ID (Admin) | `DELETE` | `/api/books/1` | `200 OK` | Record deleted from PostgreSQL |
| `BK-10` | Delete book by non-existent ID | `DELETE` | `/api/books/999` | `404 Not Found` | Returns `"Book Not Found"` |
| `BK-11` | Delete book as Non-Admin (Assistant) | `DELETE` | `/api/books/1` | `403 Forbidden` | Access denied error message |

---

### 👤 2. Member Management Test Matrix (`/api/members`)

| Case ID | Scenario | HTTP Method | Endpoint | Expected Status | Expected Result / Body |
|---|---|---|---|---|---|
| `MB-01` | Register valid member | `POST` | `/api/members` | `200 OK` | Returns `MemberResponse` with `memberId` |
| `MB-02` | Register member with invalid email | `POST` | `/api/members` | `400 Bad Request` | Returns `["must be a well-formed email address"]` |
| `MB-03` | Register member with blank name | `POST` | `/api/members` | `400 Bad Request` | Returns `["name cannot be blank"]` |
| `MB-04` | Get member by existing ID | `GET` | `/api/members/1` | `200 OK` | Returns `MemberResponse` |
| `MB-05` | Get member by missing ID | `GET` | `/api/members/999` | `404 Not Found` | Returns `"Member Not Found"` |
| `MB-06` | Delete member by valid ID (Admin) | `DELETE` | `/api/members/1` | `200 OK` | Member record removed |
| `MB-07` | Delete member as Non-Admin | `DELETE` | `/api/members/1` | `403 Forbidden` | Access denied |

---

### 📖 3. Borrowing & Return Workflow Test Matrix (`/api/borrow`)

| Case ID | Scenario | HTTP Method | Endpoint | Expected Status | Expected Result / Body |
|---|---|---|---|---|---|
| `BW-01` | Borrow available book | `POST` | `/api/borrow` | `200 OK` | Returns `BorrowResponse`, sets `book.available = false`, sets `dueDate = today + 14` |
| `BW-02` | Borrow already checked out book | `POST` | `/api/borrow` | `404 Not Found` | Returns `"Book Not available"` |
| `BW-03` | Borrow with non-existent memberId | `POST` | `/api/borrow` | `404 Not Found` | Returns `"Member Not Found"` |
| `BW-04` | Borrow with non-existent bookId | `POST` | `/api/borrow` | `404 Not Found` | Returns `"Book Not Found"` |
| `BW-05` | Return borrowed book by valid borrowId | `POST` | `/api/borrow/return/1` | `200 OK` | Returns `BorrowResponse`, sets `returned = true`, resets `book.available = true` |
| `BW-06` | Return with non-existent borrowId | `POST` | `/api/borrow/return/999` | `404 Not Found` | Returns `"Borrow Record Not Found"` |

---

### 🔒 4. Authentication & Security Test Matrix (`/auth`)

| Case ID | Scenario | HTTP Method | Endpoint | Expected Status | Expected Result / Body |
|---|---|---|---|---|---|
| `AU-01` | Login with valid credentials | `POST` | `/auth/login` | `200 OK` | Returns `accessToken`, `refreshToken`, `tokenType` |
| `AU-02` | Login with invalid password | `POST` | `/auth/login` | `401 Unauthorized` | Returns `"Bad credentials"` |
| `AU-03` | Refresh token with valid refresh token | `POST` | `/auth/refresh` | `200 OK` | Returns renewed `accessToken` and `refreshToken` |
| `AU-04` | Refresh token with invalid/expired token | `POST` | `/auth/refresh` | `401 Unauthorized` | Returns `"Refresh token was expired..."` |
| `AU-05` | Logout authenticated user | `POST` | `/auth/logout` | `200 OK` | Sets `revoked = true` in PostgreSQL |
| `AU-06` | Access protected endpoint without token | `GET` | `/api/books` | `401 Unauthorized` | `"Full authentication is required..."` |

---

## 🛠️ Step-by-Step Manual Verification with cURL

### 1. Authenticate and Store JWT
```bash
# Save login response to a variable
TOKEN=$(curl -s -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "AdminPassword123!"}' | jq -r '.accessToken')
```

### 2. Verify Valid Book Creation (200 OK)
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "Refactoring", "author": "Martin Fowler", "available": true}'
```

### 3. Verify Validation Error on Blank Book Title (400 Bad Request)
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"title": "", "author": "Martin Fowler"}'
```

### 4. Verify Not Found Error (404 Not Found)
```bash
curl -X GET http://localhost:8080/api/books/99999 \
  -H "Authorization: Bearer $TOKEN"
```

### 5. Verify Complete Borrow and Return Lifecycle
```bash
# 1. Borrow book 1 for member 1
curl -X POST http://localhost:8080/api/borrow \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1}'

# 2. Attempt duplicate borrow (should fail with 404 Book Not available)
curl -X POST http://localhost:8080/api/borrow \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId": 1, "memberId": 1}'

# 3. Return the book
curl -X POST http://localhost:8080/api/borrow/return/1 \
  -H "Authorization: Bearer $TOKEN"
```

# 🔌 API Documentation -- Library Management System

This document outlines all RESTful API endpoints, request payloads, response formats, validation rules, and error handling behaviors in the Library Management System backend.

---

## 📖 Interactive Swagger 3.0 / OpenAPI Documentation

When the application is running locally, interactive API documentation with built-in JWT authorization is available at:
- **Swagger UI**: [`http://localhost:8080/swagger-ui/index.html`](http://localhost:8080/swagger-ui/index.html)
- **OpenAPI JSON Spec**: [`http://localhost:8080/v3/api-docs`](http://localhost:8080/v3/api-docs)

> **Testing via Swagger UI**: Click the **Authorize** button on top of the Swagger UI page, paste your Bearer JWT Token obtained from `/auth/login`, and execute all protected endpoints directly from your browser!

---

## 🔒 Security & Authentication

Spring Security (`SecurityConfig.java`) is enabled across all API endpoints:
- **CSRF**: Disabled (`csrf.disable()`)
- **Public Endpoints**: `/auth/login`, `/auth/refresh`, `/v3/api-docs/**`, `/swagger-ui/**`, `/swagger-ui.html`
- **Authentication**: Required (`.anyRequest().authenticated()`) for protected endpoints.
- **Authorization**: Credentials must be supplied via HTTP Authentication for protected endpoints.

### 🔑 Authentication Endpoints (`/auth`)

Base Path: `/auth`

#### 🔹 0.1 Register User
- **HTTP Method**: `POST`
- **Path**: `/auth/register`
- **Description**: Registers a new user account with BCrypt password encoding.
- **Request Body**: `RegisterRequest` (JSON)
```json
{
  "username": "johndoe",
  "password": "secretpassword",
  "role": "ADMIN"
}
```
- **Validation Rules**:
  - `username`: `@NotBlank(message = "Username is mandatory")`
  - `password`: `@NotBlank(message = "Password is mandatory")`
  - `role`: `@NotNull(message = "Role is mandatory")` (Roles: `ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Response**: `200 OK` ("User registered successfully")

#### 🔹 0.2 User Login
- **HTTP Method**: `POST`
- **Path**: `/auth/login`
- **Description**: Authenticates user credentials and returns a signed JWT access token and a refresh token.
- **Request Body**: `LoginRequest` (JSON)
```json
{
  "username": "johndoe",
  "password": "secretpassword"
}
```
- **Response**: `200 OK`
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890",
  "tokenType": "Bearer"
}
```

#### 🔹 0.3 Refresh Access Token
- **HTTP Method**: `POST`
- **Path**: `/auth/refresh`
- **Description**: Verifies a valid refresh token from PostgreSQL and issues a fresh JWT access token.
- **Request Body**: `RefreshTokenRequest` (JSON)
```json
{
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890"
}
```
- **Validation Rules**:
  - `refreshToken`: `@NotBlank(message = "Refresh token is mandatory")`
- **Response**:
  - `200 OK`: Returns new `LoginResponse` containing renewed `accessToken` and `refreshToken`.
  - `401 Unauthorized`: If refresh token is expired, revoked, or not found in database.
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890",
  "tokenType": "Bearer"
}
```

#### 🔹 0.4 User Logout & Token Revocation
- **HTTP Method**: `POST`
- **Path**: `/auth/logout`
- **Description**: Invalidates and revokes the active refresh token (`revoked = true`) in the database for the authenticated user.
- **Authorization**: Required (`Bearer <accessToken>`). Requires role: `ADMIN`, `LIBRARIAN`, or `ASSISTANT`.
- **Request Body**: None
- **Response**:
  - `200 OK`: `"User logged out and refresh token revoked successfully"`
  - `401 Unauthorized`: Missing or invalid JWT access token.
  - `403 Forbidden`: Insufficient role permissions.

---

## 📚 1. Book API Endpoints (`/api/books`)

Base Path: `/api/books`

### 🔹 1.1 Get All Books
- **HTTP Method**: `GET`
- **Path**: `/api/books`
- **Description**: Retrieves a list of all registered books.
- **Request Body**: None
- **Response**: `200 OK`
- **Sample Response Body**:
```json
[
  {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "totalCopies": 5,
    "availableCopies": 4,
    "available": true
  },
  {
    "id": 2,
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
    "totalCopies": 2,
    "availableCopies": 0,
    "available": false
  }
]
```

---

### 🔹 1.2 Get Book by ID
- **HTTP Method**: `GET`
- **Path**: `/api/books/{id}`
- **Description**: Retrieves a single book by its ID.
- **Path Variable**: `id` (Long) - Book ID
- **Response**:
  - `200 OK` if found.
  - `404 Not Found` if book ID does not exist (`"Book Not Found"`).
- **Sample Response Body (`200 OK`)**:
```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "totalCopies": 5,
  "availableCopies": 4,
  "available": true
}
```

---

### 🔹 1.3 Create New Book
- **HTTP Method**: `POST`
- **Path**: `/api/books`
- **Description**: Registers a new book into inventory.
- **Request Body**: `CreateBookRequest` (JSON)
```json
{
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "totalCopies": 3
}
```
- **Validation Rules**:
  - `title`: `@NotBlank` (Cannot be empty or null)
  - `author`: `@NotBlank` (Cannot be empty or null)
  - `totalCopies`: `@Min(1)` (Must be at least 1)
- **Response**:
  - `200 OK` with created `BookResponse`.
  - `400 Bad Request` if validation fails (returns list of validation error messages).
- **Sample Response Body (`200 OK`)**:
```json
{
  "id": 3,
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "totalCopies": 3,
  "availableCopies": 3,
  "available": true
}
```

---

### 🔹 1.4 Update Book
- **HTTP Method**: `PUT`
- **Path**: `/api/books/{id}`
- **Description**: Updates an existing book by ID.
- **Path Variable**: `id` (Long) - Book ID
- **Request Body**: `CreateBookRequest` (JSON)
```json
{
  "title": "Effective Java (3rd Edition)",
  "author": "Joshua Bloch",
  "available": true
}
```
- **Response**:
  - `200 OK` with updated `BookResponse`.
  - `404 Not Found` if book ID does not exist (`"Book Not Found"`).
  - `400 Bad Request` if validation fails.

---

### 🔹 1.5 Delete Book
- **HTTP Method**: `DELETE`
- **Path**: `/api/books/{id}`
- **Description**: Deletes a book by ID.
- **Path Variable**: `id` (Long) - Book ID
- **Response**:
  - `200 OK` (empty response body).
  - `404 Not Found` if book ID does not exist (`"Book Not Found"`).

---

## 👤 2. Member API Endpoints (`/api/members`)

Base Path: `/api/members`

### 🔹 2.1 Get All Members
- **HTTP Method**: `GET`
- **Path**: `/api/members`
- **Description**: Retrieves a list of all library members.
- **Response**: `200 OK`
- **Sample Response Body**:
```json
[
  {
    "memberId": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phoneNumber": "1234567890"
  }
]
```

---

### 🔹 2.2 Get Member by ID
- **HTTP Method**: `GET`
- **Path**: `/api/members/{memberId}`
- **Description**: Retrieves a member by ID.
- **Path Variable**: `memberId` (Long)
- **Response**:
  - `200 OK` if found.
  - `404 Not Found` if member ID does not exist (`"Member Not Found"`).

---

### 🔹 2.3 Add New Member
- **HTTP Method**: `POST`
- **Path**: `/api/members`
- **Description**: Registers a new library member.
- **Request Body**: `CreateMemberRequest` (JSON)
```json
{
  "name": "Alice Smith",
  "email": "alice.smith@example.com",
  "phoneNumber": "+1-555-0199"
}
```
- **Validation Rules**:
  - `name`: `@NotBlank`
  - `email`: `@NotBlank`, `@Email` (must be valid email format)
  - `phoneNumber`: `@NotBlank`
- **Response**:
  - `200 OK` with created `MemberResponse`.
  - `400 Bad Request` if validation fails.

---

### 2.4 Update Member
- **HTTP Method**: `PUT`
- **Path**: `/api/members/{memberId}`
- **Description**: Updates member details by ID.
- **Path Variable**: `memberId` (Long)
- **Request Body**: `CreateMemberRequest` (JSON)
- **Response**:
  - `200 OK` with updated `MemberResponse`.
  - `404 Not Found` if member ID does not exist (`"Member Not Found"`).
  - `400 Bad Request` if validation fails.

---

### 🔹 2.5 Delete Member
- **HTTP Method**: `DELETE`
- **Path**: `/api/members/{memberId}`
- **Description**: Deletes a member by ID.
- **Path Variable**: `memberId` (Long)
- **Response**:
  - `200 OK` (empty response body).
  - `404 Not Found` if member ID does not exist (`"Member Not Found"`).

---

## 📖 3. Borrow API Endpoints (`/api/borrow`)

Base Path: `/api/borrow`

### 🔹 3.1 Borrow a Book
- **HTTP Method**: `POST`
- **Path**: `/api/borrow`
- **Description**: Borrows an available book for a registered member.
- **Request Body**: `CreateBorrowRequest` (JSON)
```json
{
  "bookId": 1,
  "memberId": 1
}
```
- **Validation Rules**:
  - `bookId`: `@NotNull(message = "BookId is mandatory")`
  - `memberId`: `@NotNull(message = "MemberId is mandatory")`
- **Business Behavior**:
  1. Finds `Member` by `memberId` (throws `MemberNotFoundException` if missing).
  2. Finds `Book` by `bookId` (throws `BookNotFoundException` if missing).
  3. Checks `book.isAvailable()`. If `false`, throws `BookUnavailableException`.
  4. Sets `borrowDate` = today (`LocalDate.now()`).
  5. Sets `dueDate` = today + 14 days (`LocalDate.now().plusDays(14)`).
  6. Sets `returned` = `false`.
  7. Sets `book.setAvailable(false)` and saves to database.
  8. Saves `BorrowRecord`.
- **Response**:
  - `200 OK` returning `ResponseEntity<BorrowResponse>`.
  - `404 Not Found` if member or book does not exist, or if book is unavailable.
  - `400 Bad Request` if validation fails.
- **Sample Response Body (`200 OK`)**:
```json
{
  "borrowId": 1,
  "bookId": 1,
  "memberName": "John Doe",
  "bookTitle": "Clean Code",
  "borrowDate": "2026-07-27",
  "dueDate": "2026-08-10",
  "returned": false
}
```

---

### 🔹 3.2 Return a Book
- **HTTP Method**: `POST`
- **Path**: `/api/borrow/return/{borrowId}`
- **Description**: Marks a borrowed book as returned. If active waitlist reservations exist for this book, the returned copy is automatically locked for the next patron in the FIFO queue with a 48-hour pickup window (`NOTIFIED_READY`); otherwise, the book's `availableCopies` is safely incremented.
- **Path Variable**: `borrowId` (Long) - ID of the borrow record
- **Business Behavior**:
  1. Finds `BorrowRecord` by `borrowId` (throws `BorrowRecordNotFoundException` if missing).
  2. If not already returned:
     - Sets `returned` = `true`.
     - Sets `returnDate` = today (`LocalDate.now()`).
     - Acquires pessimistic lock on `Book`.
     - Checks for `WAITING` reservations:
       - **If reservation found**: sets reservation status to `NOTIFIED_READY` and `pickupDeadline` to `now() + 48 hours`. Does not increment public `availableCopies`.
       - **If no reservation found**: increments `availableCopies` (capped at `totalCopies`).
     - Saves `BorrowRecord`.
  3. Returns updated `BorrowResponse`.
- **Response**:
  - `200 OK` returning `BorrowResponse`.
  - `404 Not Found` if borrow record ID does not exist (`"Borrow Record Not Found"`).
- **Sample Response Body (`200 OK`)**:
```json
{
  "borrowId": 1,
  "bookId": 1,
  "memberName": "John Doe",
  "bookTitle": "Clean Code",
  "borrowDate": "2026-07-27",
  "dueDate": "2026-08-10",
  "returned": true
}
```

---

## 🎟️ 4. Reservation & Waitlist API Endpoints (`/api/reservations`)

Base Path: `/api/reservations`

### 🔹 4.1 Join Waitlist (Create Reservation)
- **HTTP Method**: `POST`
- **Path**: `/api/reservations`
- **Description**: Places a member in the FIFO waitlist queue for an out-of-stock book.
- **Request Body**: `CreateReservationRequest` (JSON)
```json
{
  "bookId": 1,
  "memberId": 2
}
```
- **Validation Rules**:
  - `bookId`: `@NotNull(message = "Book ID is mandatory")`
  - `memberId`: `@NotNull(message = "Member ID is mandatory")`
- **Business Rules**:
  - Requires `book.availableCopies == 0` (returns `400 Bad Request` if copies are available).
  - Rejects duplicate active reservations for the same member (`409 Conflict`).
- **Response**: `201 Created`
```json
{
  "reservationId": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 2,
  "memberName": "Alice Smith",
  "status": "WAITING",
  "reservedAt": "2026-09-29T17:45:00",
  "pickupDeadline": null
}
```

---

### 🔹 4.2 Cancel Reservation
- **HTTP Method**: `POST`
- **Path**: `/api/reservations/{id}/cancel`
- **Description**: Cancels an active reservation. If the reservation was in `NOTIFIED_READY` status, the held copy is automatically reallocated to the next waiting patron, or returned to public inventory if the queue is empty.
- **Path Variable**: `id` (Long) - Reservation ID
- **Response**: `200 OK`
```json
{
  "reservationId": 1,
  "bookId": 1,
  "bookTitle": "Clean Code",
  "memberId": 2,
  "memberName": "Alice Smith",
  "status": "CANCELLED",
  "reservedAt": "2026-09-29T17:45:00",
  "pickupDeadline": null
}
```

---

### 🔹 4.3 Get Reservation by ID
- **HTTP Method**: `GET`
- **Path**: `/api/reservations/{id}`
- **Description**: Retrieves details of a specific reservation.
- **Response**: `200 OK` (or `404 Not Found`)

---

### 🔹 4.4 Get Waitlist Queue for a Book
- **HTTP Method**: `GET`
- **Path**: `/api/reservations/book/{bookId}`
- **Description**: Retrieves all `WAITING` reservations for a book ordered by FIFO priority (`reservedAt ASC`).
- **Response**: `200 OK` (list of `ReservationResponse`)

---

### 🔹 4.5 Get Member Reservations
- **HTTP Method**: `GET`
- **Path**: `/api/reservations/member/{memberId}`
- **Description**: Retrieves all reservations associated with a member ordered chronologically.
- **Response**: `200 OK` (list of `ReservationResponse`)

---

## 💰 5. Fine Management API Endpoints (`/api/fines`)

Base Path: `/api/fines`

### 🔹 5.1 Get All Fines (System-wide Ledger)
- **HTTP Method**: `GET`
- **Path**: `/api/fines`
- **Description**: Retrieves all recorded overdue fines across the entire library system.
- **Authorization**: Required (`Bearer <accessToken>`). Roles: `ADMIN`, `LIBRARIAN`.
- **Response**: `200 OK` (list of `FineResponse`)
- **Sample Response Body**:
```json
[
  {
    "fineId": 1,
    "borrowId": 12,
    "bookId": 3,
    "bookTitle": "Designing Data-Intensive Applications",
    "memberId": 5,
    "memberName": "Alice Smith",
    "amount": 30.00,
    "paid": false,
    "calculatedAt": "2026-09-30T00:00:00",
    "paidAt": null
  }
]
```

---

### 🔹 5.2 Get Member Fines (Least-Privilege Patron Lookup)
- **HTTP Method**: `GET`
- **Path**: `/api/fines/member/{memberId}`
- **Description**: Retrieves all fine records belonging to a specific patron by `memberId`.
- **Authorization**: Required (`Bearer <accessToken>`). Roles: `ADMIN`, `LIBRARIAN`, `ASSISTANT`.
- **Path Variable**: `memberId` (Long)
- **Response**:
  - `200 OK` (list of `FineResponse`)
  - `404 Not Found` if member does not exist (`"Member not found with id: X"`)

---

### 🔹 5.3 Pay / Settle an Overdue Fine
- **HTTP Method**: `POST`
- **Path**: `/api/fines/{fineId}/pay`
- **Description**: Marks an outstanding fine as settled and records the payment timestamp.
- **Authorization**: Required (`Bearer <accessToken>`). Roles: `ADMIN`, `LIBRARIAN`, `ASSISTANT`.
- **Path Variable**: `fineId` (Long)
- **Response**:
  - `200 OK` returning updated `FineResponse` with `paid: true` and `paidAt` timestamp.
  - `404 Not Found` if fine ID does not exist (`"Fine record not found with id: X"`).
  - `400 Bad Request` if fine was already paid (`"Fine has already been settled"`).
- **Sample Response Body**:
```json
{
  "fineId": 1,
  "borrowId": 12,
  "bookId": 3,
  "bookTitle": "Designing Data-Intensive Applications",
  "memberId": 5,
  "memberName": "Alice Smith",
  "amount": 30.00,
  "paid": true,
  "calculatedAt": "2026-09-30T00:00:00",
  "paidAt": "2026-09-30T19:40:00"
}
```

---

### 🔹 5.4 Manually Trigger Overdue Fine Reconciliation
- **HTTP Method**: `POST`
- **Path**: `/api/fines/reconcile`
- **Description**: Manually triggers the overdue fine reconciliation engine on demand (in addition to nightly midnight cron).
- **Authorization**: Required (`Bearer <accessToken>`). Role: `ADMIN`.
- **Response**: `200 OK` (`"Reconciliation completed. Reconciled X overdue loan(s)."`)

---

## ⚠️ 6. Global Error Handling & HTTP Status Codes

### Application Exceptions (Centralized in `GlobalExceptionHandler.java`):

| Exception Class | HTTP Status Code | Response Body Format |
|---|---|---|
| `BookNotFoundException` | `404 NOT_FOUND` | `"Book Not Found"` |
| `MemberNotFoundException` | `404 NOT_FOUND` | `"Member Not Found"` |
| `BookUnavailableException` | `404 NOT_FOUND` | `"Book Not available"` |
| `BorrowRecordNotFoundException` | `404 NOT_FOUND` | `"Borrow Record Not Found"` |
| `ReservationNotFoundException` | `404 NOT_FOUND` | `"Reservation Not Found"` |
| `FineNotFoundException` | `404 NOT_FOUND` | `"Fine Record Not Found"` / Detail message |
| `DuplicateReservationException` | `409 CONFLICT` | `"Member already has an active reservation for this book"` |
| `IllegalStateException` | `400 BAD_REQUEST` | Message string |
| `TokenRefreshException` | `401 UNAUTHORIZED` | `"Failed for [token]: message"` |
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` | `["Error message 1", "Error message 2"]` |

### Security Filter Exceptions (Configured in `SecurityConfig.java`):

| Scenario | HTTP Status Code | Response Body Format |
|---|---|---|
| Missing, invalid, or expired JWT token (`AuthenticationEntryPoint`) | `401 UNAUTHORIZED` | `{"error": "Unauthorized", "message": "Full authentication is required to access this resource"}` |
| Insufficient role / permission (`AccessDeniedHandler`) | `403 FORBIDDEN` | `{"error": "Forbidden", "message": "You do not have permission to access this resource"}` |



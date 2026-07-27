# 🔌 API Documentation -- Library Management System

This document outlines all RESTful API endpoints, request payloads, response formats, validation rules, and error handling behaviors in the Library Management System backend.

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
    "available": true
  },
  {
    "id": 2,
    "title": "Spring Boot in Action",
    "author": "Craig Walls",
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
  "available": true
}
```

---

### 🔹 1.3 Create New Book
- **HTTP Method**: `POST`
- **Path**: `/api/books`
- **Description**: Registers a new book.
- **Request Body**: `CreateBookRequest` (JSON)
```json
{
  "title": "Effective Java",
  "author": "Joshua Bloch",
  "available": true
}
```
- **Validation Rules**:
  - `title`: `@NotBlank` (Cannot be empty or null)
  - `author`: `@NotBlank` (Cannot be empty or null)
- **Response**:
  - `200 OK` with created `BookResponse`.
  - `400 Bad Request` if validation fails (returns list of validation error messages).
- **Sample Response Body (`200 OK`)**:
```json
{
  "id": 3,
  "title": "Effective Java",
  "author": "Joshua Bloch",
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
- **Description**: Marks a borrowed book as returned and sets the book availability back to `true`.
- **Path Variable**: `borrowId` (Long) - ID of the borrow record
- **Business Behavior**:
  1. Finds `BorrowRecord` by `borrowId` (throws `BorrowRecordNotFoundException` if missing).
  2. If not already returned:
     - Sets `returned` = `true`.
     - Sets `returnDate` = today (`LocalDate.now()`).
     - Resets associated `book.setAvailable(true)` and saves `Book`.
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

## ⚠️ 4. Global Error Handling & HTTP Status Codes

Centralized in `GlobalExceptionHandler.java`:

| Exception Class | HTTP Status Code | Response Body Format |
|---|---|---|
| `BookNotFoundException` | `404 NOT_FOUND` | `"Book Not Found"` |
| `MemberNotFoundException` | `404 NOT_FOUND` | `"Member Not Found"` |
| `BookUnavailableException` | `404 NOT_FOUND` | `"Book Not available"` |
| `BorrowRecordNotFoundException` | `404 NOT_FOUND` | `"Borrow Record Not Found"` |
| `MethodArgumentNotValidException` | `400 BAD_REQUEST` | `["Error message 1", "Error message 2"]` |

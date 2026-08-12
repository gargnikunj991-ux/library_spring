# 🔌 REST API Reference & Specification

This document provides complete, production-grade documentation for every RESTful API endpoint exposed by the **Library Management System**.

---

## 🔒 Authentication & Headers Specification

Except for public endpoints (`/auth/login` and `/auth/refresh`), all API requests require a valid JWT Access Token passed in the HTTP `Authorization` header:

```http
Authorization: Bearer <YOUR_JWT_ACCESS_TOKEN>
Content-Type: application/json
```

### Role Matrix Summary
| Endpoint | Method | Permitted Roles | Notes |
|---|---|---|---|
| `/auth/login` | `POST` | `permitAll` (Public) | Authenticates credentials |
| `/auth/register` | `POST` | `ADMIN` | Registers system operators |
| `/auth/refresh` | `POST` | `permitAll` (Public) | Issues fresh access token |
| `/auth/logout` | `POST` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Revokes active refresh token |
| `/api/books` | `GET` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | List all inventory |
| `/api/books/{id}` | `GET` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Get book details |
| `/api/books` | `POST` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Add new book |
| `/api/books/{id}` | `PUT` | `ADMIN`, `LIBRARIAN` | Update book |
| `/api/books/{id}` | `DELETE` | `ADMIN` | Delete book |
| `/api/members` | `GET` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | List all members |
| `/api/members/{memberId}` | `GET` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Get member details |
| `/api/members` | `POST` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Add new member |
| `/api/members/{memberId}` | `PUT` | `ADMIN`, `LIBRARIAN` | Update member |
| `/api/members/{memberId}` | `DELETE` | `ADMIN` | Delete member |
| `/api/borrow` | `POST` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Checkout a book |
| `/api/borrow/return/{borrowId}` | `POST` | `ADMIN`, `LIBRARIAN`, `ASSISTANT` | Return a checked out book |

---

## 🔑 1. Authentication Endpoints (`/auth`)

### 1.1 User Login
- **URL**: `/auth/login`
- **Method**: `POST`
- **Authentication**: None (`permitAll`)
- **Purpose**: Authenticates user credentials and returns a JWT access token and refresh token.

#### Request Headers
```http
Content-Type: application/json
```

#### Request Body (`LoginRequest`)
```json
{
  "username": "admin",
  "password": "AdminPassword123!"
}
```

#### Success Response (`200 OK`)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcyMzQ1MDAwMCwiZXhwIjoxNzIzNDU4NjAwfQ...",
  "refreshToken": "e3a89f92-5d41-477a-b9c1-456789abcdef",
  "tokenType": "Bearer"
}
```

#### Error Responses
- **`400 Bad Request`** (Validation failure):
  ```json
  [
    "Username is mandatory",
    "Password is mandatory"
  ]
  ```
- **`401 Unauthorized`** (Invalid credentials):
  ```json
  {
    "error": "Unauthorized",
    "message": "Bad credentials"
  }
  ```

---

### 1.2 Register New User
- **URL**: `/auth/register`
- **Method**: `POST`
- **Authentication**: Required (`ROLE_ADMIN`)
- **Purpose**: Registers a new user account with BCrypt password hashing.

#### Request Headers
```http
Authorization: Bearer <ADMIN_JWT_TOKEN>
Content-Type: application/json
```

#### Request Body (`RegisterRequest`)
```json
{
  "username": "librarian_john",
  "password": "SecurePassword456!",
  "role": "LIBRARIAN"
}
```
*Allowed `role` values*: `ADMIN`, `LIBRARIAN`, `ASSISTANT`.

#### Success Response (`200 OK`)
```
User registered successfully
```

#### Error Responses
- **`400 Bad Request`** (Validation failure):
  ```json
  [
    "Username is mandatory",
    "Role is mandatory"
  ]
  ```
- **`403 Forbidden`** (Insufficient role permissions):
  ```json
  {
    "error": "Forbidden",
    "message": "You do not have permission to access this resource"
  }
  ```

---

### 1.3 Refresh Access Token
- **URL**: `/auth/refresh`
- **Method**: `POST`
- **Authentication**: None (`permitAll`)
- **Purpose**: Verifies an active refresh token in PostgreSQL and issues a fresh JWT access token.

#### Request Headers
```http
Content-Type: application/json
```

#### Request Body (`RefreshTokenRequest`)
```json
{
  "refreshToken": "e3a89f92-5d41-477a-b9c1-456789abcdef"
}
```

#### Success Response (`200 OK`)
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTcyMzQ2MDAwMCwiZXhwIjoxNzIzNDY4NjAwfQ...",
  "refreshToken": "e3a89f92-5d41-477a-b9c1-456789abcdef",
  "tokenType": "Bearer"
}
```

#### Error Responses
- **`401 Unauthorized`** (Token expired or revoked):
  ```
  Failed for [e3a89f92-5d41-477a-b9c1-456789abcdef]: Refresh token was expired. Please make a new signin request
  ```

---

### 1.4 User Logout
- **URL**: `/auth/logout`
- **Method**: `POST`
- **Authentication**: Required (`ROLE_ADMIN`, `ROLE_LIBRARIAN`, `ROLE_ASSISTANT`)
- **Purpose**: Revokes all active refresh tokens (`revoked = true`) in the database for the authenticated user.

#### Request Headers
```http
Authorization: Bearer <JWT_TOKEN>
```

#### Success Response (`200 OK`)
```
User logged out and refresh token revoked successfully
```

#### Error Responses
- **`401 Unauthorized`** (Missing or invalid token):
  ```json
  {
    "error": "Unauthorized",
    "message": "Full authentication is required to access this resource"
  }
  ```

---

## 📚 2. Book Management Endpoints (`/api/books`)

### 2.1 Get All Books
- **URL**: `/api/books`
- **Method**: `GET`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Purpose**: Retrieves all book inventory records.

#### Success Response (`200 OK`)
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
    "title": "Effective Java",
    "author": "Joshua Bloch",
    "available": false
  }
]
```

---

### 2.2 Get Book by ID
- **URL**: `/api/books/{id}`
- **Method**: `GET`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Path Parameter**: `id` (`Long`) - Unique book ID

#### Success Response (`200 OK`)
```json
{
  "id": 1,
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "available": true
}
```

#### Error Response (`404 Not Found`)
```
Book Not Found
```

---

### 2.3 Add New Book
- **URL**: `/api/books`
- **Method**: `POST`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Purpose**: Registers a new book into inventory.

#### Request Body (`CreateBookRequest`)
```json
{
  "title": "Design Patterns: Elements of Reusable Object-Oriented Software",
  "author": "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides",
  "available": true
}
```

#### Success Response (`200 OK`)
```json
{
  "id": 3,
  "title": "Design Patterns: Elements of Reusable Object-Oriented Software",
  "author": "Erich Gamma, Richard Helm, Ralph Johnson, John Vlissides",
  "available": true
}
```

#### Error Response (`400 Bad Request`)
```json
[
  "Title cannot be blank",
  "Author cannot be blank"
]
```

---

### 2.4 Update Book
- **URL**: `/api/books/{id}`
- **Method**: `PUT`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`)
- **Path Parameter**: `id` (`Long`) - Unique book ID

#### Request Body (`CreateBookRequest`)
```json
{
  "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
  "author": "Robert C. Martin",
  "available": true
}
```

#### Success Response (`200 OK`)
```json
{
  "id": 1,
  "title": "Clean Code: A Handbook of Agile Software Craftsmanship",
  "author": "Robert C. Martin",
  "available": true
}
```

#### Error Responses
- **`404 Not Found`**: `Book Not Found`
- **`403 Forbidden`**: Insufficient permissions (if called by `ASSISTANT`).

---

### 2.5 Delete Book
- **URL**: `/api/books/{id}`
- **Method**: `DELETE`
- **Authentication**: Required (`ROLE_ADMIN`)
- **Path Parameter**: `id` (`Long`) - Unique book ID

#### Success Response (`200 OK`)
*(Empty Body)*

#### Error Responses
- **`404 Not Found`**: `Book Not Found`
- **`403 Forbidden`**: `{"error": "Forbidden", "message": "You do not have permission to access this resource"}`

---

## 👤 3. Member Management Endpoints (`/api/members`)

### 3.1 Get All Members
- **URL**: `/api/members`
- **Method**: `GET`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)

#### Success Response (`200 OK`)
```json
[
  {
    "memberId": 1,
    "name": "Alice Johnson",
    "email": "alice.johnson@example.com",
    "phoneNumber": "+1-555-0143"
  }
]
```

---

### 3.2 Get Member by ID
- **URL**: `/api/members/{memberId}`
- **Method**: `GET`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Path Parameter**: `memberId` (`Long`)

#### Success Response (`200 OK`)
```json
{
  "memberId": 1,
  "name": "Alice Johnson",
  "email": "alice.johnson@example.com",
  "phoneNumber": "+1-555-0143"
}
```

#### Error Response (`404 Not Found`)
```
Member Not Found
```

---

### 3.3 Add New Member
- **URL**: `/api/members`
- **Method**: `POST`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)

#### Request Body (`CreateMemberRequest`)
```json
{
  "name": "Bob Williams",
  "email": "bob.williams@example.com",
  "phoneNumber": "+1-555-0189"
}
```

#### Success Response (`200 OK`)
```json
{
  "memberId": 2,
  "name": "Bob Williams",
  "email": "bob.williams@example.com",
  "phoneNumber": "+1-555-0189"
}
```

#### Error Response (`400 Bad Request`)
```json
[
  "Email must be a well-formed email address",
  "Name cannot be blank"
]
```

---

### 3.4 Update Member
- **URL**: `/api/members/{memberId}`
- **Method**: `PUT`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`)
- **Path Parameter**: `memberId` (`Long`)

#### Request Body (`CreateMemberRequest`)
```json
{
  "name": "Bob Williams Jr.",
  "email": "bob.jr@example.com",
  "phoneNumber": "+1-555-0199"
}
```

#### Success Response (`200 OK`)
```json
{
  "memberId": 2,
  "name": "Bob Williams Jr.",
  "email": "bob.jr@example.com",
  "phoneNumber": "+1-555-0199"
}
```

#### Error Response (`404 Not Found`)
```
Member Not Found
```

---

### 3.5 Delete Member
- **URL**: `/api/members/{memberId}`
- **Method**: `DELETE`
- **Authentication**: Required (`ROLE_ADMIN`)
- **Path Parameter**: `memberId` (`Long`)

#### Success Response (`200 OK`)
*(Empty Body)*

#### Error Responses
- **`404 Not Found`**: `Member Not Found`
- **`403 Forbidden`**: Insufficient permissions.

---

## 📖 4. Borrowing & Return Endpoints (`/api/borrow`)

### 4.1 Borrow a Book
- **URL**: `/api/borrow`
- **Method**: `POST`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Purpose**: Issues a book to a registered member, marks `available = false`, and schedules a 14-day return window.

#### Request Body (`CreateBorrowRequest`)
```json
{
  "bookId": 1,
  "memberId": 1
}
```

#### Success Response (`200 OK`)
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

#### Error Responses
- **`404 Not Found`** (Book already checked out):
  ```
  Book Not available
  ```
- **`404 Not Found`** (Book or Member ID missing):
  ```
  Book Not Found
  ```
  *or*
  ```
  Member Not Found
  ```
- **`400 Bad Request`** (Missing required fields):
  ```json
  [
    "BookId is mandatory",
    "MemberId is mandatory"
  ]
  ```

---

### 4.2 Return a Book
- **URL**: `/api/borrow/return/{borrowId}`
- **Method**: `POST`
- **Authentication**: Required (`ADMIN`, `LIBRARIAN`, `ASSISTANT`)
- **Path Parameter**: `borrowId` (`Long`) - Unique transaction record ID
- **Purpose**: Marks transaction `returned = true`, stamps `returnDate = today`, and resets book `available = true`.

#### Success Response (`200 OK`)
```json
{
  "borrowId": 101,
  "bookId": 1,
  "memberName": "Alice Johnson",
  "bookTitle": "Clean Code",
  "borrowDate": "2026-08-12",
  "dueDate": "2026-08-26",
  "returned": true
}
```

#### Error Response (`404 Not Found`)
```
Borrow Record Not Found
```

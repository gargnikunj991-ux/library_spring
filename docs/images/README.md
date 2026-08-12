# 🖼️ Screenshots, Visual Diagrams & Demo Assets

This directory houses visual architecture diagrams, API request/response flowcharts, ER diagrams, and demo walkthrough snapshots for the **Library Management System**.

---

## 🧭 Visual System Architecture Flow

```
+---------------------------------------------------------------------------------------+
|                                    CLIENT REQUEST                                     |
|               (Postman / cURL / Frontend App / Mobile Client)                         |
+------------------------------------------+--------------------------------------------+
                                           |
                                           | HTTP Requests (with Bearer JWT Token)
                                           v
+---------------------------------------------------------------------------------------+
|                             SPRING SECURITY FILTER CHAIN                              |
|  +---------------------------------------------------------------------------------+  |
|  |                            JwtAuthenticationFilter                              |  |
|  |   - Parses 'Authorization: Bearer <token>' header                               |  |
|  |   - Validates HMAC-SHA256 signature & claims via JwtService                     |  |
|  |   - Loads UserDetails via CustomUserDetailsService                              |  |
|  |   - Sets SecurityContextHolder Authentication token                             |  |
|  +---------------------------------------------------------------------------------+  |
|                                          |                                            |
|                  +-----------------------+-----------------------+                    |
|                  | (Valid Token & Role)                          | (Invalid / 403)    |
|                  v                                               v                    |
|       [ Pass to Controller ]                  [ AuthenticationEntryPoint / ]          |
|                                               [    AccessDeniedHandler     ]          |
+---------------------------------------------------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                                   CONTROLLER LAYER                                    |
|   AuthController  |  BookController  |  MemberController  |  Borrowcontroller        |
|                                                                                       |
|   - Endpoint Routing & HTTP Verbs (GET, POST, PUT, DELETE)                            |
|   - Request Body Validation via Jakarta Bean Validation (@Valid)                      |
|   - Returns standardized ResponseEntity<DTO>                                          |
+------------------------------------------+--------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                                    SERVICE LAYER                                      |
|    AuthService   |   BookService   |   MemberService   |   BorrowService              |
|                                                                                       |
|   - Business Rule Validation (e.g. is book currently available for checkout?)         |
|   - Transaction Management (@Transactional)                                           |
|   - Automatic 14-day due date calculation                                             |
|   - Password Hashing (BCryptPasswordEncoder) & Refresh Token issuance                 |
|   - DTO <-> Entity Mapping                                                            |
+------------------------------------------+--------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                                  REPOSITORY LAYER                                     |
|  UserRepository | BookRepository | MemberRepository | BorrowRecordRepository | TokenRepo|
|                                                                                       |
|   - Spring Data JPA Repositories (JpaRepository<T, ID>)                               |
|   - Hibernate ORM Query Translation & Parameterized SQL Execution                     |
+------------------------------------------+--------------------------------------------+
                                           |
                                           v
+---------------------------------------------------------------------------------------+
|                               POSTGRESQL RELATIONAL DB                                |
|   users  |  refresh_tokens  |  books  |  members  |  borrow_records                   |
+---------------------------------------------------------------------------------------+
```

---

## 📊 Relational Database Schema Visualization

```
+---------------------------+       +---------------------------+       +---------------------------+
|           books           |       |          members          |       |           users           |
+---------------------------+       +---------------------------+       +---------------------------+
| PK  id           (BIGINT) |       | PK  member_id    (BIGINT) |       | PK  id           (BIGINT) |
|     title        (VARCHAR)|       |     name         (VARCHAR)|       |     username     (VARCHAR)|
|     author       (VARCHAR)|       |     email        (VARCHAR)|       |     password     (VARCHAR)|
|     available    (BOOLEAN)|       |     phone_number (VARCHAR)|       |     role         (VARCHAR)|
+-------------+-------------+       +-------------+-------------+       +-------------+-------------+
              | 1                                 | 1                                 | 1
              |                                   |                                   |
              | N                                 | N                                 | N
+-------------v-----------------------------------v-------------+       +-------------v-------------+
|                         borrow_records                        |       |      refresh_tokens       |
+---------------------------------------------------------------+       +---------------------------+
| PK  borrow_id    (BIGINT)                                     |       | PK  id           (BIGINT) |
| FK  book_id      (BIGINT)  ────────► books(id)                |       | FK  user_id      (BIGINT) |
| FK  member_id    (BIGINT)  ────────► members(member_id)       |       |     token        (VARCHAR)|
|     borrow_date  (DATE)                                       |       |     expiry_date  (TIMESTMP|
|     due_date     (DATE)                                       |       |     revoked      (BOOLEAN)|
|     return_date  (DATE, NULL)                                 |       +---------------------------+
|     returned     (BOOLEAN)                                    |
+---------------------------------------------------------------+
```

---

## 📸 Demo Screenshots Guide & Placeholder Reference

When taking screenshots for portfolio presentations or GitHub README displays, capture the following key flows:

1. **Authentication & Token Issuance**:
   - `POST /auth/login` returning `accessToken` and `refreshToken`.
2. **Borrow Book Workflow**:
   - `POST /api/borrow` with payload `{"bookId": 1, "memberId": 1}` returning due date and updated book status.
3. **Availability Conflict Handling**:
   - `POST /api/borrow` for an already borrowed book returning `404 Book Not available`.
4. **Book Return & Availability Reset**:
   - `POST /api/borrow/return/1` restoring book availability to `true`.
5. **Spring Security Enforcement**:
   - Accessing protected endpoints without token returning `401 Unauthorized`.
   - Accessing restricted endpoints with insufficient permissions returning `403 Forbidden`.

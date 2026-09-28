# 🗄️ Database Schema & Data Models -- Library Management System

This document describes the PostgreSQL database schema, JPA entity mapping, table definitions, data types, primary keys, and table relationships.

---

## 📊 Entity Relationship Diagram (ERD)

```
┌─────────────────────────┐             ┌─────────────────────────┐             ┌─────────────────────────┐
│          books          │             │         members         │             │          users          │
├─────────────────────────┤             ├─────────────────────────┤             ├─────────────────────────┤
│ PK  id                  │             │ PK  member_id           │             │ PK  id                  │
│     title               │             │     name                │             │     username (unique)   │
│     author              │             │     email               │             │     password            │
│     available           │             │     phone_number        │             │     role                │
└────────────┬────────────┘             └────────────┬────────────┘             └────────────┬────────────┘
             │                                       │                                       │
             │ 1                                     │ 1                                     │ 1
             │                                       │                                       │
             │ N                                     │ N                                     │ N
┌────────────┴───────────────────────────────────────┴────────────┐             ┌────────────┴────────────┐
│                         borrow_records                          │             │     refresh_tokens      │
├─────────────────────────────────────────────────────────────────┤             ├─────────────────────────┤
│ PK  borrow_id                                                   │             │ PK  id                  │
│ FK  book_id    ──────────────► books(id)                        │             │ FK  user_id ──►users(id)│
│ FK  member_id  ──────────────► members(member_id)               │             │     token (unique)      │
│     borrow_date                                                 │             │     expiry_date         │
│     due_date                                                    │             │     revoked             │
│     return_date                                                 │             └─────────────────────────┘
│     returned                                                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## 🗃️ Table Specifications

### 1. `books` Table

Mapped to Entity: `com.nikunj.library.model.Book`

| Column Name | Data Type | JPA Annotation | Constraints | Description |
|---|---|---|---|---|
| `id` | `BIGINT` | `@Id @GeneratedValue(strategy = IDENTITY)` | Primary Key, Auto-increment | Unique identifier for each book |
| `title` | `VARCHAR(255)` | Field: `title` | NOT NULL | Title of the book |
| `author` | `VARCHAR(255)` | Field: `author` | NOT NULL | Author name |
| `total_copies` | `INTEGER` | Field: `totalCopies` | NOT NULL, Default `1` | Total physical/digital inventory copies |
| `available_copies` | `INTEGER` | Field: `availableCopies` | NOT NULL, Default `1` | Number of currently available copies |

**JPA Mapping (`Book.java`)**:
```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(name = "total_copies", nullable = false)
    private int totalCopies = 1;

    @Column(name = "available_copies", nullable = false)
    private int availableCopies = 1;
}
```

---

### 2. `members` Table

Mapped to Entity: `com.nikunj.library.model.Member`

| Column Name | Data Type | JPA Annotation | Constraints | Description |
|---|---|---|---|---|
| `member_id` | `BIGINT` | `@Id @GeneratedValue(strategy = IDENTITY)` | Primary Key, Auto-increment | Unique identifier for each member |
| `name` | `VARCHAR(255)` | Field: `name` | Nullable | Full name of the member |
| `email` | `VARCHAR(255)` | Field: `email` | Nullable | Email address of the member |
| `phone_number` | `VARCHAR(255)` | Field: `phoneNumber` | Nullable | Contact phone number |

**JPA Mapping (`Member.java`)**:
```java
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    private String name;
    private String email;
    private String phoneNumber;
}
```

---

### 3. `borrow_records` Table

Mapped to Entity: `com.nikunj.library.model.BorrowRecord`

| Column Name | Data Type | JPA Annotation | Constraints | Description |
|---|---|---|---|---|
| `borrow_id` | `BIGINT` | `@Id @GeneratedValue(strategy = IDENTITY)` | Primary Key, Auto-increment | Unique identifier for each borrow record |
| `book_id` | `BIGINT` | `@ManyToOne @JoinColumn(name = "book_id")` | Foreign Key -> `books(id)` | References the borrowed book |
| `member_id` | `BIGINT` | `@ManyToOne @JoinColumn(name = "member_id")` | Foreign Key -> `members(member_id)` | References the borrowing member |
| `borrow_date` | `DATE` | Field: `borrowDate` | `LocalDate` | Date when the book was borrowed |
| `due_date` | `DATE` | Field: `dueDate` | `LocalDate` | Date when book is due to be returned (default: +14 days) |
| `return_date` | `DATE` | Field: `returnDate` | `LocalDate` (Nullable) | Actual date when book was returned |
| `returned` | `BOOLEAN` | Field: `returned` | NOT NULL | `false` when active, `true` when returned |

**JPA Mapping (`BorrowRecord.java`)**:
```java
@Entity
@Table(name = "borrow_records")
public class BorrowRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long borrowId;

    @ManyToOne
    @JoinColumn(name = "book_id")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private boolean returned;
}
```

---

### 4. `users` Table

Mapped to Entity: `com.nikunj.library.model.User`

| Column Name | Data Type | JPA Annotation | Constraints | Description |
|---|---|---|---|---|
| `id` | `BIGINT` | `@Id @GeneratedValue(strategy = IDENTITY)` | Primary Key, Auto-increment | Unique user identifier |
| `username` | `VARCHAR(255)` | `@Column(nullable = false, unique = true)` | UNIQUE, NOT NULL | User login username |
| `password` | `VARCHAR(255)` | `@Column(nullable = false)` | NOT NULL | Hashed / User password |
| `role` | `VARCHAR(255)` | `@Enumerated(EnumType.STRING) @Column(nullable = false)` | NOT NULL | User Role (`ADMIN`, `LIBRARIAN`, `ASSISTANT`) |

**JPA Mapping (`User.java`)**:
```java
@Entity
@Table(name = "users")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    public enum Role {
        ADMIN,
        LIBRARIAN,
        ASSISTANT
    }

    public Long getId() { return id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
}
```

---

### 5. `refresh_tokens` Table

Mapped to Entity: `com.nikunj.library.model.RefreshToken`

| Column Name | Data Type | JPA Annotation | Constraints | Description |
|---|---|---|---|---|
| `id` | `BIGINT` | `@Id @GeneratedValue(strategy = IDENTITY)` | Primary Key, Auto-increment | Unique identifier for the refresh token record |
| `token` | `VARCHAR(255)` | `@Column(nullable = false, unique = true)` | UNIQUE, NOT NULL | Refresh token string |
| `expiry_date` | `TIMESTAMP WITH TIME ZONE` | `@Column(nullable = false)` | NOT NULL | Expiration timestamp (`Instant`) |
| `user_id` | `BIGINT` | `@ManyToOne @JoinColumn(name = "user_id", nullable = false)` | Foreign Key -> `users(id)` | Associated user entity reference |
| `revoked` | `BOOLEAN` | Field: `revoked` | NOT NULL | Flag indicating if token is revoked |

**JPA Mapping (`RefreshToken.java`)**:
```java
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private boolean revoked;
}
```

---

## ⚙️ JPA Configuration Notes (`application.properties`)

- `spring.jpa.hibernate.ddl-auto=update`: Hibernate automatically synchronizes Java entity definitions with PostgreSQL database tables.
- `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`: Configures Hibernate dialect for PostgreSQL compatibility.



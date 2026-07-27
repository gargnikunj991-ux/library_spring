# 🗄️ Database Schema & Data Models -- Library Management System

This document describes the PostgreSQL database schema, JPA entity mapping, table definitions, data types, primary keys, and table relationships.

---

## 📊 Entity Relationship Diagram (ERD)

```
┌─────────────────────────┐             ┌─────────────────────────┐
│          books          │             │         members         │
├─────────────────────────┤             ├─────────────────────────┤
│ PK  id                  │             │ PK  member_id           │
│     title               │             │     name                │
│     author              │             │     email               │
│     available           │             │     phone_number        │
└────────────┬────────────┘             └────────────┬────────────┘
             │                                       │
             │ 1                                     │ 1
             │                                       │
             │ N                                     │ N
┌────────────┴───────────────────────────────────────┴────────────┐
│                         borrow_records                          │
├─────────────────────────────────────────────────────────────────┤
│ PK  borrow_id                                                   │
│ FK  book_id    ──────────────► books(id)                        │
│ FK  member_id  ──────────────► members(member_id)               │
│     borrow_date                                                 │
│     due_date                                                    │
│     return_date                                                 │
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
| `title` | `VARCHAR(255)` | Field: `title` | Nullable | Title of the book |
| `author` | `VARCHAR(255)` | Field: `author` | Nullable | Author name |
| `available` | `BOOLEAN` | Field: `available` | NOT NULL | `true` if available for borrow, `false` if borrowed |

**JPA Mapping (`Book.java`)**:
```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    private String author;
    private boolean available;
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

## ⚙️ JPA Configuration Notes (`application.properties`)

- `spring.jpa.hibernate.ddl-auto=update`: Hibernate automatically synchronizes Java entity definitions with PostgreSQL database tables.
- `spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect`: Configures Hibernate dialect for PostgreSQL compatibility.

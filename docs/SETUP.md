# 🚀 Local Environment Setup & Installation Guide

This guide walks you through setting up and running the **Library Management System** on a fresh machine from scratch.

---

## 📋 Prerequisites & Tools

Ensure you have the following installed on your operating system:

| Tool | Recommended Version | Verification Command | Download Link |
|---|---|---|---|
| **Java Development Kit (JDK)** | Java 21 (LTS) | `java -version` | [Adoptium Temurin 21](https://adoptium.net/) |
| **PostgreSQL Database** | 15 or 16+ | `psql --version` | [PostgreSQL Downloads](https://www.postgresql.org/download/) |
| **Apache Maven** (Optional) | 3.9+ | `mvn -version` | *(Maven Wrapper `./mvnw` is included in repo)* |
| **Git** | Latest | `git --version` | [Git SCM](https://git-scm.com/) |
| **REST Client** (Optional) | Any | - | [Postman](https://www.postman.com/) or VS Code REST Client |

---

## 🛠️ Step-by-Step Installation

### Step 1: Clone the Repository
```bash
git clone https://github.com/gargnikunj991-ux/library.git
cd library
```

---

### Step 2: Set Up PostgreSQL Database

1. Open your terminal or `psql` shell:
   ```bash
   psql -U postgres
   ```
2. Create the `library` database:
   ```sql
   CREATE DATABASE library;
   ```
3. Verify the database exists:
   ```sql
   \l
   ```
4. Exit `psql`:
   ```sql
   \q
   ```

> [!NOTE]
> Hibernate will automatically generate all tables (`users`, `refresh_tokens`, `books`, `members`, `borrow_records`) upon application startup via `spring.jpa.hibernate.ddl-auto=update`.

---

### Step 3: Configure Environment Variables

1. Copy the provided `.env.example` file to `.env`:
   - **Linux / macOS**:
     ```bash
     cp .env.example .env
     ```
   - **Windows (PowerShell)**:
     ```powershell
     Copy-Item .env.example .env
     ```
2. Open `.env` in your text editor and verify the credentials:
   ```properties
   DB_URL=jdbc:postgresql://localhost:5432/library
   DB_USERNAME=postgres
   DB_PASSWORD=your_actual_postgres_password
   JWT_SECRET=5F05gEkmG5Gxi6GHqehXUFlsusNdoO0tXnwuK1iUVpQ=
   JWT_REFRESH_EXPIRATION_MS=604800000
   SERVER_PORT=8080
   ```

---

### Step 4: Build & Compile the Project

Use Maven or the bundled Maven Wrapper to clean, resolve dependencies, and compile:

- **Windows (PowerShell / CMD)**:
  ```powershell
  .\mvnw.cmd clean test-compile
  ```
- **Linux / macOS**:
  ```bash
  chmod +x mvnw
  ./mvnw clean test-compile
  ```

---

### Step 5: Start the Application

#### Option A: Run via Maven Plugin (Development Mode)
```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

#### Option B: Build and Run Standalone JAR (Production Mode)
```bash
# Package executable JAR
.\mvnw.cmd clean package -DskipTests

# Run JAR
java -jar target/library-0.0.1-SNAPSHOT.jar
```

The server will start listening on `http://localhost:8080`.

---

## 🧪 First-Time Verification & Quick Smoke Test

### 1. Register the Initial Admin User
Because `/auth/register` is protected in standard operation, you can seed your initial administrator account:
```bash
curl -X POST http://localhost:8080/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "AdminPassword123!", "role": "ADMIN"}'
```

### 2. Login to Receive JWT Tokens
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "AdminPassword123!"}'
```
*Output*:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890",
  "tokenType": "Bearer"
}
```

### 3. Add a Book Using the Access Token
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title": "Clean Architecture", "author": "Robert C. Martin", "available": true}'
```

---

## 🔧 Common Setup Pitfalls & Troubleshooting

### ❌ 1. `Connection to localhost:5432 refused`
- **Cause**: PostgreSQL service is stopped or not running on port 5432.
- **Solution**:
  - **Windows**: Open `services.msc`, locate `postgresql-x64-16`, and click **Start**.
  - **Linux**: `sudo systemctl start postgresql`
  - **macOS (Homebrew)**: `brew services start postgresql@16`

### ❌ 2. `FATAL: password authentication failed for user "postgres"`
- **Cause**: The password specified in `.env` under `DB_PASSWORD` does not match your local PostgreSQL `postgres` user password.
- **Solution**: Update `DB_PASSWORD` in `.env` or reset your PostgreSQL password via `ALTER USER postgres WITH PASSWORD 'newpassword';`.

### ❌ 3. `Web server failed to start. Port 8080 was already in use.`
- **Cause**: Another service or IDE process is holding port 8080.
- **Solution**:
  - Kill existing process:
    - **Windows**: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`
    - **Linux/macOS**: `lsof -i :8080` then `kill -9 <PID>`
  - Or specify another port in `.env`: `SERVER_PORT=8081`.

### ❌ 4. `The specified key byte array is confirmed to be ... bits, but HMAC-SHA256 requires at least 256 bits`
- **Cause**: `JWT_SECRET` in `.env` is either empty or too short.
- **Solution**: Use a 256-bit base64 secret (at least 32 bytes) like the default provided in `.env.example`.

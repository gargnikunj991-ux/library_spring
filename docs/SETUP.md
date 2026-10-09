# 🚀 Local Environment Setup & Installation Guide -- LibroSphere

This guide walks you through setting up and running the **Library Management System (LibroSphere)** on a fresh machine from scratch.

---

## 📋 Prerequisites & Tools

Ensure you have the following installed on your operating system:

| Tool | Recommended Version | Verification Command | Download Link |
|---|---|---|---|
| **Java Development Kit (JDK)** | Java 21 (LTS) | `java -version` | [Adoptium Temurin 21](https://adoptium.net/) |
| **PostgreSQL Database** | 15 or 16+ | `psql --version` | [PostgreSQL Downloads](https://www.postgresql.org/download/) |
| **Docker & Docker Compose** (Optional) | Latest | `docker compose version` | [Docker Desktop](https://www.docker.com/) |
| **Apache Maven** (Optional) | 3.9+ | `mvn -version` | *(Maven Wrapper `./mvnw` is included in repo)* |
| **Git** | Latest | `git --version` | [Git SCM](https://git-scm.com/) |
| **REST Client** (Optional) | Any | - | [Postman](https://www.postman.com/) or Swagger UI |

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
3. Exit `psql`:
   ```sql
   \q
   ```

> [!NOTE]
> Hibernate will automatically generate all 7 tables (`users`, `refresh_tokens`, `books`, `members`, `borrow_records`, `book_reservations`, `fine_records`) and composite B-tree indexes upon application startup via `spring.jpa.hibernate.ddl-auto=update`.

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
2. Open `.env` and configure your credentials:
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

Use Maven or the bundled Maven Wrapper to clean and compile test sources:

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

#### Option B: Run via Docker Compose (Zero Local DB Setup)
If you have Docker installed, you don't even need local PostgreSQL:
```bash
docker-compose up -d --build
```
This spins up PostgreSQL 16 Alpine and the backend in isolated containers.

#### Option C: Build and Run Standalone JAR
```bash
# Package executable JAR
.\mvnw.cmd clean package -DskipTests

# Run JAR
java -jar target/library-0.0.1-SNAPSHOT.jar
```

The server will start listening on **`http://localhost:8080`**.  
Interactive Swagger UI is available at **`http://localhost:8080/swagger-ui/index.html`**.

---

## 🧪 First-Time Verification & Quick Smoke Test

### 1. Default Admin User (Auto-Seeded)
The application automatically seeds a default `ADMIN` user on first boot via `DataInitializer`:
- **Username**: `admin`
- **Password**: `admin123`

### 2. Login to Receive JWT Access and Refresh Tokens
```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "admin123"}'
```
*Output*:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "4a7e9b21-8c34-4d82-bcf2-9e1234567890",
  "tokenType": "Bearer"
}
```

### 3. Add a Multi-Copy Book Using the Access Token
```bash
curl -X POST http://localhost:8080/api/books \
  -H "Authorization: Bearer <YOUR_ACCESS_TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"title": "Clean Architecture", "author": "Robert C. Martin", "totalCopies": 3}'
```

---

## 🔧 Common Setup Pitfalls & Troubleshooting

### ❌ 1. `Connection to localhost:5432 refused`
- **Cause**: PostgreSQL service is stopped or not running on port 5432.
- **Solution**:
  - **Windows**: Open `services.msc`, locate `postgresql-x64-16`, and click **Start**.
  - **Linux**: `sudo systemctl start postgresql`
  - **macOS (Homebrew)**: `brew services start postgresql@16`
  - Or use Docker Compose: `docker-compose up -d`.

### ❌ 2. `FATAL: password authentication failed for user "postgres"`
- **Cause**: The password specified in `.env` under `DB_PASSWORD` does not match your PostgreSQL server.
- **Solution**: Update `DB_PASSWORD` in `.env` or reset PostgreSQL password via `ALTER USER postgres WITH PASSWORD 'newpassword';`.

### ❌ 3. `Web server failed to start. Port 8080 was already in use.`
- **Cause**: Another service or IDE process is holding port 8080.
- **Solution**:
  - Windows: `netstat -ano | findstr :8080` then `taskkill /PID <PID> /F`
  - Linux/macOS: `lsof -i :8080` then `kill -9 <PID>`
  - Or change port in `.env`: `SERVER_PORT=8081`.

### ❌ 4. `The specified key byte array is confirmed to be ... bits, but HMAC-SHA256 requires at least 256 bits`
- **Cause**: `JWT_SECRET` in `.env` is too short or empty.
- **Solution**: Use the 256-bit base64 secret key provided in `.env.example`.

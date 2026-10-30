# ABL_Auth Microservice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Membangun microservice baru `ABL_Auth` dengan endpoint register, login (JWT), dan validate token, serta mendaftarkannya ke Eureka dan API Gateway.

**Architecture:** Microservice Spring Boot mandiri port 8084 dengan H2 database in-memory, password hashing BCrypt, pembuatan/validasi JWT stateless via JJWT, discovery via Eureka Client, dan perutean via Spring Cloud Gateway.

**Tech Stack:** Java 21, Spring Boot 4.1.1, Spring Cloud 2025.1.3 (Eureka Client), Spring Data JPA, H2 Database, Spring Security Crypto, JJWT 0.12.6.

**Spec:** `docs/superpowers/specs/2026-10-23-auth-service-design.md`

## Global Constraints
- Java Version: 21
- Spring Boot Starter Parent: 4.1.1
- Spring Cloud: 2025.1.3
- Service port: 8084
- Service name: `auth`
- Base package: `com.ihsan.auth`

## Review Focus
1. Password tidak pernah disimpan dalam bentuk plaintext di database.
2. Username duplikat saat registrasi menghasilkan HTTP 400 Bad Request, bukan 500.
3. Login dengan kredensial salah menghasilkan HTTP 401 Unauthorized.
4. Token malformed/expired saat validasi menghasilkan HTTP 401 Unauthorized.
5. Endpoint `/api/auth/**` dapat di-route oleh `ABL_Gateway` ke instance `auth`.

---

### Task 1: Scaffolding Project ABL_Auth

**Files:**
- Create: `ABL_Auth/pom.xml`
- Create: `ABL_Auth/src/main/resources/application.properties`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/AuthApplication.java`
- Copy wrapper: `ABL_Auth/mvnw`, `ABL_Auth/mvnw.cmd`, `ABL_Auth/.mvn/`

**Interfaces:**
- Produces: Spring Boot context for `auth` service running on port 8084.

- [ ] **Step 1: Create project directory and copy maven wrapper from ABL_Product**
- [ ] **Step 2: Create `ABL_Auth/pom.xml` with dependencies (webmvc, jpa, h2, eureka-client, spring-security-crypto, jjwt)**
- [ ] **Step 3: Create `ABL_Auth/src/main/resources/application.properties` configuring port 8084, eureka url, and h2 config**
- [ ] **Step 4: Create `ABL_Auth/src/main/java/com/ihsan/auth/AuthApplication.java`**
- [ ] **Step 5: Verify build with `./mvnw clean compile`**

---

### Task 2: User Entity and UserRepository

**Files:**
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/entity/User.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/repository/UserRepository.java`
- Test: `ABL_Auth/src/test/java/com/ihsan/auth/repository/UserRepositoryTest.java`

**Interfaces:**
- Consumes: JPA / H2
- Produces: `UserRepository.findByUsername(String username): Optional<User>`, `UserRepository.existsByUsername(String username): boolean`

- [ ] **Step 1: Write failing repository test for User entity persistence and custom queries**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Create `User` entity and `UserRepository` interface**
- [ ] **Step 4: Run test to verify it passes**

---

### Task 3: Security Config and JwtUtil

**Files:**
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/util/JwtUtil.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/config/SecurityConfig.java`
- Test: `ABL_Auth/src/test/java/com/ihsan/auth/util/JwtUtilTest.java`

**Interfaces:**
- Consumes: `io.jsonwebtoken`
- Produces: `JwtUtil.generateToken(String username, String role): String`, `JwtUtil.validateToken(String token): boolean`, `JwtUtil.extractUsername(String token): String`, `JwtUtil.extractRole(String token): String`, `BCryptPasswordEncoder` bean.

- [ ] **Step 1: Write failing unit test for `JwtUtil` (generate, extract claims, validate expired/invalid)**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement `JwtUtil` and `SecurityConfig` with password encoder bean**
- [ ] **Step 4: Run test to verify it passes**

---

### Task 4: DTOs and AuthService

**Files:**
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/dto/RegisterRequest.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/dto/LoginRequest.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/dto/AuthResponse.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/dto/ValidateResponse.java`
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/service/AuthService.java`
- Test: `ABL_Auth/src/test/java/com/ihsan/auth/service/AuthServiceTest.java`

**Interfaces:**
- Consumes: `UserRepository`, `JwtUtil`, `BCryptPasswordEncoder`
- Produces: `AuthService.register(RegisterRequest): AuthResponse`, `AuthService.login(LoginRequest): AuthResponse`, `AuthService.validate(String bearerToken): ValidateResponse`

- [ ] **Step 1: Write failing unit tests for `AuthService` (register success, duplicate fail, login success, invalid password fail, validate)**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement DTO classes and `AuthService`**
- [ ] **Step 4: Run test to verify it passes**

---

### Task 5: AuthController & REST Endpoints

**Files:**
- Create: `ABL_Auth/src/main/java/com/ihsan/auth/controller/AuthController.java`
- Test: `ABL_Auth/src/test/java/com/ihsan/auth/controller/AuthControllerTest.java`

**Interfaces:**
- Consumes: `AuthService`
- Produces: REST endpoints `/api/auth/register`, `/api/auth/login`, `/api/auth/validate`

- [ ] **Step 1: Write WebMvc test for `AuthController` verifying HTTP statuses (201, 200, 400, 401)**
- [ ] **Step 2: Run test to verify it fails**
- [ ] **Step 3: Implement `AuthController`**
- [ ] **Step 4: Run test to verify it passes**

---

### Task 6: Gateway & run-all.sh Integration

**Files:**
- Modify: `ABL_Gateway/src/main/resources/application.yaml`
- Modify: `run-all.sh`

**Interfaces:**
- Consumes: `ABL_Auth` (port 8084, eureka name `auth`)
- Produces: Gateway routing `/api/auth/**` -> `lb://auth` and runner startup for `auth`

- [ ] **Step 1: Add route `AUTH` to `ABL_Gateway/src/main/resources/application.yaml`**
- [ ] **Step 2: Add `"auth|ABL_Auth|8084|"` entry to `run-all.sh`**
- [ ] **Step 3: Build `ABL_Auth` package via `./mvnw package -DskipTests` to confirm packaging**

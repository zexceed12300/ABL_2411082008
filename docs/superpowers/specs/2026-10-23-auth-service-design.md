# Design Specification: ABL_Auth Service

**Date:** 2026-10-23  
**Status:** Approved  
**Author:** AI Assistant / Ihsan  

## 1. Overview
`ABL_Auth` adalah microservice mandiri berbasis Spring Boot yang menangani registrasi akun, autentikasi pengguna (login), dan validasi token JWT. Service ini terintegrasi ke dalam ekosistem Spring Cloud ABL yang sudah ada (Eureka discovery dan Gateway routing).

## 2. Goals & Non-Goals
### Goals
- Menyediakan endpoint REST:
  - `POST /api/auth/register`: Mendaftarkan user baru dengan password yang di-hash menggunakan BCrypt.
  - `POST /api/auth/login`: Autentikasi kredensial user dan menghasilkan signed JWT token.
  - `GET /api/auth/validate`: Memvalidasi keabsahan token JWT dan mengembalikan identitas user (username, role).
- Berjalan di port `8084` dan mendaftarkan diri ke Eureka Server (`http://localhost:8761/eureka/`).
- Terhubung dengan API Gateway (`ABL_Gateway`) via route `/api/auth/**` -> `lb://auth`.
- Terintegrasi dalam runner script `run-all.sh`.
- Menggunakan database H2 mandiri (in-memory) untuk menyimpan kredensial user.

### Non-Goals
- Tidak mengimplementasikan OAuth2 / OpenID Connect authorization server kompleks.
- Tidak menyertakan refresh token rotation atau session revocation storage (stateless JWT).
- Filter validasi JWT di gateway untuk memblokir rute lain ditunda untuk iterasi berikutnya sesuai kesepakatan scope.

## 3. Architecture & Data Model

### Technology Stack
- Java 21
- Spring Boot 4.1.1 (selaras dengan service existing)
- Spring Cloud 2025.1.3 (Netflix Eureka Client)
- Spring Data JPA + H2 Database
- `spring-security-crypto` untuk BCrypt hashing
- `io.jsonwebtoken` (JJWT 0.12.6) untuk token generation & validation

### Data Model
Tabel `users`:
- `id` (Long, PK, Auto Increment)
- `username` (VARCHAR, Unique, Not Null)
- `password` (VARCHAR, Not Null, BCrypt hashed)
- `role` (VARCHAR, Not Null, default 'USER')
- `created_at` (TIMESTAMP)

### API Endpoints
1. `POST /api/auth/register`
   - Request Body: `{"username": "...", "password": "...", "role": "USER"}`
   - Response 201: `{"id": 1, "username": "...", "role": "USER", "message": "User registered successfully"}`
   - Response 400: Error jika username sudah ada atau input tidak valid.

2. `POST /api/auth/login`
   - Request Body: `{"username": "...", "password": "..."}`
   - Response 200: `{"token": "<JWT_STRING>", "type": "Bearer", "username": "...", "role": "..."}`
   - Response 401: Invalid credentials.

3. `GET /api/auth/validate`
   - Request Header: `Authorization: Bearer <token>`
   - Response 200: `{"valid": true, "username": "...", "role": "..."}`
   - Response 401: `{"valid": false, "message": "Invalid or expired token"}`

## 4. Integration
- Eureka Service Name: `auth`
- Gateway Route:
  ```yaml
  - id: AUTH
    uri: lb://auth
    predicates:
      - Path=/api/auth/**
  ```
- `run-all.sh`:
  Tambah entry `"auth|ABL_Auth|8084|"`.

# IAM Service

> **Identity & Access Management** microservice for the Car Rental System (CRS).  
> Handles user authentication, authorization, role/permission management, and inter-service identity verification.

---

## Table of Contents

- [Overview](#overview)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Configuration](#configuration)
  - [Running the Service](#running-the-service)
- [API Reference](#api-reference)
  - [Authentication](#authentication-apiv1auth)
  - [User Management (Admin)](#user-management-apiv1adminusers)
  - [Role Management](#role-management-apiv1roles)
  - [Permission Management](#permission-management-apiv1permissions)
  - [Internal APIs (Service-to-Service)](#internal-apis-internalusers)
- [Data Models](#data-models)
- [Security](#security)
- [Email / OTP Flow](#email--otp-flow)
- [Environment Variables](#environment-variables)

---

## Overview

The IAM Service is the **single source of truth for user identities** across the Car Rental System. It is responsible for:

- **Authentication** — Login/Register with JWT-based stateless sessions
- **Authorization** — Role-Based Access Control (RBAC) with fine-grained permissions
- **Token Lifecycle** — Access token issuance, refresh, and revocation
- **Password Management** — Change password, forgot/reset password via email OTP
- **Admin User Management** — Full CRUD on users, roles, and permissions
- **Internal Identity API** — Lightweight endpoint for other microservices to verify/fetch user info without re-authenticating

All other services (**booking-service**, **car-management**) call this service's `/internal/**` endpoints to resolve user identities.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.4.2 |
| Security | Spring Security 6 + JJWT 0.11.5 |
| Database | PostgreSQL |
| ORM | Spring Data JPA / Hibernate |
| Email | Spring Mail + Thymeleaf (Gmail SMTP) |
| API Docs | SpringDoc OpenAPI 2.8.3 (Swagger UI) |
| Build | Maven |

---

## Architecture

```
┌──────────────────────────────────────────────────────┐
│                    IAM Service (:8080)                │
│                                                      │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │  Auth API   │  │  Admin API   │  │ Internal   │  │
│  │/api/v1/auth │  │/api/v1/admin │  │ /internal  │  │
│  └──────┬──────┘  └──────┬───────┘  └─────┬──────┘  │
│         │                │                │          │
│  ┌──────▼────────────────▼────────────────▼──────┐   │
│  │             Service Layer                     │   │
│  │  AuthenticationService  │  AdminUserService   │   │
│  │  RoleService            │  PermissionService  │   │
│  └─────────────────────────────────┬─────────────┘   │
│                                    │                 │
│  ┌─────────────────────────────────▼─────────────┐   │
│  │           Repository Layer (JPA)              │   │
│  │  UserRepository │ RoleRepository │ ...        │   │
│  └─────────────────────────────────┬─────────────┘   │
│                                    │                 │
└────────────────────────────────────┼─────────────────┘
                                     │
                              ┌──────▼──────┐
                              │ PostgreSQL   │
                              │iam_service_db│
                              └─────────────┘
```

**Communication pattern:**
- **Inbound**: API Gateway routes `/api/v1/auth/**`, `/api/v1/admin/**`, `/api/v1/roles/**`, `/api/v1/permissions/**` to this service
- **Inbound (direct)**: `booking-service` calls `/internal/users/**` directly (bypassing the gateway) — no JWT required

---

## Getting Started

### Prerequisites

- Java 21+
- Maven 3.8+
- PostgreSQL 15+
- A Gmail account with App Password enabled (for OTP emails)

### Configuration

Create the database before starting:

```sql
CREATE DATABASE iam_service_db;
```

All configuration lives in `src/main/resources/application.yml`. Key sections to set up:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/iam_service_db
    username: <your_db_user>
    password: <your_db_password>

  mail:
    username: <your_gmail_address>
    password: <your_gmail_app_password>   # 16-character App Password, not your real password

jwt:
  secret-key: CceAYTEK9XB5w/ibsdi5pvuNynAwNfDvGI7y+BD3ziA=  # Change in production!
  access-token-expiration: 86400000     # 1 day (ms)
  refresh-token-expiration: 604800000   # 7 days (ms)

server:
  port: 8080
```

> ⚠️ **Security Notice**: The `jwt.secret-key` above is a development default. **Always replace it** with a cryptographically strong 256-bit key in any non-development environment.

### Running the Service

```bash
# Navigate to the service directory
cd iam-service

# Build and run
mvn spring-boot:run

# Or build first, then run the JAR
mvn clean package -DskipTests
java -jar target/iam-service-*.jar
```

The service starts on **port 8080**.

- **Swagger UI**: http://localhost:8080/swagger-ui/index.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs

---

## API Reference

> **Base path through API Gateway**: `http://localhost:8888/api/v1`  
> **Direct base path**: `http://localhost:8080/api/v1`

### Authentication `/api/v1/auth`

| Method | Path | Auth Required | Description |
|--------|------|:---:|-------------|
| `POST` | `/auth/register` | ✗ | Register a new user account |
| `POST` | `/auth/login` | ✗ | Login and receive access + refresh tokens |
| `POST` | `/auth/introspect` | ✗ | Validate a JWT token, returns validity + decoded claims |
| `POST` | `/auth/refresh-token` | ✗ | Exchange a valid refresh token for a new access token |
| `POST` | `/auth/logout` | ✗ | Invalidate a refresh token (server-side revocation) |
| `GET` | `/auth/profile` | ✔ JWT | Get the currently authenticated user's profile |
| `POST` | `/auth/change-password` | ✔ JWT | Change own password (requires current password) |
| `POST` | `/auth/forgot-password` | ✗ | Step 1: Send 6-digit OTP to registered email |
| `POST` | `/auth/verify-reset-code` | ✗ | Step 2: Verify the OTP code is valid |
| `POST` | `/auth/reset-password` | ✗ | Step 3: Set a new password using the verified OTP |

**`POST /auth/login` — Request body:**
```json
{
  "email": "user@example.com",
  "password": "yourpassword"
}
```

**`POST /auth/login` — Response:**
```json
{
  "code": 1000,
  "message": "Success",
  "data": {
    "accessToken": "eyJhbGci...",
    "refreshToken": "eyJhbGci...",
    "email": "user@example.com",
    "role": "CUSTOMER"
  }
}
```

**`POST /auth/register` — Request body:**
```json
{
  "email": "user@example.com",
  "password": "StrongPass123!",
  "fullName": "Nguyen Van A",
  "phone": "0901234567",
  "roleId": 2
}
```

---

### User Management `/api/v1/admin/users`

> Requires `ROLE_ADMIN`. All endpoints return paginated `PageResponse<AdminUserResponse>` where applicable.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/admin/users` | List all users (paginated, sortable by `createdAt`) |
| `GET` | `/admin/users/search` | Search users by keyword, roleId, isActive, isDeleted |
| `GET` | `/admin/users/{userId}` | Get a single user by UUID |
| `POST` | `/admin/users` | Create a new user with a specific role |
| `PUT` | `/admin/users/{userId}` | Update user details (name, phone, gender, dob) |
| `DELETE` | `/admin/users/{userId}` | Soft-delete a user (`isDeleted = true`) |
| `PATCH` | `/admin/users/{userId}/activate` | Re-activate a deactivated user |
| `PATCH` | `/admin/users/{userId}/deactivate` | Deactivate a user (blocks login) |
| `POST` | `/admin/users/{userId}/reset-password` | Force-reset a user's password |
| `GET` | `/admin/users/stats` | Aggregated user statistics |

**`GET /admin/users/stats` — Response:**
```json
{
  "data": {
    "totalUsers": 120,
    "totalCustomers": 98,
    "newCustomers": 12,
    "totalDrivers": 15
  }
}
```

**`GET /admin/users` — Query parameters:**
| Param | Type | Default | Description |
|---|---|---|---|
| `page` | int | `0` | Page number (0-indexed) |
| `size` | int | `10` | Page size |
| `sort` | string | `createdAt` | Field to sort by |

---

### Role Management `/api/v1/roles`

> Requires `ROLE_ADMIN` or `MANAGE_ROLES` permission.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/roles` | List all roles |
| `GET` | `/roles/active` | List only active roles |
| `GET` | `/roles/{id}` | Get role by ID |
| `GET` | `/roles/name/{name}` | Get role by name (e.g. `CUSTOMER`, `DRIVER`) |
| `POST` | `/roles` | Create a new role |
| `PUT` | `/roles/{id}` | Update role name/description |
| `PATCH` | `/roles/{id}/activate` | Activate a role |
| `PATCH` | `/roles/{id}/deactivate` | Deactivate a role |
| `POST` | `/roles/{id}/permissions` | Assign a set of permissions to a role |
| `DELETE` | `/roles/{id}/permissions` | Remove permissions from a role |
| `DELETE` | `/roles/{id}` | Delete a role |

---

### Permission Management `/api/v1/permissions`

> Requires `ROLE_ADMIN` or `MANAGE_PERMISSIONS` permission.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/permissions` | List all permissions |
| `GET` | `/permissions/{id}` | Get permission by string ID |
| `POST` | `/permissions` | Create a new permission |
| `PUT` | `/permissions/{id}` | Update a permission |
| `DELETE` | `/permissions/{id}` | Delete a permission |

---

### Internal APIs `/internal/users`

> **No JWT required.** These endpoints are on a trusted, internal path used only by other microservices (`booking-service`, etc.). They should **never be exposed** to external clients.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/internal/users/{userId}` | Fetch user info (fullName, email, phone, role) by UUID |
| `GET` | `/internal/users/{userId}/exists` | Returns `true`/`false` — fast user existence check |
| `GET` | `/internal/users/role/{roleName}` | List all active users with a given role name |

---

## Data Models

### `User`
| Field | Type | Notes |
|---|---|---|
| `id` | `UUID` | Auto-generated, primary key |
| `email` | `String` | Unique, used as login identifier |
| `passwordHash` | `String` | BCrypt-encoded |
| `fullName` | `String` | |
| `phone` | `String` | |
| `gender` | `String` | |
| `dob` | `LocalDate` | Date of birth |
| `isActive` | `boolean` | `false` = login blocked |
| `isDeleted` | `boolean` | Soft delete flag |
| `roles` | `Set<Role>` | ManyToMany |
| `createdAt` | `LocalDateTime` | Auto-set on insert |

### `Role`
| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | Auto-generated |
| `name` | `String` | e.g. `ADMIN`, `STAFF`, `CUSTOMER`, `DRIVER` |
| `isActive` | `boolean` | |
| `permissions` | `Set<Permission>` | ManyToMany |

### `RefreshToken`
| Field | Type | Notes |
|---|---|---|
| `id` | `Long` | |
| `token` | `String` | The raw refresh token string |
| `expiresAt` | `LocalDateTime` | Tokens past this date are rejected |
| `user` | `User` | ManyToOne |

---

## Security

### JWT Token Flow

```
Client                          IAM Service
  │                                  │
  │  POST /auth/login                │
  │ ────────────────────────────►   │
  │                                  │  Validates credentials
  │  { accessToken, refreshToken }   │  Issues JWT pair
  │ ◄────────────────────────────   │
  │                                  │
  │  GET /api/... (Bearer <token>)   │
  │ ────────────────────────────►   │ (validated by api-gateway or service itself)
```

- **Access Token**: Short-lived JWT (1 day). Contains `sub` (userId), `scope` (role), `iat`, `exp`. Stateless — verified by any service holding the secret.
- **Refresh Token**: Long-lived JWT (7 days). Stored server-side in `refresh_tokens` table. Used only to issue a new access token. Revocable.

### Security Rules

| Path Pattern | Rule |
|---|---|
| `/api/v1/auth/**` | Public — no authentication required |
| `/internal/**` | Public — **internal network only**, no JWT |
| `/api/v1/admin/**` | Requires `ROLE_ADMIN` |
| `/v3/api-docs/**`, `/swagger-ui/**` | Public (documentation) |
| Everything else | Requires a valid JWT |

### Method-Level Security

Controllers use `@PreAuthorize` for fine-grained checks:

```java
@PreAuthorize("hasRole('ADMIN') or hasAuthority('MANAGE_USERS')")
public ResponseEntity<?> updateUser(...) { ... }
```

---

## Email / OTP Flow

Password reset uses a 3-step OTP process:

```
Step 1: POST /auth/forgot-password   { "email": "..." }
        → Generates a 6-digit OTP, stores it with 15-min expiry
        → Sends email via Gmail SMTP using Thymeleaf HTML template

Step 2: POST /auth/verify-reset-code { "email": "...", "code": "123456" }
        → Validates OTP is correct and not expired
        → Returns a short-lived reset session token

Step 3: POST /auth/reset-password    { "email": "...", "code": "...", "newPassword": "..." }
        → Validates OTP again, updates password hash, invalidates all refresh tokens
        → Enforces password history (no reuse of last N passwords)
```

---

## Environment Variables

| Variable | Description | Default (dev) |
|---|---|---|
| `SPRING_DATASOURCE_URL` | PostgreSQL connection URL | `jdbc:postgresql://localhost:5432/iam_service_db` |
| `SPRING_DATASOURCE_USERNAME` | DB username | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | DB password | `postgres` |
| `JWT_SECRET_KEY` | Base64-encoded 256-bit HMAC secret | *(development key — change this!)* |
| `JWT_ACCESS_TOKEN_EXPIRATION` | Access token TTL in milliseconds | `86400000` (1 day) |
| `JWT_REFRESH_TOKEN_EXPIRATION` | Refresh token TTL in milliseconds | `604800000` (7 days) |
| `SPRING_MAIL_USERNAME` | Gmail address for sending OTP emails | — |
| `SPRING_MAIL_PASSWORD` | Gmail App Password (16 chars) | — |
| `SERVER_PORT` | HTTP port | `8080` |

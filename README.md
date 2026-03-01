# 💰 FinFlow - Personal Finance & Payment System

A microservices-based personal finance management system built with Spring Boot and Docker.

---

## 📋 Table of Contents
- [About](#about)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Services](#services)
- [Getting Started](#getting-started)
- [API Endpoints](#api-endpoints)
- [Project Structure](#project-structure)

---

## 📖 About

FinFlow is a backend system that simulates core banking features such as user registration, account management, fund transfers, and in-app notifications. This project demonstrates the implementation of microservices architecture using Spring Boot, Spring Cloud, and Docker.

**Key Concepts Demonstrated:**
- Spring IoC (Inversion of Control) & Dependency Injection
- Java Stream API for data processing
- Intermediate Native SQL Queries
- Containerization with Docker
- Microservices communication with OpenFeign

---

## 🛠️ Tech Stack

| Technology | Usage |
|---|---|
| Java 17 | Programming Language |
| Spring Boot 3.2 | Application Framework |
| Spring Cloud Gateway | API Gateway & Routing |
| Spring Cloud OpenFeign | Inter-service Communication |
| Spring Data JPA | ORM & Database Access |
| PostgreSQL | Relational Database |
| Docker & Docker Compose | Containerization |
| Maven | Build Tool |
| Lombok | Boilerplate Reduction |

---

## 🏗️ Architecture

```
                        ┌─────────────────┐
         Request        │   API Gateway   │
  Client ─────────────► │   (Port 8080)   │
                        └────────┬────────┘
                                 │ Route by path
              ┌──────────────────┼──────────────────┐
              │                  │                  │
    ┌─────────▼──────┐  ┌───────▼────────┐  ┌──────▼──────────┐
    │  User Service  │  │Account Service │  │  Transaction    │
    │  (Port 1000)   │  │  (Port 2000)   │  │    Service      │
    └─────────┬──────┘  └───────┬────────┘  │  (Port 3000)    │
              │                  │           └──────┬──────────┘
              ▼                  ▼                  ▼
           user-db          account-db     ┌──notification──┐
                                           │    Service     │
                                           │  (Port 4000)   │
                                           └──────┬─────────┘
                                                  ▼
                                          notification-db
```

**Communication Flow:**
```
User registers     → User Service (1000) → user-db
User opens account → Account Service (2000)
                       → validates user via Feign → User Service (1000)
                       → saves to account-db
User transfers     → Transaction Service (3000)
                       → validates & updates balance via Feign → Account Service (2000)
                       → saves transaction → transaction-db
                       → sends notification via Feign → Notification Service (4000)
```

---

## 📦 Services

| Service | Port | Database | Description |
|---|---|---|---|
| API Gateway | 8080 | - | Single entry point, routes all requests |
| User Service | 1000 | finflow_users | User registration & management |
| Account Service | 2000 | finflow_accounts | Account & balance management |
| Transaction Service | 3000 | finflow_transactions | Transfer & deposit |
| Notification Service | 4000 | finflow_notifications | In-app notifications |

---

## 🚀 Getting Started

### Prerequisites
- Docker Desktop installed
- Git

### Run the Application

```bash
# 1. Clone repository
git clone https://github.com/YOUR_USERNAME/finflow.git

# 2. Masuk ke folder project
cd finflow

# 3. Jalankan semua service
docker-compose up --build
```

Tunggu hingga semua container berjalan (sekitar 5-15 menit saat pertama kali).

### Verifikasi Semua Service Berjalan

```bash
docker-compose ps
```

### Stop Semua Service

```bash
docker-compose down
```

---

## 📡 API Endpoints

Semua request melalui API Gateway di `http://localhost:8080`

### 👤 User Service

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/users/register` | Register user baru |
| GET | `/api/v1/users/{id}` | Get user by ID |
| PUT | `/api/v1/users/{id}` | Update user |
| GET | `/api/v1/users/search?keyword=` | Cari user |
| GET | `/api/v1/users/stats/status` | Statistik user per status |
| GET | `/api/v1/users/stats/monthly` | Statistik registrasi per bulan |

**Contoh Register:**
```json
POST /api/v1/users/register
{
    "fullName": "Hizkia Renat",
    "email": "Hizkiarenat@example.com",
    "phoneNumber": "+6281234567890",
    "password": "password123"
}
```

### 🏦 Account Service

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/accounts` | Buka rekening baru |
| GET | `/api/v1/accounts/{id}` | Get rekening by ID |
| GET | `/api/v1/accounts/number/{accountNumber}` | Get rekening by nomor |
| GET | `/api/v1/accounts/user/{userId}` | Get semua rekening user |
| POST | `/api/v1/accounts/{id}/topup` | Top up saldo |
| PATCH | `/api/v1/accounts/{id}/balance` | Update saldo |

**Contoh Buka Rekening:**
```json
POST /api/v1/accounts
{
    "userId": "uuid-user-id",
    "accountType": "SAVING"
}
```

**Contoh Top Up:**
```json
POST /api/v1/accounts/{id}/topup
{
    "amount": 500000
}
```

### 💸 Transaction Service

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/transactions/transfer` | Transfer antar rekening |
| POST | `/api/v1/transactions/deposit` | Deposit ke rekening |
| GET | `/api/v1/transactions/history/{accountId}` | Riwayat transaksi |
| GET | `/api/v1/transactions/summary/{accountId}` | Ringkasan transaksi |

**Contoh Transfer:**
```json
POST /api/v1/transactions/transfer
{
    "fromAccountNumber": "1202401010001",
    "toAccountNumber": "1202401010002",
    "amount": 100000,
    "description": "Bayar makan siang"
}
```

**Contoh Deposit:**
```json
POST /api/v1/transactions/deposit
{
    "accountNumber": "1202401010001",
    "amount": 500000,
    "description": "Setor tunai"
}
```

### 🔔 Notification Service

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/notifications/user/{userId}` | Get semua notifikasi |
| GET | `/api/v1/notifications/user/{userId}/unread` | Get notifikasi belum dibaca |
| GET | `/api/v1/notifications/user/{userId}/unread/count` | Jumlah belum dibaca |
| PATCH | `/api/v1/notifications/user/{userId}/read-all` | Tandai semua sudah dibaca |
| GET | `/api/v1/notifications/user/{userId}/summary` | Ringkasan notifikasi per tipe |

---

## 📁 Project Structure

```
finflow/
├── api-gateway/                        # API Gateway (Port 8080)
│   ├── src/main/
│   │   ├── java/com/finflow/gateway/
│   │   │   └── ApiGatewayApplication.java
│   │   └── resources/
│   │       └── application.properties  # Routing config
│   ├── Dockerfile
│   └── pom.xml
│
├── user-service/                       # User Service (Port 1000)
│   ├── src/main/java/com/finflow/user/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/                 
│   │   ├── model/
│   │   ├── dto/
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
├── account-service/                    # Account Service (Port 2000)
│   ├── src/main/java/com/finflow/account/
│   │   ├── client/                     # Feign Client → User Service
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/                 
│   │   ├── model/
│   │   ├── dto/
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
├── transaction-service/                # Transaction Service (Port 3000)
│   ├── src/main/java/com/finflow/transaction/
│   │   ├── client/                     # Feign Client → Account & Notification
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/                 
│   │   ├── model/
│   │   ├── dto/
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
├── notification-service/               # Notification Service (Port 4000)
│   ├── src/main/java/com/finflow/notification/
│   │   ├── controller/
│   │   ├── service/
│   │   ├── repository/                 
│   │   ├── model/
│   │   ├── dto/
│   │   └── exception/
│   ├── Dockerfile
│   └── pom.xml
│
└── docker-compose.yml                  # Orchestrate semua service & database
```
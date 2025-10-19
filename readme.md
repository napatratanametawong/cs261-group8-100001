# Tech Stack
Java 17 · Spring Boot 3 

Spring Web, Spring Data JPA

Microsoft SQL Server (JDBC)

JWT (HS256)

Maven

## Prerequisites
- Docker & Docker Compose
- (Optional) Java 17+ and Maven 3.9+ if you plan to run locally without Docker

# Quick Start
## 1) Configure environment
**Do NOT commit `.env` to git.**

Copy the example and fill real values:
```bash
cp .env.example .env
```
### Generate a secure JWT secret (≥ 32 bytes):
Online link: https://onlinebase64tools.com/generate-random-base64

## 2) Run with Docker (DB + App)
1) build 
```bash
docker compose up -d --build
```
2) Run Application
```bash
docker compose logs -f app
```
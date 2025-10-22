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

## Optional
```bash
docker compose down -v --remove-orphans
docker builder prune -f
docker system prune -f
```
## 2) Run with Docker (DB + App)
1) build 
```bash
docker compose build --no-cache --progress=plain app
```
2) Run Application

```bash
docker compose logs -f app
docker compose up -d mssql
docker compose up --abort-on-container-exit db-init
```

```bash
docker compose up -d app
```
### Link Website

http://localhost:8080/login/pages/loginPage.html

### In case of 00_init.sql can't run by docker-compose.yml
# 1) ตั้งรหัส SA (หรือแก้เป็นของคุณ)
```bash
$env:SA_PASSWORD = 'YourStrong@Passw0rd!'
```
# 2) รัน sqlcmd จากคอนเทนเนอร์ mssql-tools บนเน็ตเวิร์กของ compose
```bash
docker run --rm `
  --network cs261-group8-100001_default `
  -v "${PWD}/docker/sql:/sql:ro" `
  mcr.microsoft.com/mssql-tools:latest `
  /bin/sh -lc "/opt/mssql-tools/bin/sqlcmd -S mssql -U sa -P '$($env:SA_PASSWORD)' -d master -b -V16 -i /sql/00_init.sql"
```

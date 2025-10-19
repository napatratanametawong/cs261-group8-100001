Tech Stack
Java 17 · Spring Boot 3
Spring Web, Spring Data JPA
Microsoft SQL Server (JDBC)
JWT (HS256)
Maven

Quick Start
1) Configure environment
Copy example and fill real values (do NOT commit .env):
cp .env.example .env
Generate a secure JWT secret (≥ 32 bytes):
link: https://onlinebase64tools.com/generate-random-base64

2) Run with Docker (DB + App)
docker compose up -d --build
docker compose logs -f app
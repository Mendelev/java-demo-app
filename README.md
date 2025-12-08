# Todo App

Full-stack todo app with a Spring Boot backend, React + Vite frontend, and Postgres. Tasks support title, description, status (`PENDING`, `IN_PROGRESS`, `DONE`), optional due date, filtering by status/date range, and full CRUD.

## Stack
- Backend: Java 21, Spring Boot 3.2, JPA, Flyway, Postgres
- Frontend: React 18 + Vite, React Query, Axios
- Tooling: Docker Compose for local orchestration, Maven for backend builds

## Quick Start

### Option 1: Run everything with Docker (recommended)
Prereqs: Docker + Docker Compose installed.

```bash
docker compose up --build
```

Services:
- Frontend: http://localhost:8081
- Backend API: http://localhost:8080 (profile `docker`, Postgres db)
- Postgres: exposed on port 5432 with db/user/password `todoapp`/`todo`/`todo`

Stop the stack with `docker compose down` (add `-v` to drop volumes).

### Option 2: Local development (run services separately)
Prereqs: JDK 21 + Maven, Node 18+, a running Postgres instance.

1) Start Postgres  
Create a database and user, or export env vars that match your setup (defaults shown):
```
DB_HOST=localhost
DB_PORT=5432
DB_NAME=todoapp
DB_USER=todo
DB_PASSWORD=todo
ALLOWED_ORIGINS=http://localhost:5173,http://localhost:8081
```

2) Run the backend
```bash
cd backend
mvn spring-boot:run
```
API served at http://localhost:8080/api. Flyway migrates schema on startup.

3) Run the frontend
```bash
cd frontend
npm install
VITE_API_URL=http://localhost:8080 npm run dev -- --host
```
App served at http://localhost:5173.

## Useful Commands
- Backend tests: `cd backend && mvn test`
- Backend package: `cd backend && mvn package`
- Frontend build: `cd frontend && npm run build`

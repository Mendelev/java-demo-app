# Todo App

Full-stack todo app with a Spring Boot backend, React + Vite frontend, and Postgres. Containers are split per service.

## Run with Docker

```bash
docker compose up --build
```

Services:
- Backend: http://localhost:8080 (profile `docker`, Postgres db)
- Frontend: http://localhost:8081

## Local Backend (optional)

```bash
cd backend
mvn spring-boot:run
```

Environment variables for local/dev:
- `DB_HOST` (default `localhost`)
- `DB_PORT` (default `5432`)
- `DB_NAME` (default `todoapp`)
- `DB_USER` / `DB_PASSWORD`
- `ALLOWED_ORIGINS` (comma separated; default `http://localhost:5173,http://localhost:8081`)

## Local Frontend (optional)

```bash
cd frontend
npm install
npm run dev -- --host
```

Set `VITE_API_URL` to your backend URL (e.g., `http://localhost:8080`).

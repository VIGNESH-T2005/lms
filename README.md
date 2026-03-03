# Full LMS Website (Spring Boot + React + MySQL)

This repository contains a complete Learning Management System with:
- **Backend**: Java 17, Spring Boot, Spring Data JPA, MySQL
- **Frontend**: React (Vite)
- **Database**: MySQL 8
- **Orchestration**: Docker Compose

## Features
- Manage students
- Manage courses
- Enroll students in courses
- View enrollment records
- Seed sample students and courses on first run

## Project Structure
- `backend/` — Spring Boot REST API
- `frontend/` — React web app
- `docker-compose.yml` — runs MySQL, backend, and frontend together

## Run with Docker (recommended)

```bash
docker compose up --build
```

Then open:
- Frontend: `http://localhost:5173`
- Backend API: `http://localhost:8080/api`

## Run Locally Without Docker

### 1) Start MySQL
Ensure a local MySQL is running and create credentials:
- database: `lms_db`
- username: `lms_user`
- password: `lms_pass`

### 2) Start Backend
```bash
cd backend
mvn spring-boot:run
```

### 3) Start Frontend
```bash
cd frontend
npm install
npm run dev
```

Frontend development URL: `http://localhost:5173`

## API Endpoints

### Students
- `GET /api/students`
- `POST /api/students`

Payload:
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com"
}
```

### Courses
- `GET /api/courses`
- `POST /api/courses`

Payload:
```json
{
  "title": "Databases 101",
  "description": "Intro to relational DBs",
  "instructor": "Prof. Lee"
}
```

### Enrollments
- `GET /api/enrollments`
- `POST /api/enrollments`

Payload:
```json
{
  "studentId": 1,
  "courseId": 1
}
```

## Notes
- CORS is configured for `http://localhost:5173`.
- Backend schema is auto-managed via JPA (`ddl-auto=update`).

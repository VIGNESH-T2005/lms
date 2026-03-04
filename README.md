# Advanced LMS Platform (Spring Boot + React + MySQL)

A production-style Learning Management System with analytics, assignment planning, grading workflows, and full-stack containerized deployment.

## Stack
- **Backend**: Java 17, Spring Boot 3, Spring Data JPA, Validation
- **Frontend**: React 18 + Vite
- **Database**: MySQL 8
- **DevOps**: Docker + Docker Compose

## Advanced Features
- Student and course management with search filters
- Enrollment workflow with duplicate enrollment protection
- Assignment planner (publish assignment per course with due date and max score)
- Submission + grading workflow (create/update submission scores)
- Validation rule: students can submit only when enrolled in the assignment course
- Dashboard analytics endpoint and UI cards:
  - total students/courses/enrollments/assignments/submissions
  - average score
- Global JSON error handling for API exceptions and validation errors

## Backend API
Base URL: `/api`

### Dashboard
- `GET /dashboard/stats`

### Students
- `GET /students?q=alice`
- `POST /students`

### Courses
- `GET /courses?q=spring`
- `POST /courses`

### Enrollments
- `GET /enrollments`
- `POST /enrollments`

### Assignments
- `GET /assignments`
- `GET /assignments?courseId=1`
- `POST /assignments`

### Submissions
- `GET /submissions`
- `GET /submissions?studentId=1`
- `GET /submissions?assignmentId=1`
- `POST /submissions` (upsert behavior by assignment+student)

## Run with Docker
```bash
docker compose up --build
```

Services:
- Frontend: `http://localhost:5173`
- Backend: `http://localhost:8080/api`
- MySQL: `localhost:3306`

## Run Locally
1) Start MySQL with database/credentials:
- database: `lms_db`
- username: `lms_user`
- password: `lms_pass`

2) Start backend:
```bash
cd backend
mvn spring-boot:run
```

3) Start frontend:
```bash
cd frontend
npm install
npm run dev
```

## Seed Data
App startup seeds demo records for:
- students
- courses
- enrollments
- assignment
- sample submission

## Notes
- CORS is configured for `http://localhost:5173`.
- `spring.jpa.hibernate.ddl-auto=update` is enabled for quick iteration.

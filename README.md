# Centralized LMS Platform (Spring Boot + React + MySQL)

A centralized Learning Management System that unifies **user logins**, **course enrollment**, **attendance tracking**, **assessments**, and **certifications** in one seamless modern dashboard.

## What this solves
This project implements the problem statement by providing a single, intuitive portal where institutions can:
- authenticate users,
- manage students/courses,
- enroll students,
- conduct assessment and grading workflows,
- track attendance,
- issue certifications,
- monitor KPIs from one dashboard.

## Architecture
- **Frontend**: React + Vite (modern responsive UI)
- **Backend**: Java 17, Spring Boot REST API
- **Database**: MySQL 8 via Spring Data JPA
- **Orchestration**: Docker Compose

## Core Modules
1. **Authentication**
   - Register and login endpoints
   - Session token generation and profile endpoint (`/auth/me`)
2. **Academic Core**
   - Students, Courses, Enrollments
3. **Assessments**
   - Assignments per course
   - Submissions and grading
4. **Attendance**
   - Daily attendance logs per student-course-date
5. **Certifications**
   - Certificate issue workflow with unique code and score threshold
6. **Analytics Dashboard**
   - Central stats for users, academics, attendance, certifications, and performance

## API Highlights
Base: `/api`
- `POST /auth/register`
- `POST /auth/login`
- `GET /auth/me` (header: `X-Auth-Token`)
- `GET /dashboard/stats`
- `GET/POST /students`
- `GET/POST /courses`
- `GET/POST /enrollments`
- `GET/POST /assignments`
- `GET/POST /submissions`
- `GET/POST /attendance`
- `GET/POST /certifications`

## Business Rules Included
- Duplicate student email protection
- Duplicate enrollment protection
- Submission allowed only for enrolled students
- Submission score cannot exceed assignment max score
- Attendance status restricted to PRESENT / ABSENT / LATE
- Certification requires enrollment and minimum passing final score
- One certification per student per course

## Local Run (without Docker)
1. Start MySQL and create:
   - DB: `lms_db`
   - User: `lms_user`
   - Password: `lms_pass`
2. Run backend:
   ```bash
   cd backend
   mvn spring-boot:run
   ```
3. Run frontend:
   ```bash
   cd frontend
   npm install
   npm run dev
   ```

## Docker Run
```bash
docker compose up --build
```

## Seeded Demo Credentials
- Email: `admin@lms.com`
- Password: `Admin@123`

## Notes
- CORS is configured for `http://localhost:5173`
- `ddl-auto=update` is enabled for iterative development

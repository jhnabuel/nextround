# NextRound
![CI](https://github.com/jhnabuel/nextround/actions/workflows/ci.yml/badge.svg)

A full-stack job application and interview pipeline tracker built with **Java 21, Spring Boot, React, and TypeScript**.

**Live Demo:** [nextround.nextroundwork.workers.dev](https://nextround.nextroundwork.workers.dev)

---

## Features

- **JWT Authentication & Data Isolation:** Stateless JWT authentication with Spring Security. Database queries and mutations are scoped to the authenticated user, preventing unauthorized cross-user resource access.
- **Application Pipeline:** Full CRUD for job applications with status tracking (`WISHLIST`, `APPLIED`, `INTERVIEWING`, `OFFER_RECEIVED`, `ACCEPTED`, `REJECTED`, `WITHDRAWN`, `GHOSTED`), search, filtering, and sorting.
- **Company Directory:** Manage target companies with case-insensitive duplicate prevention, debounced server-side search, inline creation from the application form, and deletion protection when linked applications exist.
- **Analytics Dashboard:** User-scoped KPI cards and interactive 12-week / 6-month application activity charts backed by SQL aggregation and timezone-safe period bucketing.
- **Automatic Cache Synchronization:** TanStack Query cache invalidation keeps the dashboard, application board, and company directory synchronized after mutations.

---

## Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Backend** | Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA, Hibernate, PostgreSQL 16, Maven |
| **Frontend** | React, TypeScript, Vite, Tailwind CSS, TanStack Query, React Router, Recharts, Zod, Axios |
| **Testing** | JUnit 5, Mockito, Testcontainers (PostgreSQL 16) — 76 unit and integration tests |
| **Deployment** | Railway (Spring Boot API + PostgreSQL), Cloudflare Workers Static Assets (SPA) |

---

## Architecture

NextRound follows a client-server architecture. The React frontend communicates with the Spring Boot REST API over HTTPS using JWT bearer authentication. The backend handles business logic, authorization, and database operations through Spring Data JPA.

```text
                 React + TypeScript
                  Cloudflare Workers
                         |
                         | HTTPS / REST / JWT
                         |
                         v
                  Spring Boot API
                      Railway
                         |
                         | Spring Data JPA
                         |
                         v
                    PostgreSQL 16
                      Railway
```

### Database Design

The core data model consists of three entities:

- **User:** Represents an authenticated account.
- **Company:** Represents a company managed by a user.
- **JobApplication:** Represents a job application associated with a user and company.

The data model supports user-specific company directories and application records. Ownership is enforced at the service and repository levels.

---

## Security and Engineering Decisions

Several implementation decisions focus on data integrity, authorization, and application reliability.

- **Stateless Authentication:** JWT-based authentication with Spring Security and BCrypt password hashing.
- **Resource Ownership:** Repository queries and service operations enforce user ownership rather than relying solely on frontend authorization.
- **IDOR Prevention:** Cross-user resource access is rejected through ownership-scoped lookups, returning `404` to avoid exposing resource existence.
- **Database Constraints:** Case-insensitive unique company names per user prevent duplicate records, including concurrent creation attempts.
- **Data Integrity:** Company deletion is restricted when associated job applications exist, preserving application history.
- **Query Optimization:** Entity graphs and SQL aggregation are used to reduce unnecessary database queries and calculate dashboard statistics efficiently.
- **Integration Testing:** Testcontainers runs integration tests against PostgreSQL 16 to verify database behavior and constraints.

---

## API Overview

Base path: `/api/v1`.

All endpoints except `/auth/**` and `/actuator/health` require a `Bearer` JWT.

| Domain | Endpoints |
| :--- | :--- |
| **Auth** | `POST /auth/register`, `POST /auth/login` |
| **Users** | `GET /users/me`, `PATCH /users/me/password` |
| **Applications** | `GET /applications`, `POST /applications`, `GET /applications/{id}`, `PATCH /applications/{id}`, `DELETE /applications/{id}` |
| **Companies** | `GET /companies`, `POST /companies`, `GET /companies/{id}`, `PATCH /companies/{id}`, `DELETE /companies/{id}` |
| **Dashboard** | `GET /dashboard/stats`, `GET /dashboard/activity?granularity=WEEK\|MONTH` |

---

## Project Structure

```text
JobApplicationTracker/
├── backend/          # Spring Boot REST API
│   ├── src/
│   └── pom.xml
└── frontend/         # React + TypeScript SPA
    ├── src/
    ├── public/
    └── package.json
```

---

## Local Development

### Prerequisites

- **Java 21+**
- **Node.js 20+**
- **Docker** (required for Testcontainers and optional local PostgreSQL)

### 1. Backend

Set the required environment variables in your terminal, IDE, or `.env` configuration:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/nextround
export SPRING_DATASOURCE_USERNAME=postgres
export SPRING_DATASOURCE_PASSWORD=your_password
export JWT_SECRET=your_base64_encoded_secret_key
export JWT_EXPIRATION=86400000
export CORS_ALLOWED_ORIGINS=http://localhost:5173
```

Run the API:

```bash
cd backend
./mvnw spring-boot:run
```

### 2. Frontend

Create `frontend/.env.local`:

```env
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Install dependencies and start the development server:

```bash
cd frontend
npm install
npm run dev
```

---

## Running Tests and Builds

```bash
# Backend: unit and Testcontainers integration tests
# Docker must be running
cd backend && ./mvnw test

# Frontend: TypeScript check and production build
cd frontend && npm run build
```

---

## Deployment

- **Backend:** Deployed on Railway from the `main` branch using Spring Boot.
- **Database:** PostgreSQL 16 hosted on Railway, connected to the backend through Railway's private network.
- **Frontend:** Deployed as a Single-Page Application using Cloudflare Workers Static Assets. The `frontend/wrangler.jsonc` configuration uses `not_found_handling: "single-page-application"` for client-side routing.

---

## Planned Improvements

- Custom interview stage tracking for each job application.
- Interview scheduling and reminders.
- Google Calendar integration.
- Profile management and account recovery.
- Load testing and performance optimization using k6.
- Database indexing and query optimization based on benchmark results.

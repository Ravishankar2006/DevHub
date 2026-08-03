# DevHub

> **AI Developer Operating System** — One connected workspace for developers to manage projects, tasks, notes, learning, resumes, and job applications — with AI-powered guidance built on top of their own data.

---

## Project Structure

```
DevHub/
├── frontend/       # React + TypeScript + Vite (deploy → Vercel)
├── backend/        # Spring Boot 3 (deploy → Railway)
├── docs/           # PRD, schema, API contracts
└── infra/          # Docker Compose (local dev), env templates
```

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 18, TypeScript, Vite, TailwindCSS, shadcn/ui |
| Backend | Spring Boot 3.x, Spring Security, Spring Data JPA, Flyway |
| Database | PostgreSQL (Neon) + pgvector |
| AI | OpenAI API via Spring AI |
| Storage | Supabase Storage |
| Cache | Redis (narrow, optional) |

## Getting Started (Local Dev)

### Prerequisites
- Node.js 20+
- Java 21+ JDK
- Maven 3.9+

### Frontend

```bash
cd frontend
cp .env.example .env.local
npm install
npm run dev
```

### Backend

```bash
cd backend
cp .env.example .env
# Fill in NEON_DATABASE_URL and JWT_SECRET
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## Build Phases

| Phase | Description |
|---|---|
| **0** | Scaffold + Auth |
| **1** | Core Workspace (Projects, Tasks, Goals, Notes) |
| **2** | Growth Modules (Learning, Resumes, Jobs, Calendar) |
| **3** | Intelligence Layer (AI Assistant, Daily Brief) |
| **4** | Connected Context (GitHub, Embeddings, Semantic Search) |
| **5** | Hardening + Production Deployment |

## MVP Modules

Auth · Dashboard · Projects · Tasks & Milestones · Goals & Habits · Notes & Docs · Learning Tracker · Resume Manager · Job Tracker · Calendar · AI Assistant · AI Daily Brief · AI Resume Review · GitHub Integration · Semantic Search

---

*Built solo. Designed for scale.*

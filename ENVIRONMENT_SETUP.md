# Environment Setup Guide

This document outlines the strict environment configuration required for local development, staging, and production environments for Ai-Code-Review.

## Prerequisites
- **Java 17** (Temurin/Adoptium recommended)
- **Node.js 20+** (Active LTS)
- **Docker & Docker Compose** (v2+)
- **Git**

## 1. Local Development via Docker (Recommended)

To mimic production as closely as possible, use the provided Docker Compose environment.

1. **Copy the Environment Template**
   ```bash
   cp .env.example .env
   ```
2. **Inject Secrets**
   Open `.env` and configure:
   - `GROQ_API_KEY`: Obtain from console.groq.com
   - `GOOGLE_CLIENT_ID` / `SECRET`: Obtain from GCP Console (OAuth credentials)
   - `GITHUB_CLIENT_ID` / `SECRET`: Obtain from GitHub Developer Settings
   - `POSTGRES_USER` & `POSTGRES_PASSWORD`: Use `user`/`password` for local.

3. **Start the Infrastructure**
   ```bash
   docker-compose --env-file .env up -d --build
   ```

4. **Start Frontend (Hot Reload)**
   ```bash
   cd frontend
   npm ci
   npm run dev
   ```

## 2. Environment Variables Dictionary

| Variable | Scope | Description |
| :--- | :--- | :--- |
| `GROQ_API_KEY` | Backend (`worker-service`) | Bearer token for AI Code analysis. |
| `SPRING_PROFILES_ACTIVE` | Backend | Use `docker` for compose, `local` for IDE development. |
| `VITE_API_URL` | Frontend | Base URL for REST API. Default: `http://localhost:8088`. |
| `GRAFANA_PASSWORD` | Docker | Admin password for monitoring dashboards. |

> **Warning:** Never commit `.env`, `.env.local`, or `.env.production` files. 

## 3. Local Execution Without Docker (Bare Metal)

If you prefer running services directly via IDE (e.g., IntelliJ or STS):

1. Start PostgreSQL (Port 5432) and RabbitMQ (Port 5672) locally or via a partial `docker-compose`.
2. Update `api-service/src/main/resources/application.properties` to ensure URLs map to `localhost` rather than docker hostnames (`postgres`, `rabbitmq`).
3. Set your IDE Environment Variables to match your `.env` contents.
4. Run `ApiServiceApplication` and `WorkerServiceApplication` standard main classes.

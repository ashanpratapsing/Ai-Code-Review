# 🎯 SENIOR FULL-STACK EXECUTION GUIDE
**Standard Operating Procedure for AI Code Review Platform**

This guide provides a rigorous, senior-level protocol for deploying and verifying the full-stack ecosystem.

---

## 🏗 Phase 1: Environment Baseline
Before execution, verify the environment parameters:
- **Node**: `v20.x` or `v22.x`
- **Java**: `JDK 17+`
- **Memory**: Minimum 8GB RAM for Docker orchestration.
- **Environment**: Rename `.env.example` to `.env` in the root and frontend directories.

## 📦 Phase 2: Dependency Integrity
Ensure all local caches are healthy.
```bash
# Frontend
cd frontend && npm install && cd ..

# Backend
./mvnw dependency:go-offline
```

## 🚀 Phase 3: Infrastructure Orchestration
Launch the service mesh (Postgres, Redis, RabbitMQ, Grafana):
```powershell
docker-compose up -d --build --remove-orphans
```
*Wait for Docker health checks to pass before Phase 4.*

## ⚡ Phase 4: Backend Boot & Actuation
Start the Spring Boot application core:
```powershell
./mvnw spring-boot:run
```
**Verification**: Reach `http://localhost:8080/actuator/health`. Ensure status is `UP`.

## 🎨 Phase 5: Frontend Dashboard Mount
Initialize the Vite development server:
```powershell
cd frontend
npm run dev
```
**Verification**: Application should be reachable at `http://localhost:5173`.

## 🔐 Phase 6: Authentication & Handshake
Perform a signup/login cycle to generate a fresh JWT.
1. Navigate to `/register`.
2. Create `dev@faang.com`.
3. Login and verify a token is present in `localStorage`.

## 🧪 Phase 7: End-to-End Verification
1. **Flow**: Paste a 20-line Java function into the Code Analyzer.
2. **Action**: Click "Check Code Now".
3. **Internal Check**: Watch the backend logs for `callGroqAI` logs.
4. **Success**: Verify that "Refactored Code" and "Issues" appear in the results panel.

## 🛠 Phase 8: Diagnostic Protocols (If Fails)
- **401 Unauthorized**: JWT signature mismatch. Sign out and sign in again.
- **500 Error**: Check if Groq API keys in `application.properties` are valid.
- **Frontend Blank**: Inspect `div#root` in DevTools for height collapse issues.

---
*Reference the [MASTER_INDEX.md](./MASTER_INDEX.md) for related guides.*

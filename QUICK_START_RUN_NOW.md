# ⚡ QUICK START: RUN NOW
**Project: AI Code Review Platform**

Follow these steps to get the full-stack environment running in under 5 minutes.

---

## 📋 Pre-Flight Checklist
- [ ] **Docker Desktop**: Must be running.
- [ ] **Java**: JDK 17 or 21 installed (`java -version`).
- [ ] **Node.js**: v20+ installed (`node -v`).
- [ ] **Ports**: Verify `8080` (Backend) and `5173` (Frontend) are free.

---

## 🚀 Startup Order

### Terminal 1: Infrastructure & Backend
```powershell
# 1. Package the Spring Boot app
./mvnw clean package -DskipTests

# 2. Start all services (Postgres, Redis, MQ, Actuator)
docker-compose up -d --build
```

### Terminal 2: Frontend Dashboard
```powershell
cd frontend
npm install
npm run dev
```

---

## 🔍 Verification (Do this immediately)

### 1. Backend Health
Check if the API is responsive:
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### 2. Frontend Access
Open your browser to:
👉 **[http://localhost:5173](http://localhost:5173)**

---

## 🛠 Quick Fixes for Common Issues

| Issue | Symptom | Fix |
|-------|---------|-----|
| **Port Conflict** | "Address already in use" | Run `Stop-Process -Name node -Force` in PowerShell. |
| **Auth Error** | 401 Unauthorized | Backend restarted? Clear browser LocalStorage and re-login. |
| **Missing Editor** | Screen is blank | Check `dev.log` for compilation errors or container height. |
| **DB Issues** | Can't connect | Verify `docker ps` shows the postgres container as "Healthy". |

---
*Reference the [MASTER_INDEX.md](./MASTER_INDEX.md) for full documentation.*

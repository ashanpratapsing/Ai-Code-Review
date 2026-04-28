# 🔍 SENIOR FRONTEND DIAGNOSTIC GUIDE
**17-Phase Debugging Protocol for Modern React Applications**

Use this systematic checklist when the frontend dashboard fails to load or interacts incorrectly with the backend.

---

## 🏗 Phase 1: Environment Baseline
- **Sync**: Verify `node_modules` exists. Run `npm install` if missing.
- **Node**: Ensure `v20.x` or higher is active.
- **Env**: check `.env` for `VITE_API_URL`.

## 📦 Phase 2-4: Build & Compile
- **Check**: Run `npm run build` and check for type errors.
- **Fix**: Resolve any "Expected corresponding JSX closing tag" or "Missing Import" errors.

## 📡 Phase 5-9: Connectivity & Network
- **401 Unauthorized**: Backend RSA key has likely rotated. Clear localStorage and re-login.
- **Port Conflict**: Vite may move from `5173` to `5174` if child processes leak. Use `Get-Process node | Stop-Process` to clean up.
- **CORS**: Verify `SecurityConfig.java` in the backend explicitly allows `http://localhost:5173`.

## 🧩 Phase 10-14: State & Rendering
- **Blank Screen**: Inspect DevTools. Ensure the root element is visible and not collapsed.
- **Stale Context**: If Auth is broken, the `AuthContext` may be providing dummy users. Verify the login endpoint returns a valid JWT.

## 🛠 Phase 15-17: Final Verification
- **Network Tab**: Ensure no failing (red) requests to `/api/*`.
- **Console Tab**: No `Uncaught ReferenceError` or `TypeError` messages.
- **Recharts Warnings**: Usually benign layout warnings, but ensure chart parents have fixed dimensions.

---
*Reference the [MASTER_INDEX.md](./MASTER_INDEX.md) for related guides.*

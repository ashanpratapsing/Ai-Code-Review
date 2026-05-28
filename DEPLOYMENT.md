# Deployment Guide

This document outlines how to deploy the Ai-Code-Review application into production. The application consists of a React/Vite frontend and a multi-service Spring Boot backend using PostgreSQL, Redis, and RabbitMQ.

## 1. Backend Deployment (Docker Compose)
The easiest way to deploy the backend services is using Docker Compose. This is ideal for a single VPS or a containerized platform like Render or DigitalOcean.

### Prerequisites
- Docker and Docker Compose installed.

### Steps
1. Clone the repository to your production server.
2. Navigate to the root directory where `docker-compose.yml` is located.
3. Create a `.env` file based on `.env.example`. **DO NOT COMMIT YOUR SECRETS!**
   ```bash
   cp .env.example .env
   # Edit .env with your real credentials
   nano .env
   ```
4. Build and start the services in detached mode:
   ```bash
   docker-compose --env-file .env up -d --build
   ```
5. Verify services are running:
   ```bash
   docker-compose ps
   ```

### Important Ports
- **API Gateway**: 80 (nginx reverse proxy)
- **API Service**: 8080 (internal mapped port)
- **Grafana Dashboard**: 3000
- **Prometheus**: 9090

## 2. Frontend Deployment (Vercel / Netlify)
The frontend is built with Vite and React and can be hosted statically on Vercel or Netlify.

### Vercel Deployment
1. Connect your GitHub repository to Vercel.
2. Ensure the Framework Preset is set to **Vite**.
3. The Build Command should be: `npm run build`
4. The Output Directory should be: `dist`
5. **Environment Variables**: Add `VITE_API_URL` pointing to your deployed backend (e.g., `https://api.yourdomain.com`).

### Netlify Deployment
Same settings as above. Ensure you add `VITE_API_URL` in the Site Settings > Environment Variables.

## 3. Production Readiness Checklist

### Security
- [ ] No API keys are hardcoded in the codebase (Checked via audit).
- [ ] `GROQ_API_KEY` is injected via `.env` securely.
- [ ] Database exposed ports (5432, 6379) are secured via firewall (UFW) in production.
- [ ] SSL/TLS is set up (via Nginx or Cloudflare).

### Environment Configuration
- [ ] Spring Boot profiles are set correctly (`SPRING_PROFILES_ACTIVE=docker`).
- [ ] CORS policies in `api-service` explicitly allow the production frontend domain instead of `*` or `localhost`.

### CI/CD
- [ ] GitHub Actions workflow is passing on every commit.

---
**Note:** Ensure your backend URL is secure (HTTPS) in production, as the frontend will block mixed content (HTTP calls from an HTTPS site).

# Security Architecture & Policies

This document outlines the security architecture and hardening measures implemented in the Ai-Code-Review platform.

## 1. Zero Trust Architecture

### Frontend / Client Trust
The React frontend is treated as completely untrusted. 
- **Secret Isolation**: The frontend bundle DOES NOT contain any API keys, provider credentials, or database connection strings.
- **VITE Variables**: Only safe variables prefixed with `VITE_` (like API endpoints) are baked into the frontend bundle.
- **XSS Mitigation**: React handles most injection automatically via escaping. Any injected HTML via markdown uses `dompurify` to strip dangerous execution scripts.

### AI Provider Isolation
- The frontend **never** communicates directly with Groq, OpenAI, or Claude APIs.
- AI requests are brokered by the backend `worker-service`. The `worker-service` holds the `GROQ_API_KEY` entirely Server-Side.
- This prevents malicious users from scraping keys from DevTools network tabs.

## 2. Platform Security (Backend)

### Strict Headers (Helmet Equivalents)
Spring Security enforces strict security headers:
- **Content-Security-Policy (CSP)**: `default-src 'self'; script-src 'self' 'unsafe-inline'; object-src 'none'; frame-ancestors 'none';` - Prevents most XSS and clickjacking attacks.
- **X-Frame-Options**: `DENY` - Defends against clickjacking.
- **HSTS**: Forces HTTPS on all subdomains.

### Authorization (OAuth2 / JWT)
- Auth is handled via strictly validated JWT Bearer tokens issued by the API Gateway or Identity Provider (Google/GitHub).
- Endpoints except `/auth/**` are guarded by `@EnableWebSecurity` rejecting unauthenticated requests.

## 3. Infrastructure & DevOps

### Environment Injection
- Secrets are NEVER checked into version control. `docker-compose` mounts `.env` directly into container process spaces at runtime.

### API Gateway (Nginx)
- The Nginx reverse proxy hides backend port bindings (8088/8089) from the public internet.
- Nginx provides an additional layer to terminate SSL/TLS before passing traffic locally to the Spring containers.

## 4. Threat Models Addressed
1. **Developer Secret Leak**: Mitigated by `.gitignore` preventing `.env` and `.env.local` commits.
2. **Rate Limiting Exhaustion**: (TODO/Mitigated) The `api-service` should restrict requests via Bucket4j to prevent budget draining on Groq.
3. **Cross-Site Scripting (XSS)**: Mitigated by React escaping and strict CSP headers.
4. **Third-Party Supply Chain**: Mitigated by running `npm audit` and restricting dependencies.

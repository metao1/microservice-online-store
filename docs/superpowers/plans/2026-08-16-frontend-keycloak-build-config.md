# Frontend Keycloak Build Configuration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Stop the frontend reload loop by providing and validating the Keycloak build-time configuration.

**Architecture:** A pure configuration parser validates Vite environment values before `keycloak-js` is constructed. Docker Compose supplies those values as build arguments, and the frontend Dockerfile exposes them to Vite during compilation.

**Tech Stack:** React, TypeScript, Vite, Vitest, keycloak-js, Docker Compose

## Global Constraints

- Use `http://localhost:8080`, `bookstore`, and `bookstore-frontend` for the Compose frontend build.
- Do not change the PKCE flow, realm definition, redirect URIs, or category fetching.
- Do not cache configuration failures.

---

### Task 1: Validate and inject frontend Keycloak configuration

**Files:**
- Create: `frontend/src/auth/keycloakConfig.ts`
- Create: `frontend/src/auth/keycloakConfig.test.ts`
- Modify: `frontend/src/auth/keycloak.ts`
- Modify: `frontend/Dockerfile`
- Modify: `docker-compose.yml`

**Interfaces:**
- Produces: `readKeycloakConfig(env): { url: string; realm: string; clientId: string }`
- Consumes: `ImportMetaEnv` and the existing `Keycloak` constructor.

- [x] **Step 1: Write failing parser tests**

Test that valid values are returned unchanged and missing values throw an error containing every missing Vite variable name.

```ts
expect(readKeycloakConfig({
  VITE_KEYCLOAK_URL: 'http://localhost:8080',
  VITE_KEYCLOAK_REALM: 'bookstore',
  VITE_KEYCLOAK_CLIENT_ID: 'bookstore-frontend',
})).toEqual({
  url: 'http://localhost:8080',
  realm: 'bookstore',
  clientId: 'bookstore-frontend',
});

expect(() => readKeycloakConfig({})).toThrow(/VITE_KEYCLOAK_URL.*VITE_KEYCLOAK_REALM.*VITE_KEYCLOAK_CLIENT_ID/);
```

- [x] **Step 2: Verify RED**

Run: `cd frontend && npm test -- --run src/auth/keycloakConfig.test.ts`

Expected: FAIL because `readKeycloakConfig` does not exist.

- [x] **Step 3: Implement the parser and use it**

Implement a pure parser that trims values, accumulates missing names in fixed order, and throws `Missing Keycloak configuration: <names>`. Construct `Keycloak` with `readKeycloakConfig(import.meta.env)` in `keycloak.ts`.

- [x] **Step 4: Inject the Vite build variables**

Add matching `ARG` and `ENV` declarations to `frontend/Dockerfile`. Add these frontend build args to `docker-compose.yml`:

```yaml
VITE_KEYCLOAK_URL: "http://localhost:8080"
VITE_KEYCLOAK_REALM: "bookstore"
VITE_KEYCLOAK_CLIENT_ID: "bookstore-frontend"
```

- [x] **Step 5: Verify GREEN and configuration**

Run: `cd frontend && npm test -- --run src/auth/keycloakConfig.test.ts`

Expected: both focused tests pass.

Run: `docker compose config`

Expected: frontend build args contain all three exact Keycloak values.

Run: `cd frontend && VITE_KEYCLOAK_URL=http://localhost:8080 VITE_KEYCLOAK_REALM=bookstore VITE_KEYCLOAK_CLIENT_ID=bookstore-frontend npm run build`

Expected: build succeeds.

- [x] **Step 6: Commit**

```bash
git add frontend/src/auth/keycloakConfig.ts frontend/src/auth/keycloakConfig.test.ts frontend/src/auth/keycloak.ts frontend/Dockerfile docker-compose.yml
git commit -m "fix: configure frontend Keycloak client"
```

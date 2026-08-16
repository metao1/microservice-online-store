# Security Filter Chain CORS Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Allow configured browser CORS preflights to pass through the shared JWT security filter chain without weakening authentication for actual API requests.

**Architecture:** Spring Security enables its standard CORS integration and delegates policy decisions to the existing Spring MVC CORS configuration. A focused order-service integration test exercises the real filter chain at the `/cart` boundary.

**Tech Stack:** Java 25, Spring Boot 4.1, Spring Security, JUnit 5, RestAssured, Gradle

## Global Constraints

- Reuse `app.api.cors.*`; do not duplicate origin values in Java security code.
- Keep JWT authentication enabled for actual cart, order, and payment requests.
- Do not add `http://0.0.0.0:3000` or wildcard origins.

---

### Task 1: Enable CORS in the shared JWT filter chain

**Files:**
- Modify: `shared-kernel/src/main/java/com/metao/book/shared/security/JwtSecurityAutoConfiguration.java`
- Test: `order-microservice/src/test/java/com/metao/book/order/integration/SecureOrderFlowIT.java`

**Interfaces:**
- Consumes: existing `WebSecurityAutoConfiguration` MVC CORS mappings and `app.api.cors.*` properties.
- Produces: a `SecurityFilterChain` that delegates preflight policy to Spring CORS support before JWT authentication.

- [ ] **Step 1: Write the failing preflight test**

Add a test that sends:

```java
given()
    .header("Origin", "http://localhost:3000")
    .header("Access-Control-Request-Method", "GET")
    .header("Access-Control-Request-Headers", "Authorization")
    .options("/cart")
    .then()
    .statusCode(HttpStatus.OK.value())
    .header("Access-Control-Allow-Origin", "http://localhost:3000")
    .header("Access-Control-Allow-Methods", containsString("GET"))
    .header("Access-Control-Allow-Headers", containsStringIgnoringCase("Authorization"));
```

- [ ] **Step 2: Verify RED**

Run: `./gradlew :order-microservice:test --tests com.metao.book.order.integration.SecureOrderFlowIT.shouldAllowConfiguredCorsPreflightWithoutAuthentication --no-parallel`

Expected: FAIL because the preflight receives `401` before MVC CORS handling.

- [ ] **Step 3: Enable standard Spring Security CORS integration**

Import `org.springframework.security.config.Customizer` and add this to the existing `HttpSecurity` chain before authorization rules:

```java
.cors(Customizer.withDefaults())
```

- [ ] **Step 4: Verify GREEN**

Run the focused Gradle command from Step 2.

Expected: PASS with status `200` and the configured CORS response headers.

- [ ] **Step 5: Verify runtime behavior**

Run: `docker compose up -d --build order-microservice`

Then repeat the unauthenticated `OPTIONS /cart` preflight with origin `http://localhost:3000`, method `GET`, and requested header `authorization`.

Expected: `200`, `Access-Control-Allow-Origin: http://localhost:3000`, and authorization listed in allowed headers.

- [ ] **Step 6: Commit**

```bash
git add shared-kernel/src/main/java/com/metao/book/shared/security/JwtSecurityAutoConfiguration.java order-microservice/src/test/java/com/metao/book/order/integration/SecureOrderFlowIT.java docs/superpowers/plans/2026-08-16-security-filter-chain-cors.md
git commit -m "fix: allow authenticated CORS preflights"
```

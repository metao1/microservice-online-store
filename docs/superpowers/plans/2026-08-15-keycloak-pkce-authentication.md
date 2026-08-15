# Keycloak PKCE Authentication Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace simulated frontend identity with a real Keycloak Authorization Code + PKCE flow, make JWT `sub` the sole customer owner, and verify registration through purchase with a real browser E2E test.

**Architecture:** Keycloak owns credentials, registration, sessions, and themed login pages. React uses `keycloak-js` with in-memory tokens and injects refreshed bearer tokens into protected Axios clients; Spring services validate issuer, signature, expiry, audience, roles, and scopes before controllers derive ownership from `sub`. Docker Compose and Playwright exercise the complete Keycloak → frontend → order → payment → inventory path.

**Tech Stack:** Keycloak 26.4.2, `keycloak-js` 26.2.2, React 18, TypeScript, Axios, Playwright, Spring Boot/Security OAuth2 Resource Server, Java 25, Docker Compose, PostgreSQL, Kafka.

## Global Constraints

- Authorization Code flow MUST require PKCE S256; implicit and direct-access password grants MUST remain disabled.
- Access and refresh tokens MUST remain in memory and MUST NOT be written to local storage or session storage.
- Keycloak MUST be the only component that receives user passwords.
- Customer ownership MUST come exclusively from the validated JWT `sub` claim.
- New self-registered users MUST receive `CUSTOMER`; `ADMIN` MUST be assigned only by an administrator or deterministic E2E realm fixture.
- The access token MUST contain the `bookstore-api` audience.
- Product catalog reads remain public; cart, account, orders, checkout, and payment operations remain protected.
- Missing or invalid authentication MUST return 401; valid authentication with insufficient authority MUST return 403.
- Browser E2E tests MUST run serially and MUST NOT persist tokens or expose credentials in traces, screenshots, or logs.
- Preserve unrelated user changes and keep each authentication commit limited to the files listed for its task.

---

### Task 1: Reproducible Keycloak Realm and Compose Service

**Files:**
- Create: `infrastructure/keycloak/realm/bookstore-realm.json`
- Create: `infrastructure/keycloak/.env.e2e.example`
- Modify: `docker-compose.yml`
- Modify: `frontend/.env.example`
- Test: `infrastructure/keycloak/realm/bookstore-realm.test.mjs`

**Interfaces:**
- Produces: realm `bookstore`, public client `bookstore-frontend`, audience `bookstore-api`, roles `CUSTOMER` and `ADMIN`, and deterministic users `e2e-customer` and `e2e-admin`.
- Consumes: local frontend URL `http://localhost:3000` and service issuer URL `http://keycloak:8080/realms/bookstore` inside Compose.

- [ ] **Step 1: Add a failing structural realm test**

Create a Node test that parses the realm JSON and asserts exact security invariants:

```javascript
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const realm = JSON.parse(readFileSync(new URL('./bookstore-realm.json', import.meta.url), 'utf8'));
const frontend = realm.clients.find((client) => client.clientId === 'bookstore-frontend');

test('realm requires PKCE and disables unsafe browser grants', () => {
  assert.equal(realm.realm, 'bookstore');
  assert.equal(realm.registrationAllowed, true);
  assert.equal(frontend.publicClient, true);
  assert.equal(frontend.standardFlowEnabled, true);
  assert.equal(frontend.implicitFlowEnabled, false);
  assert.equal(frontend.directAccessGrantsEnabled, false);
  assert.equal(frontend.attributes['pkce.code.challenge.method'], 'S256');
  assert.deepEqual(frontend.redirectUris, ['http://localhost:3000/*']);
  assert.deepEqual(frontend.webOrigins, ['http://localhost:3000']);
});

test('realm separates customer and administrator access', () => {
  const roleNames = realm.roles.realm.map((role) => role.name);
  assert.ok(roleNames.includes('CUSTOMER'));
  assert.ok(roleNames.includes('ADMIN'));
  assert.ok(realm.defaultRole.composite);
  assert.ok(realm.defaultRole.composites.realm.includes('CUSTOMER'));
});
```

- [ ] **Step 2: Run the realm test and confirm RED**

Run: `node --test infrastructure/keycloak/realm/bookstore-realm.test.mjs`

Expected: FAIL because `bookstore-realm.json` does not exist.

- [ ] **Step 3: Add the minimal realm import**

Create `bookstore-realm.json` with these exact client settings and protocol mapper:

```json
{
  "realm": "bookstore",
  "enabled": true,
  "registrationAllowed": true,
  "resetPasswordAllowed": true,
  "loginTheme": "bookstore",
  "roles": {
    "realm": [
      { "name": "CUSTOMER" },
      { "name": "ADMIN" }
    ]
  },
  "defaultRole": {
    "name": "default-roles-bookstore",
    "composite": true,
    "composites": { "realm": ["CUSTOMER"] }
  },
  "clients": [
    {
      "clientId": "bookstore-frontend",
      "enabled": true,
      "publicClient": true,
      "standardFlowEnabled": true,
      "implicitFlowEnabled": false,
      "directAccessGrantsEnabled": false,
      "redirectUris": ["http://localhost:3000/*"],
      "webOrigins": ["http://localhost:3000"],
      "attributes": {
        "pkce.code.challenge.method": "S256",
        "post.logout.redirect.uris": "http://localhost:3000/*"
      },
      "protocolMappers": [
        {
          "name": "bookstore-api-audience",
          "protocol": "openid-connect",
          "protocolMapper": "oidc-audience-mapper",
          "config": {
            "included.custom.audience": "bookstore-api",
            "access.token.claim": "true"
          }
        }
      ]
    }
  ]
}
```

Add E2E users with non-production credentials sourced from the checked-in test fixture; document that deployed credentials are supplied separately. Give only the administrator user the explicit `ADMIN` realm role.

- [ ] **Step 4: Add the Compose Keycloak service**

Add a pinned service using:

```yaml
keycloak:
  image: quay.io/keycloak/keycloak:26.4.2
  command: ["start-dev", "--import-realm", "--health-enabled=true"]
  environment:
    KC_BOOTSTRAP_ADMIN_USERNAME: ${KEYCLOAK_ADMIN_USERNAME:-admin}
    KC_BOOTSTRAP_ADMIN_PASSWORD: ${KEYCLOAK_ADMIN_PASSWORD:-admin}
  ports:
    - "8080:8080"
    - "9000:9000"
  volumes:
    - ./infrastructure/keycloak/realm:/opt/keycloak/data/import:ro
    - ./infrastructure/keycloak/themes/bookstore:/opt/keycloak/themes/bookstore:ro
  healthcheck:
    test: ["CMD-SHELL", "exec 3<>/dev/tcp/127.0.0.1/9000 && printf 'GET /health/ready HTTP/1.1\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3 && grep -q '200 OK' <&3"]
    interval: 5s
    timeout: 3s
    retries: 30
```

Set order/payment `JWT_ISSUER_URI` to `http://keycloak:8080/realms/bookstore` and `JWT_AUDIENCE` to `bookstore-api`; add `keycloak` health dependencies. Add Vite variables `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, and `VITE_KEYCLOAK_CLIENT_ID` to `.env.example`.

- [ ] **Step 5: Run structural and Compose validation**

Run:

```bash
node --test infrastructure/keycloak/realm/bookstore-realm.test.mjs
docker compose config --quiet
```

Expected: both commands exit 0.

- [ ] **Step 6: Commit the realm and Compose boundary**

```bash
git add docker-compose.yml frontend/.env.example infrastructure/keycloak
git commit -m "feat: add local Keycloak PKCE realm"
```

---

### Task 2: Storefront Keycloak Theme

**Files:**
- Create: `infrastructure/keycloak/themes/bookstore/login/theme.properties`
- Create: `infrastructure/keycloak/themes/bookstore/login/resources/css/login.css`
- Create: `infrastructure/keycloak/themes/bookstore/login/messages/messages_en.properties`
- Test: `infrastructure/keycloak/themes/bookstore/login/theme.test.mjs`

**Interfaces:**
- Consumes: Keycloak realm `loginTheme: bookstore` from Task 1.
- Produces: stable form semantics (`#username`, `#password`, `#kc-login`, registration link) styled using the current `AccountPage.css` visual language.

- [ ] **Step 1: Add a failing structural theme test**

```javascript
import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

test('theme inherits supported Keycloak markup and loads storefront styles', () => {
  const root = new URL('./', import.meta.url);
  const properties = readFileSync(new URL('theme.properties', root), 'utf8');
  const css = readFileSync(new URL('resources/css/login.css', root), 'utf8');
  assert.match(properties, /^parent=keycloak\.v2$/m);
  assert.match(properties, /^styles=css\/login\.css$/m);
  assert.match(css, /#kc-login/);
  assert.match(css, /:focus-visible/);
  assert.match(css, /@media/);
});
```

- [ ] **Step 2: Run the theme test and confirm RED**

Run: `node --test infrastructure/keycloak/themes/bookstore/login/theme.test.mjs`

Expected: FAIL because the theme files do not exist.

- [ ] **Step 3: Add a child theme without copying Keycloak templates**

Use `keycloak.v2` as the parent to preserve supported login and registration forms:

```properties
parent=keycloak.v2
import=common/keycloak
styles=css/login.css
locales=en
```

Port the layout, typography, field, primary-button, error, focus, and responsive styles from `frontend/src/pages/AccountPage.css` into scoped Keycloak selectors. Do not copy React or collect credentials in the SPA. Override display strings only through `messages_en.properties`, including `loginAccountTitle=Sign in to ModernStore` and `registerTitle=Create your ModernStore account`.

- [ ] **Step 4: Validate static theme assets**

Run:

```bash
test -f infrastructure/keycloak/themes/bookstore/login/theme.properties
test -f infrastructure/keycloak/themes/bookstore/login/resources/css/login.css
rg -n "parent=keycloak.v2|styles=css/login.css" infrastructure/keycloak/themes/bookstore/login/theme.properties
node --test infrastructure/keycloak/themes/bookstore/login/theme.test.mjs
```

Expected: all commands exit 0.

- [ ] **Step 5: Commit the theme**

```bash
git add infrastructure/keycloak/themes
git commit -m "feat: theme Keycloak authentication pages"
```

---

### Task 3: Standards-Compliant Resource Server Validation

**Files:**
- Create: `shared-kernel/src/main/java/com/metao/book/shared/security/AudienceValidator.java`
- Create: `shared-kernel/src/main/java/com/metao/book/shared/security/KeycloakJwtAuthoritiesConverter.java`
- Create: `shared-kernel/src/test/java/com/metao/book/shared/security/AudienceValidatorTest.java`
- Create: `shared-kernel/src/test/java/com/metao/book/shared/security/KeycloakJwtAuthoritiesConverterTest.java`
- Modify: `shared-kernel/src/main/java/com/metao/book/shared/security/JwtSecurityAutoConfiguration.java`
- Test: `order-microservice/src/test/java/com/metao/book/order/integration/SecureOrderFlowIT.java`
- Test: `payment-microservice/src/test/java/com/metao/book/payment/presentation/PaymentAggregateControllerIT.java`

**Interfaces:**
- Produces: `AudienceValidator implements OAuth2TokenValidator<Jwt>` and `KeycloakJwtAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>>`.
- Consumes: `JwtSecurityProperties.issuerUri` and `.audience`.

- [ ] **Step 1: Write failing audience and authority tests**

Cover an accepted `bookstore-api` audience, rejected missing audience, nested `realm_access.roles`, compatible top-level `roles`, and a space-delimited `scope` claim. Use assertions such as:

```java
assertThat(new AudienceValidator("bookstore-api").validate(jwt).hasErrors()).isFalse();
assertThat(converter.convert(jwt)).extracting(GrantedAuthority::getAuthority)
    .containsExactlyInAnyOrder("ROLE_CUSTOMER", "SCOPE_orders:read", "SCOPE_cart:write");
```

- [ ] **Step 2: Run focused tests and confirm RED**

Run:

```bash
./gradlew :shared-kernel:test \
  --tests com.metao.book.shared.security.AudienceValidatorTest \
  --tests com.metao.book.shared.security.KeycloakJwtAuthoritiesConverterTest
```

Expected: compilation fails because both production classes are missing.

- [ ] **Step 3: Implement the audience validator**

Return success when audience validation is disabled or `jwt.getAudience()` contains the configured value; otherwise return an `OAuth2Error` with code `invalid_token`:

```java
public OAuth2TokenValidatorResult validate(Jwt jwt) {
    if (audience == null || audience.isBlank() || jwt.getAudience().contains(audience)) {
        return OAuth2TokenValidatorResult.success();
    }
    return OAuth2TokenValidatorResult.failure(
        new OAuth2Error("invalid_token", "Required audience is missing", null)
    );
}
```

- [ ] **Step 4: Implement Keycloak authority extraction**

Extract role names from `realm_access.roles`, merge compatible top-level `roles`, split the standard `scope` string on whitespace, deduplicate, and prefix roles/scopes with `ROLE_`/`SCOPE_`. Treat missing or incorrectly typed claims as empty collections.

- [ ] **Step 5: Configure issuer and audience validators in the decoder**

Build with `JwtDecoders.fromIssuerLocation(properties.getIssuerUri())`, then set a `DelegatingOAuth2TokenValidator` combining `JwtValidators.createDefaultWithIssuer(...)` and `AudienceValidator`. Remove the manual audience exception from the authentication converter so invalid tokens fail at authentication with 401.

- [ ] **Step 6: Run shared and service security tests**

Run:

```bash
./gradlew :shared-kernel:test :order-microservice:test :payment-microservice:test --no-parallel
```

Expected: BUILD SUCCESSFUL; existing mocked decoders continue to isolate service tests from Keycloak.

- [ ] **Step 7: Commit resource-server validation**

```bash
git add shared-kernel/src/main/java/com/metao/book/shared/security shared-kernel/src/test/java/com/metao/book/shared/security order-microservice/src/test payment-microservice/src/test
git commit -m "fix: validate Keycloak JWT claims"
```

---

### Task 4: Remove Caller-Controlled Customer Ownership

**Files:**
- Delete: `order-microservice/src/main/java/com/metao/book/order/presentation/dto/CreateOrderRequestDTO.java`
- Modify: `order-microservice/src/main/java/com/metao/book/order/presentation/controller/OrderManagementController.java`
- Modify: all Java tests constructing `CreateOrderRequestDTO`
- Modify: `docs/API.md`
- Test: `order-microservice/src/test/java/com/metao/book/order/integration/SecureOrderFlowIT.java`

**Interfaces:**
- Produces: `POST /api/order` with no customer identifier, `GET /api/order/me`, `GET /api/order/me/paged`, and existing user-neutral `/cart` routes.
- Consumes: authenticated `CurrentUser.subject()` from the shared security layer.

- [ ] **Step 1: Add failing body-free order creation and ownership override tests**

First authenticate as `USER_ID` and call `POST /api/order` with no body; expect HTTP 201. Then submit `{"user_id":"victim-user"}` in a separate authenticated request, load the created aggregate, and assert:

```java
assertThat(savedOrder.getUserId()).isEqualTo(UserId.of(USER_ID));
assertThat(savedOrder.getUserId()).isNotEqualTo(UserId.of("victim-user"));
```

Also retain missing-token 401 and two-user list isolation assertions.

- [ ] **Step 2: Run the secure flow test and confirm the contract mismatch**

Run: `./gradlew :order-microservice:test --tests com.metao.book.order.integration.SecureOrderFlowIT`

Expected before implementation: the body-free request returns HTTP 400 because the legacy `@RequestBody CreateOrderRequestDTO` is still required.

- [ ] **Step 3: Remove the request DTO from order creation**

Change the controller method to:

```java
@PostMapping
@ResponseStatus(HttpStatus.CREATED)
@PreAuthorize("hasRole('CUSTOMER') or hasAuthority('SCOPE_orders:write')")
public OrderId createOrder() {
    return createOrderUseCase.createOrder(UserId.of(CurrentUser.subject()));
}
```

Delete `CreateOrderRequestDTO`; update all tests to send no body. Preserve domain/application `UserId` parameters because they are now populated only by the trusted adapter.

- [ ] **Step 4: Verify cart controllers have no owner inputs**

Keep only `/cart`, `/cart/items`, and `/cart/items/{sku}`. Add controller assertions that a payload field named `user_id` is ignored and cannot change the persisted owner.

- [ ] **Step 5: Run the order suite**

Run: `./gradlew :order-microservice:test --no-parallel`

Expected: BUILD SUCCESSFUL and both users remain isolated.

- [ ] **Step 6: Commit the ownership boundary**

```bash
git add order-microservice docs/API.md
git commit -m "fix: derive customer ownership from JWT subject"
```

---

### Task 5: Keycloak-Backed React Authentication Provider

**Files:**
- Modify: `frontend/package.json`
- Modify: `frontend/package-lock.json`
- Create: `frontend/src/auth/keycloak.ts`
- Create: `frontend/src/auth/auth.types.ts`
- Create: `frontend/src/auth/ProtectedRoute.tsx`
- Create: `frontend/src/context/AuthContext.test.tsx`
- Modify: `frontend/src/context/AuthContext.tsx`
- Modify: `frontend/src/App.tsx`
- Modify: `frontend/src/pages/AccountPage.tsx`
- Modify: `frontend/src/vite-env.d.ts`

**Interfaces:**
- Produces:

```typescript
interface AuthContextValue {
  initialized: boolean;
  isAuthenticated: boolean;
  user: AuthenticatedUser | null;
  login(returnTo?: string): Promise<void>;
  register(returnTo?: string): Promise<void>;
  logout(): Promise<void>;
  getAccessToken(minValidity?: number): Promise<string | undefined>;
}
```

- Consumes: `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, and `VITE_KEYCLOAK_CLIENT_ID`.

- [ ] **Step 1: Install the pinned browser adapter**

Run: `cd frontend && npm install keycloak-js@26.2.2`

Expected: `package.json` and lockfile contain exact compatible dependency metadata.

- [ ] **Step 2: Write failing provider tests**

Mock only the adapter boundary. Verify `init({ onLoad: 'check-sso', pkceMethod: 'S256' })`, profile mapping from `sub`/`email`/`name`, return URLs for login/register, logout redirect, and `updateToken(30)` before returning the current token.

- [ ] **Step 3: Run the provider tests and confirm RED**

Run: `cd frontend && npm run test:run -- src/context/AuthContext.test.tsx`

Expected: FAIL because the Keycloak adapter and new context interface do not exist.

- [ ] **Step 4: Add the singleton adapter and provider**

Initialize one `Keycloak` instance:

```typescript
export const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID,
});
```

Call `init` once from `AuthProvider`, normalize `tokenParsed` into `AuthenticatedUser`, expose the approved interface, and clear user state when refresh fails. Do not read or write browser storage.

- [ ] **Step 5: Add protected navigation**

`ProtectedRoute` waits for initialization, invokes `login(location.pathname + location.search)` once for anonymous users, and renders children only when authenticated. Protect `/cart`, `/orders`, and authenticated account details while leaving `/`, `/products`, and `/products/:sku` public.

- [ ] **Step 6: Remove the demo user and local password form**

Delete `defaultUser` from `App.tsx`. Make `AccountPage` call context `login`, `register`, and `logout` and render only token-derived profile information. Keep passwords out of React state and DOM.

- [ ] **Step 7: Run frontend unit and build checks**

Run:

```bash
cd frontend
npm run test:run
npm run build
```

Expected: all Vitest tests pass and TypeScript/Vite build succeeds.

- [ ] **Step 8: Commit the React authentication boundary**

```bash
git add frontend/package.json frontend/package-lock.json frontend/src/auth frontend/src/context frontend/src/App.tsx frontend/src/pages/AccountPage.tsx frontend/src/vite-env.d.ts
git commit -m "feat: authenticate storefront with Keycloak PKCE"
```

---

### Task 6: Bearer Propagation and User-Neutral Frontend APIs

**Files:**
- Create: `frontend/src/services/authenticatedAxios.ts`
- Create: `frontend/src/services/authenticatedAxios.test.ts`
- Modify: `frontend/src/services/api.types.ts`
- Modify: `frontend/src/services/api.ts`
- Modify: `frontend/src/services/api.mock.ts`
- Modify: `frontend/src/hooks/useCart.ts`
- Modify: `frontend/src/hooks/useOrders.ts`
- Modify: `frontend/src/hooks/useCheckout.ts`
- Modify: `frontend/src/context/CartContext.tsx`
- Modify: `frontend/src/pages/CartPage.tsx`
- Modify: `frontend/src/pages/OrdersPage.tsx`

**Interfaces:**
- Produces: `configureAuthentication(getAccessToken, login)` and API methods with no customer identifier:

```typescript
getCart(): Promise<Cart>;
addToCart(sku: string, productTitle: string, quantity: number, price: number, currency: string): Promise<Cart>;
removeFromCart(sku: string): Promise<Cart>;
updateCartItem(sku: string, quantity: number, price: number, currency: string): Promise<Cart>;
createOrder(): Promise<Order>;
getOrders(): Promise<Order[]>;
getOrdersPage(limit?: number, offset?: number): Promise<PaginatedResult<Order>>;
```

- Consumes: `AuthContextValue.getAccessToken(30)` and `.login()` from Task 5.

- [ ] **Step 1: Write failing interceptor tests**

Use an Axios mock adapter or custom test adapter to assert that a refreshed token becomes `Authorization: Bearer access-token`, a refresh error rejects without sending, one 401 invokes login once, and 403 rejects without login.

- [ ] **Step 2: Run focused tests and confirm RED**

Run: `cd frontend && npm run test:run -- src/services/authenticatedAxios.test.ts`

Expected: FAIL because `authenticatedAxios.ts` does not exist.

- [ ] **Step 3: Implement request and response interceptors**

Before a protected request, await `getAccessToken(30)` and attach it. Track a module-local `loginRedirectStarted` guard; on the first 401 call `login(window.location.pathname + window.location.search)`. Never redirect on 403. Reset the guard after successful authentication initialization.

- [ ] **Step 4: Correct all customer-owned endpoint shapes**

Use these requests exactly:

```text
GET    /cart
POST   /cart/items
PUT    /cart/items/{sku}
DELETE /cart/items/{sku}
DELETE /cart
POST   /api/order
GET    /api/order/me
GET    /api/order/me/paged?offset={offset}&limit={limit}
```

Remove every `user_id` body field, `/customer/{userId}` route, `/cart/{userId}` route, and fallback that recreates legacy requests.

- [ ] **Step 5: Remove user identifiers from hooks and contexts**

Change `useCart()` and `useOrders(pageSize)` to depend only on authentication state, make `CartProvider` accept only `children`, and change checkout to `processCheckout(options?)`. Response `userId` may remain display data but must never select a backend owner.

- [ ] **Step 6: Run frontend tests and build**

Run:

```bash
cd frontend
npm run test:run
npm run build
```

Expected: all tests pass, no TypeScript call sites retain the removed parameters, and build succeeds.

- [ ] **Step 7: Commit authenticated API integration**

```bash
git add frontend/src
git commit -m "fix: send authenticated user-neutral API requests"
```

---

### Task 7: Real Keycloak Browser and Purchase E2E

**Files:**
- Create: `docker-compose.e2e.yml`
- Create: `frontend/e2e/auth.setup.ts`
- Create: `frontend/e2e/authentication.spec.ts`
- Create: `frontend/e2e/authenticated-purchase.spec.ts`
- Create: `frontend/e2e/user-isolation.spec.ts`
- Create: `frontend/e2e/authorization.spec.ts`
- Create: `frontend/e2e/support/keycloak.ts`
- Create: `frontend/e2e/support/fixtures.ts`
- Modify: `frontend/playwright.config.ts`
- Modify: `frontend/package.json`
- Modify: `docker-compose.yml`

**Interfaces:**
- Produces: `npm run e2e:auth` as the serial real-stack verification command.
- Consumes: real Keycloak/browser/services from Tasks 1–6 and existing always-successful `SimulatedPaymentGatewayAdapter` under the non-production E2E profile.

- [ ] **Step 1: Add a failing theme and authentication journey**

The test opens `/cart`, expects a Keycloak redirect, asserts the themed login form and registration link, follows `Create account`, registers a unique email, returns to `/cart`, and asserts authenticated navigation. Use semantic form selectors and generated per-run email; never print the password.

- [ ] **Step 2: Add a failing purchase choreography**

Seed SKU `E2EAUTH01` with volume 8 through the inventory API, add quantity 3 through the browser, create an order, assert initial `PENDING_PAYMENT`, poll `/api/order/me` through the browser session until `PAID`, assert cart empty, and query inventory until volume equals 5.

- [ ] **Step 3: Add failing isolation and authorization scenarios**

Create an order as customer A, log out, log in as customer B, and assert A's order/cart is absent. Verify a CUSTOMER receives 403 for `PATCH /api/order/{id}/status`, while the deterministic ADMIN token succeeds.

- [ ] **Step 4: Run the new suite and confirm RED**

Run: `cd frontend && npm run e2e:auth`

Expected before stack/config completion: tests fail at Keycloak readiness, protected redirect, or corrected API calls.

- [ ] **Step 5: Add E2E Compose overrides and deterministic fixtures**

Set `SPRING_PROFILES_ACTIVE=e2e`, disable random test data, retain the existing always-successful simulated gateway, enable Kafka/outbox workers, and add health checks for Keycloak, PostgreSQL, Kafka, inventory, order, payment, and frontend. Do not enable the `real-payment-gateway` profile.

- [ ] **Step 6: Make Playwright orchestration serial and artifact-safe**

Set the authentication project to Chromium, `fullyParallel: false`, `workers: 1`, and `trace: 'retain-on-failure'`. Mask password inputs before screenshots, redact `authorization` request headers from attached logs, and add storage-state files to `.gitignore`. Correct the web server command to `npm run dev -- --host 0.0.0.0`; use an opt-in environment flag when the Compose frontend is already running.

- [ ] **Step 7: Run the real E2E suite**

Run:

```bash
docker compose -f docker-compose.yml -f docker-compose.e2e.yml up -d --build --wait
cd frontend && npm run e2e:auth
docker compose -f docker-compose.yml -f docker-compose.e2e.yml down
```

Expected: registration, login, theme, purchase choreography, logout, user isolation, and role authorization all pass serially.

- [ ] **Step 8: Commit the real browser suite**

```bash
git add docker-compose.yml docker-compose.e2e.yml frontend/e2e frontend/playwright.config.ts frontend/package.json .gitignore
git commit -m "test: verify Keycloak purchase flow end to end"
```

---

### Task 8: Documentation, OpenSpec Completion, and Full Verification

**Files:**
- Modify: `README.md`
- Modify: `docs/API.md`
- Modify: `docs/ARCHITECTURE.md`
- Modify: `frontend/.env.example`
- Modify: `openspec/changes/add-keycloak-pkce-authentication/tasks.md`

**Interfaces:**
- Produces: repeatable local startup, authentication troubleshooting, test-account guidance, and completed OpenSpec evidence.
- Consumes: all verified commands and endpoint contracts from Tasks 1–7.

- [ ] **Step 1: Document the operational flow**

Document Compose startup, Keycloak URLs, realm/client names, environment variables, self-registration, role assignment, frontend callback/logout behavior, API audience, health endpoints, serial E2E command, and clean-state realm import. Explicitly state that test credentials are local-only and tokens/passwords must not be logged.

- [ ] **Step 2: Update API and architecture descriptions**

Replace legacy user-ID routes and payloads with the exact user-neutral endpoints from Task 6. Document `sub` ownership and the browser → Keycloak → resource-server request sequence.

- [ ] **Step 3: Run focused verification from clean test outputs**

```bash
./gradlew cleanTest :shared-kernel:test :order-microservice:test :payment-microservice:test --no-daemon --no-parallel
cd frontend && npm run test:run && npm run build
node --test infrastructure/keycloak/realm/bookstore-realm.test.mjs
docker compose config --quiet
```

Expected: every command exits 0.

- [ ] **Step 4: Run repository-wide and real-stack verification**

```bash
./gradlew cleanTest test --no-daemon --no-parallel
docker compose -f docker-compose.yml -f docker-compose.e2e.yml up -d --build --wait
cd frontend && npm run e2e:auth
docker compose -f docker-compose.yml -f docker-compose.e2e.yml down
```

Expected: backend BUILD SUCCESSFUL, frontend tests/build successful, and Playwright authentication project passes.

- [ ] **Step 5: Validate security hygiene**

```bash
rg -n "localStorage|sessionStorage|grant_type=password|implicitFlowEnabled.*true|directAccessGrantsEnabled.*true" frontend infrastructure/keycloak
git diff --check
```

Expected: the first command finds no prohibited authentication behavior; `git diff --check` emits no output.

- [ ] **Step 6: Run strict OpenSpec validation**

Run: `openspec validate add-keycloak-pkce-authentication --strict`

Expected: validation succeeds. If the CLI remains unavailable, record that environmental limitation without marking OpenSpec task 7.4 complete.

- [ ] **Step 7: Mark OpenSpec tasks complete only from evidence**

Change each checkbox in `openspec/changes/add-keycloak-pkce-authentication/tasks.md` to `[x]` only after its corresponding implementation and verification output succeeded.

- [ ] **Step 8: Commit final documentation and checklist**

```bash
git add README.md docs frontend/.env.example openspec/changes/add-keycloak-pkce-authentication/tasks.md
git commit -m "docs: document Keycloak authentication operations"
```

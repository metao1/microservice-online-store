## 1. Keycloak Environment

- [ ] 1.1 Add a pinned Keycloak service, health check, realm import, persistent local volume, and service JWT environment variables to Docker Compose.
- [ ] 1.2 Configure the `bookstore-frontend` public client for Authorization Code flow with required PKCE S256, explicit redirect/logout URIs, and explicit web origins.
- [ ] 1.3 Configure the `bookstore-api` audience, `CUSTOMER` and `ADMIN` realm roles, automatic `CUSTOMER` registration assignment, password reset, and deterministic E2E users.
- [ ] 1.4 Convert the current frontend authentication design into version-controlled Keycloak login and registration theme assets.

## 2. Resource Server Security

- [ ] 2.1 Add failing shared-security tests for issuer, expiry, audience, realm-role, scope, and subject behavior.
- [ ] 2.2 Replace direct JWK decoder construction with issuer-based decoding and explicit audience validation that produces HTTP 401 on authentication failure.
- [ ] 2.3 Map Keycloak `realm_access.roles`, compatible top-level roles, and standard scopes to Spring authorities.
- [ ] 2.4 Verify public health/catalog behavior and protected order/payment authorization with integration tests.

## 3. Trusted Ownership Boundary

- [ ] 3.1 Add failing tests proving customer ownership cannot be supplied through order or cart request bodies or paths.
- [ ] 3.2 Remove `userId` from `CreateOrderRequestDTO` and update order creation tests and documentation.
- [ ] 3.3 Ensure all cart and customer-order controllers use only `CurrentUser.subject()` and retain administrator-only status operations.
- [ ] 3.4 Add two-user integration tests proving cart and order isolation.

## 4. React PKCE Integration

- [ ] 4.1 Add `keycloak-js` and environment configuration for realm, client, and Keycloak URL.
- [ ] 4.2 Add failing frontend tests for initialization, login, registration, logout, profile claims, and protected navigation.
- [ ] 4.3 Replace the simulated authentication context and demo user with a single Keycloak-backed provider using `check-sso` and PKCE S256.
- [ ] 4.4 Replace the password-collecting account page with authenticated profile state and Keycloak login, registration, and logout actions.
- [ ] 4.5 Protect cart, orders, account, and checkout while preserving anonymous product browsing and return-to-original-route behavior.

## 5. Authenticated API Calls

- [ ] 5.1 Add failing API-client tests for refresh-before-request, bearer injection, refresh failure, 401 redirect-loop prevention, and 403 handling.
- [ ] 5.2 Add the authentication-aware Axios interceptor and apply it only to protected service calls.
- [ ] 5.3 Remove `userId` parameters and payload fields from frontend cart and order contracts, hooks, contexts, and pages.
- [ ] 5.4 Update frontend API routes to the user-neutral cart and `/api/order/me` endpoints.

## 6. Real End-to-End Verification

- [ ] 6.1 Add deterministic product and successful-payment E2E configuration without changing production defaults.
- [ ] 6.2 Add serial Playwright setup for Compose health checks and test-user lifecycle.
- [ ] 6.3 Add a real self-registration/login/logout test through the themed Keycloak pages.
- [ ] 6.4 Add a browser checkout test covering cart, order creation, `PENDING_PAYMENT`, eventual `PAID`, and inventory reduction.
- [ ] 6.5 Add cross-user isolation and CUSTOMER-versus-ADMIN authorization tests.
- [ ] 6.6 Verify tokens and credentials are redacted from logs and test artifacts.

## 7. Documentation and Final Verification

- [ ] 7.1 Document local Keycloak startup, realm configuration, frontend variables, test accounts, registration, and troubleshooting.
- [ ] 7.2 Update API and architecture documentation to remove caller-supplied customer identifiers and describe JWT subject ownership.
- [ ] 7.3 Run shared-security, order, payment, frontend unit, and Playwright E2E suites.
- [ ] 7.4 Run the complete backend and frontend build/test suites and strict OpenSpec validation.


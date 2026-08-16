## Context

Order and payment services already act as OAuth2 resource servers and derive some ownership from `CurrentUser.subject()`, but their integration tests mock `JwtDecoder`. The React application creates a demo user locally, displays a password form that does not authenticate, omits bearer tokens from API requests, and still sends user IDs in customer-owned operations. Docker Compose has no identity provider, so issuer discovery, signing keys, PKCE, registration, logout, role mapping, and token propagation are not exercised end to end.

## Goals / Non-Goals

### Goals

- Authenticate browser users through a real Keycloak Authorization Code + PKCE S256 flow.
- Preserve the existing login and registration visual design as a Keycloak theme without exposing credentials to React.
- Enable self-service registration with automatic `CUSTOMER` access and administrator-managed `ADMIN` access.
- Keep tokens in memory and refresh them before protected API requests.
- Make the JWT `sub` claim the sole source of customer ownership.
- Verify the complete browser, Keycloak, order, payment, Kafka, and inventory flow with deterministic E2E tests.

### Non-Goals

- Building a custom credential or token service.
- Adding an API gateway or backend-for-frontend.
- Persisting access or refresh tokens in local storage.
- Implementing social identity providers, MFA, or production Keycloak clustering.
- Moving product catalog reads behind authentication.

## Decisions

### Keycloak topology

Docker Compose will run Keycloak with a version-controlled `bookstore` realm import and custom theme. The realm will contain:

- a public `bookstore-frontend` client with standard flow enabled, implicit and direct-access grants disabled, PKCE S256 required, and explicit local redirect/logout URIs and web origins;
- a `bookstore-api` audience included in access tokens;
- realm roles `CUSTOMER` and `ADMIN`, with `CUSTOMER` assigned to newly registered users;
- self-registration and password reset enabled;
- deterministic E2E customer and administrator accounts whose credentials come from test-only environment configuration.

Deployed environments will override public URLs and secrets through environment variables. Production credentials will not be committed.

### Browser authentication boundary

`keycloak-js` will own authorization redirects, code exchange, token refresh, and logout. React will initialize it once with `check-sso` and `pkceMethod: "S256"`. `AuthContext` will expose authentication state, normalized profile claims, `login`, `register`, `logout`, and a token refresh accessor. It will not accept an arbitrary initial user outside test adapters.

Tokens remain only in the Keycloak adapter's in-memory state. Public product routes render anonymously. Cart, account, orders, and checkout use a protected-route/action boundary that starts login and returns to the original URL.

### API client and ownership

A single authentication-aware Axios interceptor will call `updateToken(30)` before protected requests and attach the current bearer token. A 401 will cause at most one login redirect; a 403 will be surfaced as an authorization error without retry.

Frontend API contracts will stop accepting `userId` for cart and order operations and will use the existing user-neutral routes. `CreateOrderRequestDTO` will no longer contain `userId`. Resource ownership will be derived exclusively from the validated JWT `sub` claim at the HTTP adapter boundary. Domain and application services may continue accepting an explicit `UserId` passed by that trusted adapter.

Authenticated customers may process only payments whose stored user identifier matches the validated JWT `sub`. A cross-customer processing attempt returns HTTP 403. Administrators may process any customer's payment. The ownership check occurs before payment state changes.

### Resource-server validation

The shared security auto-configuration will validate the public issuer that appears in browser-issued tokens. Local Compose uses `http://localhost:8080/realms/bookstore` as that issuer, while backend containers fetch signing keys from the separately configured internal URL `http://keycloak:8080/realms/bookstore/protocol/openid-connect/certs`. Environments that can reach the issuer directly may omit the internal JWK Set URI and use issuer discovery. OAuth2 token validators ensure invalid issuer, signature, expiry, or audience is rejected as authentication failure with HTTP 401. Keycloak realm roles will be read from `realm_access.roles`; scopes will be read from the standard space-delimited `scope` claim. The existing top-level role claim may remain as a compatibility input during migration.

Business endpoints require authentication and their declared authorities. Health endpoints remain public. Product catalog reads remain public. Customer tokens cannot invoke administrator-only operations.

### Keycloak theme

The existing account-page visual language will be implemented as Keycloak login and registration templates/styles. React's account page will display authenticated profile information or buttons that redirect to Keycloak; it will not render password inputs. Keycloak continues to own validation, password reset, session, and registration errors.

## Request Flow

1. React initializes Keycloak with `check-sso`.
2. Anonymous users may browse products.
3. A protected route or action calls Keycloak login or registration with the current URL as the return target.
4. Keycloak renders the custom theme and authenticates the user.
5. Keycloak redirects with an authorization code; `keycloak-js` exchanges it using the PKCE verifier.
6. React derives display identity from token claims.
7. Before a protected API call, the interceptor refreshes a near-expiry token and attaches it as a bearer token.
8. The resource server validates the token and derives customer ownership from `sub`.
9. Logout uses Keycloak's end-session endpoint and returns to the storefront.

## Error Handling

- Missing, malformed, expired, incorrectly signed, wrong-issuer, or wrong-audience tokens return 401.
- Valid tokens without required roles or scopes return 403.
- A refresh failure clears local authentication state and starts a fresh login when the user next requests a protected operation.
- Frontend code performs at most one automatic authentication redirect for a failed request to prevent redirect loops.
- Tokens and passwords are excluded from application logs, Playwright traces, screenshots, and error messages.

## Testing

### Frontend unit tests

- Keycloak initialization and anonymous/authenticated state.
- Login, registration, logout, and protected-route return URLs.
- Refresh-before-request and bearer-token injection.
- Refresh failure, 401 redirect-loop protection, and 403 presentation.
- Removal of user IDs from customer-owned API calls.

### Backend integration tests

- Valid issuer, audience, realm role, scope, and subject extraction.
- Missing, expired, malformed, wrong-issuer, and wrong-audience tokens return 401.
- Insufficient authority returns 403.
- Cart and order ownership always follows `sub` and cannot be overridden in a payload or URL.
- Customers can process their own payments, cannot process another customer's payment, and administrators can process any payment.

### Real end-to-end tests

Playwright will run serially against the Compose stack and actual themed Keycloak pages. It will verify anonymous catalog access, self-registration, login callback, authenticated cart operations, order creation, `PENDING_PAYMENT`, eventual `PAID`, inventory reduction, logout, unauthenticated rejection, cross-user isolation, and customer/admin authorization. The E2E profile will use deterministic product fixtures and a payment adapter configured to succeed.

## Risks / Trade-offs

- Browser E2E tests are slower and depend on multiple containers. Mitigation: keep authentication unit/integration tests fast and run the real suite serially as a dedicated task.
- Keycloak realm exports can contain unstable generated fields. Mitigation: maintain a minimal reviewed import and test clean bootstrap.
- Theme markup can change across Keycloak major versions. Mitigation: pin the container version and keep selectors based on stable form semantics.
- In-memory tokens require re-authentication after a full reload if SSO cannot be checked. Mitigation: `check-sso` restores the session through Keycloak without persisting tokens in application storage.

## Migration Plan

1. Add Keycloak configuration and theme without enabling frontend redirects.
2. Harden backend JWT validation and keep mocked integration tests passing.
3. Remove client-controlled ownership fields and update API tests.
4. Enable the React PKCE provider and authenticated API client.
5. Add the real Compose/Playwright E2E suite.
6. Update local setup and rollout documentation.

Rollback can disable JWT enforcement through the existing configuration flag for local recovery, but customer-owned legacy endpoints must not be restored in a form that trusts caller-supplied user IDs.

## ADDED Requirements

### Requirement: Keycloak OpenID Connect Provider
The local system SHALL provide a version-pinned Keycloak realm named `bookstore` with a public browser client, a dedicated API audience, explicit local redirect and logout URIs, and explicit web origins.

#### Scenario: Clean local identity bootstrap
- **WHEN** the local Compose stack starts without existing Keycloak state
- **THEN** the `bookstore` realm, browser client, API audience, roles, theme, and E2E users are available without manual console configuration

### Requirement: Browser PKCE Authentication
The frontend SHALL authenticate through Authorization Code flow with PKCE S256 using `keycloak-js`, and SHALL keep access and refresh tokens out of persistent browser storage.

#### Scenario: User signs in
- **WHEN** an anonymous user requests login from the storefront
- **THEN** the browser is redirected to Keycloak and returns authenticated after a successful PKCE code exchange

#### Scenario: Browser reload restores SSO
- **WHEN** an authenticated Keycloak session exists and the storefront reloads
- **THEN** the frontend restores authentication through `check-sso` without reading tokens from local or session storage

### Requirement: Themed Self-Service Registration
Keycloak SHALL render login and registration pages using the storefront authentication design, SHALL allow self-service registration, and SHALL grant new users `CUSTOMER` access without granting administrator access.

#### Scenario: Customer registration
- **WHEN** a visitor completes valid registration through the themed Keycloak page
- **THEN** Keycloak creates the account, assigns customer access, and returns the user to the storefront authenticated

### Requirement: Token Validation and Authority Mapping
Resource servers SHALL validate JWT signature, issuer, expiry, and the `bookstore-api` audience, and SHALL map Keycloak realm roles and OAuth scopes to Spring authorities.

#### Scenario: Valid customer token
- **WHEN** a request contains a valid token with the expected issuer, audience, subject, and `CUSTOMER` realm role
- **THEN** the service authenticates the subject and grants customer-authorized operations

#### Scenario: Invalid token
- **WHEN** a token is missing, malformed, expired, incorrectly signed, issued by another issuer, or lacks the expected audience
- **THEN** the protected endpoint responds with HTTP 401

#### Scenario: Insufficient authority
- **WHEN** a valid customer token invokes an administrator-only operation
- **THEN** the endpoint responds with HTTP 403

### Requirement: JWT Subject Ownership
Customer-owned cart and order HTTP operations SHALL derive ownership exclusively from the validated JWT `sub` claim and SHALL NOT accept a caller-selected owner in the request body or path.

#### Scenario: Authenticated cart and order operation
- **WHEN** an authenticated customer changes a cart or creates or lists orders
- **THEN** the operation uses the token subject as the customer identifier

#### Scenario: Cross-user isolation
- **WHEN** one authenticated customer uses customer-facing endpoints after another customer created cart or order data
- **THEN** the first customer cannot read or mutate the other customer's data

### Requirement: Payment Processing Ownership
The payment service SHALL allow an authenticated customer to process a payment only when the payment owner matches the validated JWT `sub`, SHALL reject cross-customer processing with HTTP 403, and SHALL allow an administrator to process any customer's payment.

#### Scenario: Customer processes owned payment
- **WHEN** an authenticated customer processes a payment owned by the token subject
- **THEN** the payment service processes the payment

#### Scenario: Customer processes another customer's payment
- **WHEN** an authenticated customer attempts to process a payment owned by a different subject
- **THEN** the payment service responds with HTTP 403 before changing payment state

#### Scenario: Administrator processes customer payment
- **WHEN** an authenticated administrator processes any customer's payment
- **THEN** the payment service processes the payment without applying the customer ownership restriction

### Requirement: Token-Aware Frontend API Calls
The frontend SHALL refresh near-expiry tokens before protected requests, attach the active access token as a bearer token, and avoid infinite authentication redirects.

#### Scenario: Protected request with near-expiry token
- **WHEN** the frontend calls a protected API and the active token expires within 30 seconds
- **THEN** it refreshes the token before sending the request with the resulting bearer token

#### Scenario: Refresh failure
- **WHEN** token refresh fails
- **THEN** the frontend clears authenticated state and requires a fresh login without persisting the failed token

### Requirement: Public and Protected User Experience
The storefront SHALL allow anonymous product browsing while requiring authentication for cart, account, orders, and checkout, and SHALL return users to their intended route after login.

#### Scenario: Anonymous product browsing
- **WHEN** an anonymous visitor opens a product listing or product detail page
- **THEN** the page is available without an authentication redirect

#### Scenario: Anonymous protected navigation
- **WHEN** an anonymous visitor opens cart, account details, orders, or checkout
- **THEN** the visitor is sent through Keycloak login and returned to the requested route after authentication

### Requirement: Real Authentication End-to-End Verification
The project SHALL provide a serial browser test using the real Keycloak, frontend, databases, Kafka, and backend services.

#### Scenario: Authenticated purchase choreography
- **WHEN** a newly registered customer signs in, adds a deterministic product, and creates an order
- **THEN** the order begins pending payment, eventually becomes paid, the customer's cart clears, and inventory is reduced exactly once

#### Scenario: Logout protection
- **WHEN** an authenticated customer logs out through Keycloak
- **THEN** subsequent protected API requests without a new login respond with HTTP 401

# Change: Add Keycloak PKCE authentication

## Why
The backend currently validates JWTs only through mocked decoders in integration tests, while the frontend simulates login and continues to pass client-controlled user identifiers. This leaves the real browser-to-identity-provider-to-resource-server flow unverified and allows frontend code to model ownership independently from the authenticated JWT subject.

## What Changes
- Add a local Keycloak service with a versioned `bookstore` realm, public PKCE client, API audience, roles, self-registration, and deterministic E2E users.
- Convert the existing storefront login and registration design into a Keycloak theme so Keycloak remains the sole credential handler.
- Integrate `keycloak-js` into the React application using Authorization Code flow with PKCE S256 and in-memory tokens.
- Attach refreshed bearer tokens to protected API calls and remove client-controlled user identifiers from cart and order requests.
- Harden Spring resource-server validation for issuer, signature, expiry, audience, Keycloak realm roles, and OAuth scopes.
- Add backend integration tests and a real Playwright/Compose end-to-end flow through authentication, cart, order, payment, and inventory.

## Impact
- Affected specs: `user-auth`
- Affected code: Docker Compose and Keycloak assets, frontend authentication/context/API code, shared JWT configuration, order/cart HTTP contracts, payment authorization, Playwright E2E tests, and local-development documentation.
- API compatibility: customer-owned cart and order APIs no longer accept or use client-supplied user IDs.


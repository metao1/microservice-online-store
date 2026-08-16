# Frontend Keycloak Build Configuration

## Context

The containerized frontend is built without `VITE_KEYCLOAK_URL`, `VITE_KEYCLOAK_REALM`, or `VITE_KEYCLOAK_CLIENT_ID`. Because Vite replaces these values at build time, `keycloak-js` constructs an authorization URL containing `undefined`. The resulting navigation loop repeatedly remounts the application and reissues public category requests.

## Design

- Add the three Keycloak Vite variables as frontend Docker build arguments and environment values.
- Supply Compose defaults matching the imported realm: `http://localhost:8080`, `bookstore`, and `bookstore-frontend`.
- Validate configuration before constructing the Keycloak client and throw one clear startup error listing missing variables.
- Keep category fetching unchanged; request caching would mask the authentication reload loop rather than fix it.

## Verification

- A focused unit test proves incomplete Keycloak configuration fails clearly.
- A focused unit test proves valid configuration constructs the expected client configuration.
- The frontend build succeeds with the Compose values present.

## Non-Goals

- Changing the PKCE authentication flow.
- Adding category response caching.
- Changing Keycloak realm clients or redirect URIs.

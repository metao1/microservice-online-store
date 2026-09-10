# Security Filter Chain CORS Design

## Context

Services register an origin allow-list through Spring MVC, but the shared JWT `SecurityFilterChain` does not enable CORS. An authenticated browser request therefore fails during its unauthenticated `OPTIONS` preflight with `401` before MVC can apply the configured CORS policy.

## Design

- Enable Spring Security CORS support in `JwtSecurityAutoConfiguration` using its standard defaults.
- Reuse the existing MVC CORS configuration and service properties; do not duplicate origins in security code.
- Keep authentication requirements unchanged for actual `/cart`, order, and payment requests.
- Keep the allowed origin list unchanged because `http://localhost:3000` is already configured.

## Verification

- Add a focused security integration test for an `OPTIONS /cart` request from `http://localhost:3000` requesting `GET` with the `Authorization` header.
- Assert the preflight succeeds without a bearer token and returns the expected allow-origin, allow-method, and allow-header values.
- Rebuild the order service and repeat the preflight against port `8086`.

## Non-Goals

- Permitting arbitrary origins.
- Disabling JWT authentication for the real cart request.
- Adding `http://0.0.0.0:3000` to the allow-list.

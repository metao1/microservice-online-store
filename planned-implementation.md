# Backend Reliability and Identity Plan

## Status

**Planning only.**  
No implementation should begin until this plan is approved and the identity-provider decision is confirmed.

## Objectives

1. Secure all customer-owned order and cart operations using a verified identity.
2. Remove client-controlled `userId` values from REST request bodies and paths.
3. Guarantee eventual Kafka publication after successful database commits.
4. Use immutable event IDs for consumer idempotency.
5. Preserve payment uniqueness and concurrency protections.
6. Make order/payment lifecycle transitions explicit and valid.
7. Add automated tests for retries, duplicates, authorization, and failure recovery.

## Current-State Findings

| Area | Current state | Risk |
|---|---|---|
| HTTP identity | `CreateOrderRequestDTO`, cart APIs, and customer-order paths accept a caller-provided `userId` | A caller can access or alter another customer’s cart/orders |
| Shared security | `WebSecurityAutoConfiguration` configures CORS and API version only | No JWT authentication or authorization is enforced |
| Order persistence | `OrderManagementService.createOrder()` raises/publishes events but does not explicitly save the new order first | Order creation flow is inconsistent and can fail unexpectedly |
| Kafka publication | `OutboxDomainEventPublisher` sends after DB commit | Process crash after commit and before Kafka send loses the event |
| Payment event publication | Payment service catches publishing exceptions, clears aggregate events, and continues | Payment can be successful without notifying the order service |
| Event identity | `DomainEvent` has a UUID `eventId`, but `OrderCreatedEvent` maps `id` to order ID | Consumers deduplicate by business ID, not message identity |
| Payment listener | Uses `paymentId` or a derived fallback as the processed-event key | Later legitimate lifecycle events from one payment may be suppressed |
| Order lifecycle | New orders start `CREATED`; protobuf already exposes `PENDING_PAYMENT` | Payment lifecycle is ambiguous |
| Payment concurrency | Advisory lock, unique order constraint, pessimistic update lock, and `@Version` are present | Preserve these protections while refactoring |
| Existing inboxes | `processedpaymentevent` and `processedordercreatedevent` exist | Good foundation; must receive stable event IDs |

## Target Architecture

```text
Client
  |
  | Bearer JWT
  v
Order Service ---------------------> Order DB
  |                                    |
  | authenticated JWT sub              | same DB transaction
  |                                    v
  |                             Order Outbox Record
  |                                    |
  |                                    v
  |                            Outbox Relay -> Kafka
  |
  +---- cart/order ownership uses JWT subject only

Kafka: order-created
  |
  v
Payment Service -------------------> Payment DB
  |                                    |
  | inbox: processed order event       | same DB transaction
  | payment state change               | payment outbox record
  v                                    v
Outbox Relay ---------------------> Kafka: order-payment

Kafka: order-payment
  |
  v
Order Service
  |
  +---- inbox: processed payment event
  +---- transition order / clear cart / request inventory reduction
```

## Key Decisions

### Identity

- Use an external OpenID Connect provider, such as Keycloak, for registration, login, credential storage, token issuance, and JWT signing.
- Use JWT claim `sub` as the immutable application customer identifier.
- Introduce a `user-microservice` only for application profile data, shipping addresses, and credit/ledger capabilities; it must not store raw passwords or issue its own tokens.
- Configure order, payment, and future user services as Spring Security OAuth2 Resource Servers.
- Keep service-to-service communication on Kafka or use service credentials when synchronous calls are introduced later.

### Event Delivery

- Adopt the transactional outbox pattern in order and payment services.
- Write domain-state changes and the corresponding outbox record in the same local database transaction.
- Publish records asynchronously through a scheduled relay.
- Treat Kafka publishing as at-least-once; consumers must remain idempotent.
- Keep consumer inbox/processed-event records for de-duplication.

### Event IDs

- Keep domain event UUIDs as immutable integration event IDs.
- Add `event_id` as additive protobuf fields.
- Do not overload `id`; preserve `OrderCreatedEvent.id` as `orderId` for compatibility.
- Use `event_id` exclusively for inbox/processed-event persistence.
- Use `payment_id` and `order_id` only as business identifiers.

### API Migration

Replace externally supplied customer IDs with the authenticated principal:

| Existing API shape | Target API shape |
|---|---|
| `POST /api/order` with `{ "userId": "..." }` | `POST /api/orders` with no user ID in the request |
| `GET /api/order/customer/{userId}` | `GET /api/orders/me` |
| `GET /api/order/customer/{userId}/paged` | `GET /api/orders/me?offset=0&limit=10` |
| `GET /cart/{userId}` | `GET /cart` |
| `POST /cart` with `userId` in body | `POST /cart/items` with items only |
| `PUT /cart/{userId}/{sku}` | `PUT /cart/items/{sku}` |
| `DELETE /cart/{userId}/{sku}` | `DELETE /cart/items/{sku}` |
| `DELETE /cart/{userId}` | `DELETE /cart` |

## Phased Work Plan

## Phase 0 — Decisions and Baseline

### Scope

- Confirm external OIDC provider choice.
- Confirm token issuer URL, expected audience, and local-development configuration.
- Confirm whether the existing payment auto-processing remains a demo workflow or will call a real payment-service-provider adapter.
- Record database-backup and rollback process before adding Flyway migrations.

### Deliverables

- Identity/provider configuration document
- Environment-variable matrix
- Rollback procedure
- Baseline test run and build report

### Acceptance Criteria

- All existing tests are run before changes.
- Existing Flyway schema histories are recorded.
- OIDC issuer/audience requirements are agreed.

## Phase 1 — Shared Security Foundation

### Scope

Add shared JWT resource-server support and a safe current-user abstraction.

### Planned Files

- `shared-kernel/build.gradle`
- `shared-kernel/src/main/java/com/metao/book/shared/security/JwtSecurityAutoConfiguration.java`
- `shared-kernel/src/main/java/com/metao/book/shared/security/CurrentUser.java`
- `shared-kernel/src/main/java/com/metao/book/shared/security/SecurityExceptionHandler.java`
- `shared-kernel/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
- `order-microservice/build.gradle`
- `payment-microservice/build.gradle`
- `order-microservice/src/main/resources/application.yml`
- `payment-microservice/src/main/resources/application.yml`

### Implementation Tasks

1. Add Spring Security OAuth2 Resource Server dependencies.
2. Configure JWT validation through `spring.security.oauth2.resourceserver.jwt.issuer-uri`.
3. Validate issuer and audience.
4. Require authentication for business endpoints.
5. Permit only health/readiness/liveness endpoints without authentication.
6. Map scopes or realm roles to Spring authorities.
7. Introduce `CurrentUser.subject()` that retrieves the validated JWT `sub`.
8. Keep CORS configuration separate; rename `WebSecurityAutoConfiguration` later if needed to avoid confusion.

### Acceptance Criteria

- A request without a bearer token returns HTTP 401.
- A request with an invalid signature or invalid issuer returns HTTP 401.
- A valid token exposes its `sub` through `CurrentUser`.
- Health endpoints remain available as agreed.
- CORS rules remain allow-list based.

## Phase 2 — Order and Cart Ownership Migration

### Scope

Eliminate user-controlled ownership from the order API and cart API.

### Planned Files

- `order-microservice/src/main/java/com/metao/book/order/presentation/dto/CreateOrderRequestDTO.java`
- `order-microservice/src/main/java/com/metao/book/order/presentation/dto/AddItemRequestDto.java`
- `order-microservice/src/main/java/com/metao/book/order/presentation/controller/OrderManagementController.java`
- `order-microservice/src/main/java/com/metao/book/order/presentation/ShoppingCartController.java`
- `order-microservice/src/main/java/com/metao/book/order/domain/service/OrderManagementService.java`
- `order-microservice/src/main/java/com/metao/book/order/application/cart/ShoppingCartService.java`
- `order-microservice/src/test/java/.../OrderManagementControllerTest.java`
- `order-microservice/src/test/java/.../ShoppingCartControllerTest.java`
- `order-microservice/src/test/java/.../OrderAuthorizationIT.java`

### Implementation Tasks

1. Remove `userId` from `CreateOrderRequestDTO`.
2. Remove `userId` from `AddItemRequestDto`.
3. Change controllers to obtain the customer ID from `CurrentUser.subject()`.
4. Replace `{userId}` routes with `/me` or user-neutral routes as defined above.
5. Ensure all cart operations use the authenticated subject.
6. Ensure order listing uses the authenticated subject.
7. Add explicit ownership checks for any order-by-ID endpoint introduced later.
8. Update OpenAPI documentation and endpoint tests.

### Acceptance Criteria

- Client request bodies cannot choose a cart/order owner.
- A user can see only their own orders and cart.
- Cross-user requests cannot access data by guessing another user ID.
- Controller tests use JWT-backed mock authentication.

## Phase 3 — Order Persistence and State Model

### Scope

Make checkout persist an order reliably and use a payment-aware initial state.

### Planned Files

- `order-microservice/src/main/java/com/metao/book/order/domain/model/aggregate/OrderAggregate.java`
- `order-microservice/src/main/java/com/metao/book/order/domain/model/valueobject/OrderStatus.java`
- `order-microservice/src/main/java/com/metao/book/order/domain/service/OrderManagementService.java`
- `order-microservice/src/main/java/com/metao/book/order/domain/repository/OrderRepository.java`
- `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence/...`
- `order-microservice/src/test/java/.../OrderAggregateTest.java`
- `order-microservice/src/test/java/.../OrderManagementServiceTest.java`

### Implementation Tasks

1. Start newly checked-out orders in `PENDING_PAYMENT`.
2. Save the aggregate before recording the outbound integration event.
3. Preserve cart content until confirmed successful payment.
4. Permit expected state transitions:
   - `PENDING_PAYMENT -> PAID`
   - `PENDING_PAYMENT -> PAYMENT_FAILED`
   - `PENDING_PAYMENT -> CANCELLED`
   - `PAID -> PROCESSING`
   - `PROCESSING -> SHIPPED`
   - `SHIPPED -> DELIVERED`
5. Reject duplicate or invalid transitions safely.
6. Make successful-payment handling idempotent:
   - If already `PAID` or later, do not reduce inventory again.
   - Do not clear the cart again.
7. Define failure behavior for payment events received after cancellation.

### Acceptance Criteria

- Creating an order stores it with `PENDING_PAYMENT`.
- The aggregate is saved before the transaction completes.
- A duplicate payment-success event cannot reduce inventory twice.
- Invalid transitions return a clear domain/API error.

## Phase 4 — Shared Event Contract Evolution

### Scope

Add stable integration-event identifiers without breaking existing protobuf message consumers.

### Planned Files

- `shared-kernel/src/main/proto/OrderCreatedEvent.proto`
- `shared-kernel/src/main/proto/OrderPaymentUpdatedEvent.proto`
- `order-microservice/src/main/java/.../OrderCreatedEventTranslator.java`
- `payment-microservice/src/main/java/.../PaymentProcessedEventTranslator.java`
- Payment-failure event translator, if separate
- Related translator and listener tests
- Generated protobuf sources through the normal Gradle build; do not manually edit generated classes

### Protobuf Changes

```proto
message OrderCreatedEvent {
  string id = 1;          // Existing business order ID; do not change
  // Existing fields remain unchanged
  string event_id = 12;   // New immutable integration-event ID
}

message OrderPaymentUpdatedEvent {
  string id = 1;          // Existing field; retain for compatibility
  // Existing fields remain unchanged
  string event_id = 9;    // New immutable integration-event ID
}
```

### Implementation Tasks

1. Do not rename, remove, or reuse existing protobuf field numbers.
2. Map `DomainEvent.eventId` to protobuf `event_id`.
3. Continue using the event ID as Kafka message key unless ordering requirements require aggregate IDs instead.
4. Document that Kafka key and `event_id` have different purposes:
   - Kafka key: ordering/partition affinity, usually aggregate ID.
   - `event_id`: immutable message identity and de-duplication.
5. Prefer aggregate ID as Kafka key for order/payment ordering, while retaining `event_id` in the payload.
6. Regenerate protobuf classes through Gradle.
7. Deploy producers before requiring consumers to reject events without `event_id`.

### Compatibility Strategy

1. Release consumers that accept `event_id` but can temporarily fall back to legacy IDs.
2. Release producers that populate `event_id`.
3. Verify all producer versions publish `event_id`.
4. Remove legacy fallback only after the agreed retention period and backlog drain.

### Acceptance Criteria

- New order-created and payment-updated messages include nonblank `event_id`.
- Existing message fields retain their meanings and numbers.
- Consumer tests verify duplicate handling uses `event_id`.

## Phase 5 — Transactional Outbox

### Scope

Replace direct post-commit Kafka sends with durable outbox persistence and asynchronous relays.

### Design

Each service owns its own outbox table and relay. The transaction that changes business state must also insert one or more outbound-event rows.

### Common Outbox Schema

```sql
CREATE TABLE outboxevent (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    topic VARCHAR(255) NOT NULL,
    message_key VARCHAR(255) NOT NULL,
    payload BYTEA NOT NULL,
    payload_type VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at TIMESTAMP WITHOUT TIME ZONE NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_error TEXT NULL
);

CREATE INDEX idx_outboxevent_pending
    ON outboxevent (next_attempt_at, created_at)
    WHERE published_at IS NULL;
```

The final SQL may use a service-prefixed table name, such as `orderoutboxevent` and `paymentoutboxevent`, to keep database ownership unambiguous.

### Order Planned Files

- `order-microservice/src/main/resources/migration/V6__order_outbox.sql`
- `order-microservice/src/main/java/.../outbox/OrderOutboxEventEntity.java`
- `order-microservice/src/main/java/.../outbox/OrderOutboxEventRepository.java`
- `order-microservice/src/main/java/.../outbox/OrderOutboxWriter.java`
- `order-microservice/src/main/java/.../outbox/OrderOutboxRelay.java`
- `order-microservice/src/main/java/.../outbox/OrderOutboxProperties.java`
- `order-microservice/src/main/java/.../OrderManagementService.java`
- `order-microservice/src/test/java/.../OrderOutboxIT.java`

### Payment Planned Files

- `payment-microservice/src/main/resources/migration/V7__payment_outbox.sql`
- `payment-microservice/src/main/java/.../outbox/PaymentOutboxEventEntity.java`
- `payment-microservice/src/main/java/.../outbox/PaymentOutboxEventRepository.java`
- `payment-microservice/src/main/java/.../outbox/PaymentOutboxWriter.java`
- `payment-microservice/src/main/java/.../outbox/PaymentOutboxRelay.java`
- `payment-microservice/src/main/java/.../outbox/PaymentOutboxProperties.java`
- `payment-microservice/src/main/java/.../PaymentApplicationService.java`
- `payment-microservice/src/test/java/.../PaymentOutboxIT.java`

### Implementation Tasks

1. Translate domain events to protobuf before saving the outbox record.
2. Persist serialized protobuf payload, event type, topic, message key, and event ID.
3. Save the outbox row in the same transaction as:
   - Order persistence/state update
   - Payment persistence/state update
4. Remove direct use of `OutboxDomainEventPublisher` from order and payment transactional command flows.
5. Implement a scheduled relay with:
   - Bounded batch size
   - Row claiming using PostgreSQL `FOR UPDATE SKIP LOCKED`
   - Retry count and exponential backoff
   - Metrics and structured logging
6. Mark a row published only after Kafka producer confirmation.
7. Use Kafka idempotent producer configuration already present.
8. Leave failed rows pending for retry; never silently clear them.
9. Add an operational alert for old pending outbox rows and repeated publish failures.

### Relay Semantics

- Delivery is at-least-once.
- Duplicate messages are expected during crash/retry cases.
- Consumers must deduplicate using durable inbox records keyed by `event_id`.
- Exactly-once end-to-end behavior is not claimed.

### Acceptance Criteria

- If Kafka is unavailable, the database transaction still stores the order/payment change and its outbox record.
- When Kafka recovers, the relay publishes pending records.
- A relay crash after send but before marking published can resend; consumer inbox prevents duplicate business effects.
- No application code catches and discards an event-publish failure.

## Phase 6 — Inbox and Listener Hardening

### Scope

Use the new immutable event IDs in both consuming services.

### Planned Files

- `payment-microservice/src/main/java/.../listener/OrderCreatedEventListener.java`
- `payment-microservice/src/main/java/.../HandleOrderCreatedEventCommand.java`
- `payment-microservice/src/main/java/.../HandleOrderCreatedEventUseCase.java`
- `payment-microservice/src/main/java/.../ProcessedOrderCreatedEventRepository.java`
- `order-microservice/src/main/java/.../listener/PaymentEventListener.java`
- `order-microservice/src/main/java/.../HandleOrderPaymentEventCommand.java`
- `order-microservice/src/main/java/.../HandleOrderPaymentEventUseCase.java`
- `order-microservice/src/main/java/.../ProcessedPaymentEventRepository.java`
- Listener/use-case/integration tests

### Implementation Tasks

1. Read protobuf `event_id` and pass it unchanged to commands.
2. Insert the inbox marker using `INSERT ... ON CONFLICT DO NOTHING`.
3. Process business effects only when the inbox insert succeeds.
4. Acknowledge Kafka only after the transactional use case returns successfully.
5. Treat malformed events, unknown statuses, and missing mandatory fields as non-retryable and send them to DLT.
6. Use a transitional legacy fallback only during protobuf rollout; emit a warning metric whenever it is used.
7. Never use `paymentId` alone as an event de-duplication key.

### Acceptance Criteria

- Replaying the same Kafka event twice changes payment/order state once.
- Two distinct events for the same payment can both be processed when valid.
- A failed transaction does not acknowledge the Kafka record.
- Unknown event schema/status ends in DLT according to the error policy.

## Phase 7 — Payment Flow Corrections

### Scope

Make payment behavior explicit and remove unsafe event publishing.

### Planned Files

- `payment-microservice/src/main/java/.../PaymentApplicationService.java`
- `payment-microservice/src/main/java/.../PaymentDomainService.java`
- `payment-microservice/src/main/java/.../PaymentAggregate.java`
- `payment-microservice/src/main/java/.../PaymentController.java`
- `payment-microservice/src/main/java/.../PaymentProcessedEventTranslator.java`
- `payment-microservice/src/main/java/.../PaymentFailedEventTranslator.java`
- Payment service tests

### Implementation Tasks

1. Remove `publishDomainEvents()` direct Kafka calls from `PaymentApplicationService`.
2. Replace them with payment-outbox writes.
3. Preserve:
   - advisory transaction lock on order ID
   - unique payment-per-order constraint
   - pessimistic write lock for processing
   - optimistic version column
4. Emit an integration event for both successful and failed payment terminal states.
5. Do not use a hard-coded card as a production payment method.
6. For demo mode, explicitly configure a `DemoPaymentGateway` implementation.
7. For production mode, define a payment-gateway port and provider adapter; do not persist PAN/card data.
8. Restrict manual payment endpoints to authorized service/admin roles if they remain public.

### Acceptance Criteria

- A successful payment produces a durable outbox record.
- A failed payment produces a durable failure event.
- Payment duplicates still return the existing payment for the same order.
- Payment retries obey aggregate state rules.
- Payment data does not contain unmasked sensitive card data.

## Phase 8 — User Profile and Credit Service

### Scope

Create a user-profile service only after JWT authentication is in place.

### Responsibilities

- User profile details
- Shipping and billing addresses
- Preference data
- Store-credit balance and immutable ledger entries
- Optional customer-facing profile API

### Non-Responsibilities

- Password storage
- Login endpoint implementation
- Token signing
- OAuth/OIDC session handling

### Suggested Data Model

```text
user_profile
- user_id (JWT subject; primary key)
- display_name
- email
- created_at
- updated_at
- version

user_address
- id
- user_id
- type
- recipient_name
- address fields
- created_at
- updated_at
- version

credit_account
- user_id (primary key)
- available_balance
- currency
- version

credit_ledger_entry
- id
- user_id
- idempotency_key
- type
- amount
- currency
- reference_type
- reference_id
- created_at
```

### Credit Requirements

- Use a ledger, not balance-only mutation.
- Enforce idempotency by unique `idempotency_key`.
- Use optimistic locking for balance updates.
- Do not place user-service credit logic in the order or payment database.

## Phase 9 — Testing Strategy

### Unit Tests

- JWT subject extraction and missing/invalid authentication behavior
- Order transition rules
- Protobuf translators include `event_id`
- Outbox payload creation
- Retry/backoff calculations
- Listener maps `event_id` to inbox key
- Duplicate inbox insertion skips business processing

### Integration Tests

- Flyway migrations for outbox/inbox tables
- Order checkout saves order and outbox row atomically
- Payment state change saves payment and outbox row atomically
- Relay publishes pending records using Testcontainers Kafka/PostgreSQL
- Replayed Kafka events affect state once
- Concurrent payment creation results in one payment per order
- Optimistic locking conflict returns HTTP 409 where applicable

### End-to-End Tests

1. Authenticated user adds cart items.
2. Authenticated user creates an order.
3. Order is `PENDING_PAYMENT`.
4. Order-created outbox row is persisted.
5. Relay publishes order-created event.
6. Payment consumes it once and persists payment.
7. Payment result outbox row is persisted.
8. Relay publishes payment result.
9. Order consumes it once, becomes `PAID`, clears that user's cart, and emits inventory reduction once.
10. Replay both events and verify no duplicate payment, inventory reduction, or cart side effect.

### Failure Tests

- Kafka unavailable during order creation
- Kafka unavailable during payment completion
- Application crash simulation after database commit
- Relay crash after Kafka send and before database mark-published
- Duplicate delivery
- Concurrent delivery
- Consumer transaction failure before acknowledgment
- Expired/invalid JWT
- User A attempts User B cart/order access

## Phase 10 — Observability and Operations

### Metrics

- `outbox.pending.count`
- `outbox.oldest.pending.age`
- `outbox.publish.success`
- `outbox.publish.failure`
- `outbox.retry.count`
- `inbox.duplicate.count`
- `kafka.dlt.count`
- `payment.processing.duration`
- `order.payment.event.lag`
- `security.authentication.failure`

### Logs and Tracing

- Include `eventId`, `orderId`, `paymentId`, and authenticated `userId` where appropriate.
- Do not log JWTs, payment credentials, or personal address details.
- Propagate correlation/trace identifiers in Kafka headers where supported.
- Alert on stale outbox records and repeated relay failures.

## Migration and Rollout

### Deployment Order

1. Deploy consumer code compatible with both legacy and new event-ID fields.
2. Apply shared protobuf contract update and regenerate classes.
3. Deploy producers that populate `event_id`.
4. Verify all active producers provide `event_id`.
5. Deploy outbox migrations and relay implementation.
6. Disable direct application-level Kafka publishing.
7. Enable JWT enforcement in non-production, then production.
8. Migrate client routes to authenticated `/me` endpoints.
9. Remove legacy user-ID endpoints after deprecation period.
10. Remove legacy event-ID fallback after Kafka retention window and backlog drain.

### Rollback Rules

- Never delete or renumber protobuf fields.
- Outbox tables are additive and can remain if application rollback is needed.
- Keep old API endpoints only behind a temporary compatibility layer; never allow them to bypass authenticated ownership checks.
- Do not enable mandatory `event_id` validation until all producers are updated.

## Explicit Non-Goals

- Distributed two-phase commit between PostgreSQL and Kafka
- Claiming exactly-once end-to-end delivery
- Building a custom password/token system
- Storing raw card numbers
- Deleting cart items before confirmed successful payment
- Allowing client-provided user IDs to determine resource ownership

## Final Definition of Done

The work is complete only when:

- Every customer-owned HTTP operation derives ownership from a verified JWT.
- Order and payment state changes write a durable outbox record in the same database transaction.
- Outbox relays retry safely and expose operational metrics.
- Kafka consumers deduplicate using immutable `event_id`.
- Duplicate/replayed events do not duplicate payments, inventory reductions, or cart effects.
- The order lifecycle begins at `PENDING_PAYMENT`.
- Existing payment uniqueness and locking protections remain tested.
- Automated integration and end-to-end tests cover normal, duplicate, concurrent, and failure paths.
- Deployment and rollback documentation is available.
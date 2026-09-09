## Context
The intended dependency direction is:

```text
presentation / infrastructure adapters
                ↓
application use cases and ports
                ↓
domain model and domain ports
                ↑
infrastructure adapter implementations
```

The current code violates this in several places: domain services use Spring transactions and concrete Kafka publishers, cart application classes are JPA entities/repositories, Kafka listeners depend on concrete use-case implementations, shared domain classes contain framework annotations, and event processing uses business identifiers instead of immutable message identifiers.

## Goals
- Make domain packages technology-neutral and enforce that with ArchUnit.
- Ensure all inbound adapters depend on use-case interfaces.
- Ensure application services depend on ports/interfaces for persistence, messaging, gateway, cart, and concurrency concerns.
- Persist outbound integration events in the same local transaction as aggregate state.
- Process inbound integration events idempotently using immutable transport event IDs.
- Make order, payment, cart, and inventory event names represent one explicit business fact.
- Preserve historical order totals and VAT snapshots.

## Non-Goals
- Rebuilding all bounded contexts into separate repositories.
- Changing user-facing endpoint names unless required to stop returning entities or ambiguous command shapes.
- Implementing a production payment gateway provider. This change introduces the port and a profile-scoped fake adapter; a real gateway can be added later.

## Decisions

### Open inbound ports first
Create use-case interfaces such as `CreateOrderUseCase`, `UpdateOrderStatusUseCase`, `GetCustomerOrdersUseCase`, `UpdateOrderItemsUseCase`, `HandleOrderPaymentEventUseCase`, and payment-side `HandleOrderCreatedEventUseCase`. Controllers and listeners inject these interfaces only. Concrete implementations live in `application.service`.

### Declare architectural roles with annotations
Use technology-neutral marker annotations for inbound adapters, application use-case ports, application-service implementations, domain components, and outbound adapters. Spring stereotypes remain responsible for bean registration; role annotations describe architecture only. Architecture tests discover annotated types and prevent forbidden layer dependencies without relying on individual class names or field names. Do not use a Spring AOP aspect as the primary guard: AOP validates only instantiated/proxied beans at runtime, whereas the architecture test must fail at build time for every compiled type.

### Keep domain pure
Domain classes may contain value objects, aggregates, domain events, domain exceptions, and pure domain services. Domain exceptions are mapped to HTTP status in presentation advice only. Domain repository interfaces must not expose database lock semantics; if lock behavior is needed, model it as an application port implemented by infrastructure.

### Use outbox and inbox patterns
Application services persist aggregate state and service-local outbox rows inside one transaction, then clear in-memory domain events after the outbox rows are stored. A transaction synchronization registers an after-commit callback that attempts immediate Kafka publication only when the business transaction commits. A separate publisher reads pending rows and retries post-commit failures, marking a row published only after Kafka send confirmation. Consumers validate and store immutable event IDs using a unique `(consumer_name, event_id)` constraint, treating unique-key violations as already processed.

### Own outbox infrastructure in a dedicated module
Create an `outbox-messaging` Gradle module rather than putting outbox contracts, Spring wiring, codecs, or publishers in `shared-kernel`. The module may depend on `shared-kernel` only for technology-neutral domain-event contracts. The module provides the reusable JPA outbox adapter, transaction synchronization, Kafka dispatch orchestration, and Spring Boot auto-configuration. Each microservice owns its outbox table and migration in its local database. This keeps data ownership service-local while avoiding duplicated persistence adapters and prevents the shared kernel from becoming a Spring/Kafka integration module.

### Make event contracts explicit
Do not multiplex inventory reduction through `ProductUpdatedEvent.description = "INVENTORY_REDUCTION"`. Define dedicated integration events such as `InventoryReductionRequestedEvent` or `StockReservationRequestedEvent` with event ID, order ID, SKU, quantity, occurrence time, correlation ID, and causation ID. Payment processed, failed, cancelled, and pending states must map exhaustively without defaulting unknown statuses to failure.

### Preserve financial snapshots
Orders persist the VAT rate/tax policy and monetary totals used at creation. Rehydration uses persisted data, not current runtime configuration. Outbound order-created events include the stable financial snapshot needed by payment.

### Clean package placement
Move Kafka configuration, serializers, listeners, streams, translators, schedulers, external clients, and persistence models to infrastructure packages. Move REST DTOs, validation, and exception-to-HTTP mapping to presentation packages. Application commands/results must not contain Jackson or Spring annotations.

## Rollout Strategy
1. Establish baseline and architecture tests, using temporary exceptions only where necessary.
2. Introduce use-case interfaces and move implementations without behavior changes.
3. Remove domain framework dependencies and migrate cart persistence/DTOs.
4. Fix status transitions, event IDs, and explicit event mappings.
5. Add outbox/inbox tables and adapters, then migrate publishers/consumers.
6. Persist VAT/financial snapshots and update mappers/contracts.
7. Delete or rebuild stale Kafka Streams/configuration code.

## Risks
- Broad package moves may require many test import updates.
- Outbox/inbox migrations alter runtime behavior and must be verified with integration tests.
- Shared-kernel splitting can create dependency churn; keep it last and small.

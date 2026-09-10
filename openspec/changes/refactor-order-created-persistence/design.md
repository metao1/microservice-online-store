## Context
The order service currently publishes `DomainOrderCreatedEvent` outward and also contains a partially implemented inbound Kafka listener for `com.metao.book.shared.OrderCreatedEvent`. That inbound listener sits in infrastructure, injects the domain repository directly, listens with the wrong topic configuration (`order-payment`), and has no aggregate reconstruction logic. Persistence already exists for the order aggregate, but the naming and package structure do not make the port/adapter boundaries explicit.

## Goals
- Enforce a strict dependency direction for inbound order-created persistence.
- Keep Kafka message mapping and JPA mapping in infrastructure.
- Keep the application layer responsible for orchestration and transactions.
- Keep the domain layer free from Spring, Kafka, and JPA imports.
- Ensure Kafka acknowledgment happens only after a successful use case execution.

## Non-Goals
- Redesign the existing outbound order-created publishing flow.
- Redesign unrelated order payment consumption.
- Change the business semantics of order creation beyond reconstructing and persisting the aggregate from the inbound message.

## Decisions
### Domain event model
Create a pure domain `OrderCreatedEvent` dedicated to inbound persistence. It will carry the fields needed to reconstruct an order aggregate from the consumed message: order id, user id, sku, product title, quantity, price, currency, created timestamp, updated timestamp, and initial status.

### Aggregate reconstruction
Add a static factory method on `OrderAggregate` to reconstitute an aggregate from the inbound domain event. This keeps aggregate invariants inside the domain while avoiding Kafka or protobuf dependencies.

### Application orchestration
Create `PersistOrderUseCase` and `PersistOrderService`. The service will map the inbound domain event into an aggregate using the factory method and persist it through the domain `OrderRepository` port under a transaction.

### Shared event contract
Update the shared protobuf `OrderCreatedEvent` so the transport model can faithfully reconstruct an order line item:
- add `product_title`
- align the status enum with domain order status names such as `CREATED`, `PAID`, and `CANCELLED`

This keeps infrastructure mapping straightforward and avoids lossy placeholder values inside the domain.

### Persistence adapter
Refactor the current JPA infrastructure into explicit adapter naming:
- `OrderJpaEntity`
- `SpringDataOrderRepository`
- `OrderRepositoryAdapter`
The adapter remains responsible for domain-to-entity mapping and persistence concerns.

### Messaging adapter
Create `OrderCreatedEventMessage` as the transport model for the inbound message handling path and refactor the consumer into `OrderKafkaConsumer`. The consumer will translate the Kafka payload into the domain event, call the use case, and manually acknowledge the record only after success.

## Risks
- Existing tests may assume current repository/bean names.
- The current protobuf event contains a single line item payload, so the aggregate reconstruction must reflect that current contract without inventing unsupported multi-item semantics.
- Publishing order-created events before transaction commit can cause the same service to consume stale state. Kafka publication therefore needs to occur after commit.
- Refactoring class names may affect wiring in integration tests and event listener container configuration.

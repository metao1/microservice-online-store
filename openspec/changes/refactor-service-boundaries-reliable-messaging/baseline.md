# Baseline Dependency and Stale-Code Inventory

## Current Dependency Graph

```text
Order presentation
  -> order application use cases/services
  -> order domain aggregates, value objects, domain ports
  -> infrastructure persistence/messaging adapters

Payment presentation/listeners
  -> payment application use cases/services
  -> payment domain aggregates, value objects, domain ports
  -> infrastructure persistence/messaging adapters

Inventory presentation/listeners
  -> product application services/use cases
  -> product domain aggregates, value objects, domain ports
  -> infrastructure persistence/messaging adapters

shared-kernel domain/contracts
  <- consumed by all bounded contexts

shared-kernel config/messaging
  -> Spring, Kafka, protobuf, and persistence infrastructure
```

The intended direction is adapter/presentation -> application -> domain. Infrastructure
implements application/domain ports and must not be imported by domain code.

## Boundary Violations

### Order

- `order.application.cart.ShoppingCart` is a JPA entity and
  `order.application.cart.ShoppingCartRepository` extends `JpaRepository`.
- Kafka consumer configuration, converters, serializers, VAT configuration, and the
  commented stream configuration remain under `order.application.config`.
- `order.domain.repository.OrderRepository` exposes Spring Data's `Page` type.
- `order.infrastructure.messaging.translator.ProductUpdatedEventTranslator` emits a
  `ProductUpdatedEvent` with the magic description `INVENTORY_REDUCTION`.
- `order.infrastructure.scheduler.OrderGenerator` sends directly with `KafkaTemplate`.

### Payment

- Payment persistence is mostly isolated under `payment.infrastructure.persistence`,
  but the domain repository still exposes database locking semantics through
  `lockOrderForCreation`.
- The Kafka listener remains under `payment.listener` instead of the target
  `payment.infrastructure.messaging.kafka.consumer` package.
- Payment event translation and publishing still depend on shared Kafka support,
  which is being moved behind the outbox port.

### Inventory/Product

- `product.application.usecase.HandleProductUpdatedEventUseCase` interprets
  `description == "INVENTORY_REDUCTION"` as an inventory command.
- `product.infrastructure.factory.handler.ProductKafkaListenerComponent` and
  `ProductGenerator` mix Kafka consumption/generation concerns with factory naming.
- Product domain exceptions still use Spring `@ResponseStatus` annotations.
- `ProductCreateIdempotencyRepository` and `ProcessedInventoryEventRepository` use
  direct JDBC persistence instead of the shared inbox abstraction.

### Shared Kernel

- `shared.domain.base.DomainTranslatorAutoConfiguration` and
  `DelegatingDomainEventTranslator` are Spring configuration/components under a
  domain package.
- `shared.domain.base.AbstractEntity`, `IdentifiableDomainObject`, and
  `DomainObjectId` contain persistence/nullability framework dependencies.
- `shared.config` contains Kafka, web, transaction, and security infrastructure;
  these classes are correctly infrastructure-oriented but are currently imported
  by application/domain-facing code in some paths.

## Stale or Transitional Code

- `order.application.config.OrderStreamConfig` is commented-out Kafka Streams code
  and should be deleted or rebuilt against current event contracts.
- `ProductUpdatedEventTranslator` plus the inventory-side marker branch is a stale
  compatibility path once a dedicated inventory reservation/reduction event exists.
- The old `processed_*_event` Flyway tables remain as migration history while new
  runtime inbox adapters use `consumed_message`.
- `planned-implementation.md` is an untracked implementation note and is not part
  of the runtime architecture.
- Existing direct Kafka sends in `OrderGenerator` bypass the transactional outbox.

## Baseline Constraints

- Order and payment Java compilation previously passed before the latest JPA inbox
  adapter swap.
- Focused order/payment unit tests passed for the inbound-port and payment-event
  refactor.
- Strict OpenSpec validation and `git diff --check` pass.
- A later Gradle compile was blocked by the environment's escalation/usage limit;
  the JPA inbox changes require a fresh compile when Gradle execution is available.

# Change: Refactor order-created consumption into strict DDD layers

## Why
The current `order-microservice` order-created consumer flow is not aligned with the project's DDD and dependency rules. The Kafka listener depends directly on the domain repository, is wired to the wrong topic properties, does not acknowledge records manually, and does not reconstruct an order aggregate from the consumed event. This makes the inbound order persistence flow incomplete, hard to test, and coupled to infrastructure details.

## What Changes
- Introduce a dedicated application input port and service for persisting orders from consumed order-created events.
- Add a pure domain event model and aggregate factory method for reconstituting an `OrderAggregate` from the inbound order-created payload.
- Refactor the Kafka consumer into a thin driving adapter that maps the transport message to a domain event and calls the application use case.
- Refactor persistence into a dedicated driven adapter that implements the domain `OrderRepository` and maps aggregates to JPA entities.
- Rename and reorganize persistence classes to clarify domain vs infrastructure responsibilities.
- Extend the shared `OrderCreatedEvent` contract to carry `product_title` and use domain-aligned order status enum values.
- Enable explicit Kafka manual acknowledgment for the order-created consumer path.
- Remove the current legacy listener/repository wiring for this flow.

## Impact
- Affected specs: `order-created-persistence`
- Affected code:
  - `order-microservice/src/main/java/com/metao/book/order/infrastructure/listener/OrderCreatedEventListener.java`
  - `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence/**`
  - `order-microservice/src/main/java/com/metao/book/order/application/config/KafkaConsumerConfig.java`
  - `order-microservice/src/main/resources/application.yml`

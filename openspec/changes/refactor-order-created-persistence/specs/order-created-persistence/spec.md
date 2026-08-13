## ADDED Requirements
### Requirement: Order-Created Consumption Uses Application Port
The order service SHALL consume inbound order-created messages through a Kafka driving adapter that delegates to an application input port instead of calling the domain repository directly.

#### Scenario: Kafka consumer delegates through application layer
- **WHEN** an inbound order-created message is received from Kafka
- **THEN** the Kafka consumer maps the transport payload to a domain `OrderCreatedEvent`
- **AND** calls a `PersistOrderUseCase`
- **AND** does not call the domain repository directly

### Requirement: Inbound Order Persistence Reconstitutes Aggregate in Domain
The system SHALL reconstruct an `OrderAggregate` from a pure domain order-created event before persisting it.

#### Scenario: Application service persists reconstructed aggregate
- **WHEN** the application use case receives a domain `OrderCreatedEvent`
- **THEN** it creates an `OrderAggregate` through domain logic
- **AND** persists it through the domain `OrderRepository` port inside a transaction

### Requirement: Order-Created Contract Preserves Order Item Semantics
The shared order-created transport contract SHALL expose the data needed to rebuild an order line item without infrastructure-specific defaults.

#### Scenario: Transport payload includes product title and domain-aligned status
- **WHEN** an `OrderCreatedEvent` is published or consumed
- **THEN** the payload contains `product_title`
- **AND** the status enum values align with domain order status names such as `CREATED`

### Requirement: Order Repository Is Implemented As Infrastructure Adapter
The order domain repository SHALL be implemented by an infrastructure persistence adapter that maps aggregates to JPA entities.

#### Scenario: Persistence adapter saves aggregate through JPA
- **WHEN** the application service saves an order aggregate
- **THEN** the infrastructure repository adapter maps it to an order JPA entity
- **AND** saves it through a Spring Data repository
- **AND** does not expose JPA types to the domain or application layers

### Requirement: Kafka Acknowledgment Is Manual And Post-Success
The order-created Kafka consumer SHALL use manual acknowledgment semantics and acknowledge only after the application use case succeeds.

#### Scenario: Consumer acknowledges after successful persistence
- **WHEN** the order-created use case completes successfully
- **THEN** the Kafka consumer explicitly calls `acknowledgment.acknowledge()`
- **AND** the consumer configuration has auto-commit disabled and manual immediate acknowledgment enabled

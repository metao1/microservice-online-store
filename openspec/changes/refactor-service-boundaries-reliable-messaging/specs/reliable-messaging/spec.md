## ADDED Requirements

### Requirement: Outbound events are persisted transactionally
The system SHALL persist outbound integration-event records in an outbox table in the same local transaction as aggregate state changes.

#### Scenario: An order is created
- **GIVEN** an application service saves a new order aggregate
- **WHEN** the aggregate has domain events to publish
- **THEN** the service SHALL persist outbox records with immutable event ID, aggregate ID, event type, payload, occurred timestamp, retry count, and publication timestamp fields before clearing in-memory domain events

#### Scenario: Kafka send fails after commit
- **GIVEN** an outbox record exists for a committed aggregate change
- **WHEN** Kafka publishing fails
- **THEN** the outbox record SHALL remain pending for retry and SHALL NOT be marked published

### Requirement: Outbox publisher confirms delivery
The system SHALL publish pending outbox rows asynchronously and mark rows published only after Kafka confirms the send.

#### Scenario: Pending record is published
- **GIVEN** a pending outbox row
- **WHEN** Kafka confirms the event was sent
- **THEN** the publisher SHALL record the publication timestamp and preserve the original event ID

### Requirement: Inbound events use immutable idempotency keys
Kafka consumers SHALL use immutable transport event IDs as consumed-message keys and SHALL reject blank IDs at adapter boundaries.

#### Scenario: Order-created event is consumed by payment
- **GIVEN** an inbound order-created message has an event ID
- **WHEN** payment persists the consumed-message record
- **THEN** it SHALL store that event ID, not the order ID or another business correlation field

#### Scenario: Payment event is consumed by order
- **GIVEN** an inbound payment lifecycle message has an event ID
- **WHEN** order persists the consumed-message record
- **THEN** it SHALL store that event ID, not the payment ID

### Requirement: Consumed-message uniqueness includes consumer name
The system SHALL enforce a unique constraint on `(consumer_name, event_id)` for consumed-message records.

#### Scenario: Duplicate message is delivered
- **GIVEN** a consumer receives a message whose `(consumer_name, event_id)` already exists
- **WHEN** the idempotency insert detects a unique-key violation
- **THEN** the consumer SHALL treat the message as already processed and SHALL NOT repeat the business side effect

### Requirement: Integration events represent explicit business facts
Integration events SHALL describe one explicit business fact and SHALL NOT use magic-string fields to request unrelated behavior.

#### Scenario: Inventory reduction is requested
- **GIVEN** an order requires inventory reservation or reduction
- **WHEN** the order context emits an integration event
- **THEN** it SHALL use a dedicated inventory event containing event ID, order ID, SKU, quantity, occurrence time, correlation ID, and causation ID

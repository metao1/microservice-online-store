## ADDED Requirements

### Requirement: Outbox ordering is explicitly selected
The system SHALL treat an outbox record as ordered only when its sender supplies an ordering key.

#### Scenario: Sender omits ordering
- **GIVEN** an outbox record has no ordering key
- **WHEN** an earlier publication fails
- **THEN** the publisher SHALL remain free to publish that unordered record

#### Scenario: Sender requests ordering
- **GIVEN** two unpublished records carry the same ordering key
- **WHEN** the older record cannot be published
- **THEN** the newer record SHALL NOT be published until the older record succeeds

#### Scenario: Independent ordering group remains available
- **GIVEN** the oldest record in one ordering group cannot be published
- **WHEN** a record in another ordering group is claimable
- **THEN** the independent record MAY be published

### Requirement: Outbox payload handling is encoding-neutral
The reusable outbox module SHALL NOT require payloads to implement a Protobuf type and SHALL delegate durable-byte conversion through generic codec contracts.

#### Scenario: Protobuf payload is published
- **GIVEN** a service registers a Protobuf codec through the generic outbox codec contract
- **WHEN** its pending record is published
- **THEN** the configured Kafka producer SHALL receive the decoded Protobuf payload without the reusable publisher declaring a Protobuf-bound API

#### Scenario: Another encoding is configured
- **GIVEN** a service registers a codec for a non-Protobuf payload type
- **WHEN** its pending record is published
- **THEN** the outbox publisher SHALL process it through the same generic contract


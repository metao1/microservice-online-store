## Context
Kafka preserves order only for successfully appended records within a partition. The current publisher claims multiple rows, reschedules an unsuccessful row, and continues, allowing a later row with the same business key to be appended first. Not every event stream requires this blocking behavior, so ordering must be selected by the sender.

The publisher also deserializes every payload through `ProtobufMessageCodec` and uses `KafkaTemplate<String, Message>`, coupling the reusable outbox module to one serialization technology.

## Goals
- Make FIFO publication opt-in per record.
- Block only the failed record's ordering group.
- Keep unordered and independent ordered groups available for throughput.
- Remove all `com.google.protobuf` types from the outbox module's public and implementation contracts.
- Preserve at-least-once delivery and consumer idempotency.

## Non-Goals
- Exactly-once delivery across the database and Kafka.
- Ordering Kafka consumption across different topics or partitions.
- Automatically inferring which domain events require ordering.

## Decisions

### Use an optional sender-defined ordering key
`OutboxMessage` gains an optional `orderingKey`. A missing key means unordered publication. Supplying a key explicitly opts the record into FIFO publication relative to other unpublished records carrying the same key.

The ordering key is independent from the Kafka record key. Senders may use the same value when Kafka partition ordering is the goal, but the outbox does not infer this choice.

### Enforce ordering while claiming
The shared JPA store selects unordered claimable rows normally. An ordered row is claimable only when no older unpublished row with the same ordering key exists, including retry-delayed or currently leased rows. This prevents multiple workers from claiming different positions in one ordered stream.

A failed ordered row therefore blocks only its own ordering key. Later unordered rows and rows with different ordering keys remain claimable.

### Make payload conversion generic
Replace Protobuf-specific codec contracts with generic outbox payload codec contracts. `OutboxKafkaPublisher<T>` and its codec registry operate on `T`, and the configured `KafkaTemplate<String, T>` publishes that type. Protobuf services configure `T` as their common Protobuf message supertype outside the reusable publisher contract.

The durable outbox representation remains `byte[]`; payload interpretation stays in infrastructure codecs selected by event type and schema version.

### Share persistence code while retaining local data ownership
`outbox-messaging` owns the generic JPA entity, store implementation, claim query, and their integration tests. Each microservice still creates and owns `domain_event_outbox` in its own database through its own Flyway migration. Sharing adapter code does not introduce a shared database or cross-service transaction.

## Risks
- Incorrect store queries could allow concurrent claims from the same ordered stream; repository integration tests must cover this.
- A permanently failing head record freezes its ordering key, requiring observable retry state and an operational recovery policy.
- Changing generic bean types may require explicit Spring configuration to avoid ambiguous codec or Kafka template resolution.

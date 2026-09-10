## 1. Contract and TDD
- [x] 1.1 Change the existing red publisher test so ordering is explicitly requested by the sender.
- [x] 1.2 Add a test proving unordered records continue after an earlier failure.
- [x] 1.3 Add tests proving a failed ordered record does not block a different ordering key.

## 2. Payload Abstraction
- [x] 2.1 Introduce generic payload codec and registry contracts in `outbox-messaging`.
- [x] 2.2 Make the Kafka outbox publisher generic and remove its Protobuf imports.
- [x] 2.3 Adapt service Protobuf codecs and Spring configuration to the generic contracts.

## 3. Ordered Persistence
- [x] 3.1 Add nullable ordering-key persistence to each service-local outbox table and entity.
- [x] 3.2 Propagate the sender-selected ordering key through translations and `OutboxMessage`.
- [x] 3.3 Update claim queries so only the oldest unpublished record per ordered key is claimable.
- [x] 3.4 Add repository integration tests covering ordered, unordered, retry-delayed, and leased records.
- [x] 3.5 Consolidate the generic JPA entity, store, query, and integration test in `outbox-messaging` while retaining service-local migrations.

## 4. Verification
- [x] 4.1 Make the focused red publisher test pass.
- [x] 4.2 Run outbox and affected service tests.
- [x] 4.3 Run the full test suite and update this checklist with verified results.

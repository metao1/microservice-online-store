# Change: Add opt-in ordered outbox delivery

## Why
The outbox publisher currently allows a later event to overtake a failed earlier event, even when both events require ordered handling. It is also coupled directly to Protobuf, preventing the outbox module from supporting other payload encodings.

## What Changes
- Add an optional ordering key to each outbox record; records are unordered unless the sender explicitly supplies one.
- Prevent later claimable records with the same ordering key from being published before the oldest record succeeds.
- Continue publishing unordered records and records belonging to other ordering keys when an ordered record fails.
- Replace the Protobuf-bound publisher and codec registry contracts with payload-generic contracts.
- Retain Protobuf codecs as service infrastructure adapters using the generic contracts.
- Consolidate the generic JPA outbox entity, store, claim query, and persistence tests in `outbox-messaging`; services retain ownership of their local outbox tables and migrations.

## Impact
- Affected spec: `reliable-messaging`
- Affected code: `outbox-messaging`, service-local outbox migrations, event translators, and outbox tests
- Database change: nullable ordering-key column and supporting claim indexes in each service-local outbox table

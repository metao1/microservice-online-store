# Change: Refactor service boundaries and reliable messaging

## Why
Order, payment, cart, and shared messaging code currently cross domain, application, infrastructure, and presentation boundaries. This creates framework dependencies inside domain packages, direct controller/listener dependencies on concrete services, persistence leakage from cart APIs, unreliable event publication, and unsafe Kafka idempotency keys.

## What Changes
- **BREAKING**: Introduce explicit inbound use-case interfaces for controller and Kafka entry points, with implementations in `application.service`.
- Move application orchestration out of domain packages, especially `OrderManagementService`, and keep domain packages free of Spring, JPA, Kafka, Jackson, HTTP, and infrastructure imports.
- Isolate cart persistence and HTTP DTOs behind application ports/use cases and infrastructure adapters.
- Replace direct concrete Kafka publisher injection with an outbox module and service-local infrastructure implementations.
- **BREAKING**: Move outbox contracts, publishers, codecs, and Spring wiring out of `shared-kernel` into a dedicated `outbox-messaging` Gradle module.
- Add transaction-synchronized outbox dispatch: persist the service-local outbox row in the business transaction, trigger Kafka publication only after commit, and retain retry publication for post-commit failures.
- Add inbox/idempotency handling using immutable transport event IDs.
- Repair payment/order event semantics, status transitions, failed-payment handling, and inventory event contracts.
- Persist order financial snapshots, including VAT rate and totals, so historical totals do not change when runtime VAT changes.
- Move framework configuration, listeners, JPA entities, Spring Data repositories, and REST exception mapping into infrastructure/presentation packages.
- Add explicit layer annotations and annotation-driven ArchUnit, unit, contract, and integration tests that enforce the intended architecture and event behavior.
- Plan a later module split for `shared-kernel` into small technology-neutral and support modules.

## Impact
- Affected specs: `architecture-boundaries`, `reliable-messaging`, `order-payment-cart`
- Affected code:
  - `order-microservice/src/main/java/com/metao/book/order/domain/service/OrderManagementService.java`
  - `order-microservice/src/main/java/com/metao/book/order/application/usecase`
  - `order-microservice/src/main/java/com/metao/book/order/application/cart`
  - `order-microservice/src/main/java/com/metao/book/order/infrastructure/listener`
  - `order-microservice/src/main/java/com/metao/book/order/infrastructure/messaging`
  - `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence`
  - `payment-microservice/src/main/java/com/metao/book/payment/application/usecase`
  - `payment-microservice/src/main/java/com/metao/book/payment/application/service`
  - `payment-microservice/src/main/java/com/metao/book/payment/domain`
  - `payment-microservice/src/main/java/com/metao/book/payment/infrastructure`
  - `inventory-microservice/src/main/java/com/metao/book/product/infrastructure`
  - `shared-kernel/src/main/java/com/metao/book/shared`
  - new `outbox-messaging` Gradle module
  - database migrations for outbox, inbox/consumed events, and order financial snapshots
  - tests across order, payment, inventory, and shared modules

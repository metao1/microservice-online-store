## 1. Inventory and Baseline
- [x] 1.1 Run the current test suite and record compile/runtime failures.
- [x] 1.2 Document the current package dependency graph for order, payment, inventory, and shared modules.
- [x] 1.3 Identify stale classes and generated/dev code that fail the intended dependency direction.

## 2. Architecture Contracts
- [ ] 2.1 Add ArchUnit tests that domain packages do not depend on Spring, JPA, Kafka, Jackson, HTTP, presentation, or infrastructure packages.
- [ ] 2.2 Add annotation-driven ArchUnit tests that presentation and infrastructure depend on annotated use-case interfaces, not application service implementations.
- [ ] 2.3 Add ArchUnit tests that JPA entities and Spring Data repositories exist only under infrastructure persistence packages.
- [ ] 2.4 Add temporary architecture exceptions only where needed to keep the suite compiling during the staged refactor.
- [ ] 2.5 Add technology-neutral layer marker annotations to Spring-managed inbound adapters, use-case ports, application services, domain components, and outbound adapters in scope.

## 3. Inbound Ports
- [x] 3.1 Create order inbound interfaces for create order, update status, fetch customer orders, and order item/inventory operations.
- [x] 3.2 Move `OrderManagementService` behavior to an `order.application.service` implementation and remove the domain service.
- [x] 3.3 Convert order-side `HandleOrderPaymentEventUseCase` from concrete class to interface and move implementation to `application.service`.
- [x] 3.4 Convert payment-side `HandleOrderCreatedEventUseCase` from concrete class to interface and move implementation to `application.service`.
- [x] 3.5 Update controllers and Kafka listeners to inject use-case interfaces only.

## 4. Domain Cleanup
- [ ] 4.1 Remove Spring, transaction, repository, component, JPA, Jackson, HTTP, and Spring nullability annotations from all domain packages. (Partial: service, exceptions, and inventory event cleaned; shared base/value objects remain.)
- [x] 4.2 Move HTTP exception mapping to presentation `@RestControllerAdvice`.
- [x] 4.3 Move database locking semantics out of domain repository interfaces into application ports or infrastructure adapters.
- [x] 4.4 Move infrastructure-dependent category orchestration out of the domain service package; keep application services as Spring beans.

## 5. Cart Refactor
- [ ] 5.1 Move cart JPA model to infrastructure persistence entities and repositories.
- [x] 5.2 Add cart application ports and use-case interfaces for cart queries, commands, and clearing.
- [x] 5.3 Implement cart ports with JPA adapters.
- [ ] 5.4 Move cart HTTP DTOs and validation to presentation DTO packages. (Partial: controller no longer returns the entity.)
- [x] 5.5 Ensure cart REST endpoints never return JPA entities.

## 6. Event Correctness
- [x] 6.1 Add immutable event IDs to inbound integration commands and reject blank IDs at adapter boundaries.
- [x] 6.2 Store actual event/message IDs for consumed messages, not order IDs, payment IDs, statuses, or timestamps.
- [x] 6.3 Define explicit order/payment transition matrices covering success, failure, cancellation, and retry.
- [x] 6.4 Replace raw string status commands with enums parsed at presentation/infrastructure boundaries.
- [x] 6.5 Rename inventory-side order behavior to explicit reservation/reduction commands.
- [x] 6.6 Replace product-update magic-string inventory reduction with a dedicated inventory integration event.
- [x] 6.7 Add exhaustive payment integration-event translators and contract tests for every status mapping.
- [x] 6.8 Introduce `PaymentGatewayPort` and move fake payment behavior to an infrastructure adapter/profile.

## 7. Reliable Messaging
- [x] 7.1 Add outbox schema with event ID, aggregate ID, event type, payload, occurred timestamp, retry count, and publication timestamp.
- [x] 7.2 Persist outbox rows in the same transaction as aggregate state and clear aggregate events only after outbox persistence.
- [x] 7.3 Add an outbox publisher that retries pending rows and marks them published only after Kafka send confirmation.
- [x] 7.4 Add inbox/consumed-message schema with unique `(consumer_name, event_id)`.
- [x] 7.5 Treat duplicate unique-key claims as already processed and keep idempotency state changes in the same local transaction as business updates.
- [x] 7.6 Update consumers to acknowledge only after use-case completion and rely on retry/DLT for unexpected failures.
- [ ] 7.7 Create the `outbox-messaging` Gradle module and move outbox contracts, publishers, codecs, and Spring auto-configuration from `shared-kernel`.
- [ ] 7.8 Register transaction after-commit dispatch after durable service-local outbox persistence; retain scheduled retries for publish failures.
- [ ] 7.9 Update all microservices to use the new module while retaining service-local JPA outbox entities and repositories.

## 8. Persistence Isolation
- [x] 8.1 Remove JPA/Jackson/Spring annotations from shared domain base classes.
- [x] 8.2 Replace payment JPA inheritance from shared domain base classes with infrastructure-local persistence identity.
- [x] 8.3 Persist order VAT rate, subtotal, tax, total, and currency/tax policy data needed for deterministic rehydration.
- [x] 8.4 Update persistence mappers and event payloads to use persisted financial snapshots.

## 9. Package and Shared Cleanup
- [x] 9.1 Move Kafka consumer, serializer, stream, converter, VAT, and framework configuration to infrastructure or bootstrap packages.
- [x] 9.2 Move global REST exception handlers to presentation error packages.
- [x] 9.3 Move listeners under `*.infrastructure.messaging.kafka.consumer` and keep them thin.
- [x] 9.4 Delete or rebuild stale Kafka Streams/configuration code against current contracts.
- [ ] 9.5 Split shared-kernel responsibilities into technology-neutral domain/contracts and optional Spring/Kafka support modules or packages. (Translator contract remains in domain; Spring wiring moved to `shared.config`.)

## 10. Verification
- [x] 10.1 Add listener delegation tests for mapped command data.
- [x] 10.2 Add idempotency duplicate-delivery tests proving no duplicate business side effect.
- [ ] 10.3 Add order/payment transition tests for allowed and forbidden transitions.
- [ ] 10.4 Add integration tests for outbox write/publish/retry behavior.
- [ ] 10.5 Add integration tests for duplicate Kafka delivery and failed payment state changes.
- [x] 10.6 Add tests proving VAT changes after order creation do not alter historical totals.
- [ ] 10.7 Run module-level and full project tests after each stage.

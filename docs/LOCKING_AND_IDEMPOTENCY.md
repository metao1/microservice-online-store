# Locking And Idempotency Strategy

## Goal

This document explains why we use a mix of optimistic lock, pessimistic lock, and DB-enforced idempotency across the three microservices.

Target architecture assumptions:

- Multiple instances per microservice (horizontal scaling).
- At-least-once message delivery from Kafka.
- Concurrent API requests and concurrent consumer execution are expected.

## Design Rules

1. Use optimistic locking as the default for aggregate updates.
2. Use pessimistic locking only on narrow, high-risk critical sections.
3. Use DB idempotency at event-consumer boundaries.
4. Keep business invariants enforced by DB constraints (unique keys, PKs).

---

## Order Microservice

### Why Optimistic Lock

Orders are updated in short transactions and normally have low write contention. Optimistic locking detects concurrent overwrites without holding long DB locks.

Implemented by:

- `orders.version` column in migration.
- `@Version` in `OrderEntity`.

Files:

- `order-microservice/src/main/resources/migration/V2__orders_version.sql`
- `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence/entity/OrderEntity.java`

### Why Pessimistic Lock In Payment Finalization

The payment-finalization path (`CREATED -> PAID`) triggers inventory side effects. Under concurrent handling of payment events, this transition must be serialized to avoid duplicate side effects.

Implemented by:

- `findByIdForUpdate` with `PESSIMISTIC_WRITE`.
- Used only for payment success handling path.

Files:

- `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence/repository/JpaOrderRepository.java`
- `order-microservice/src/main/java/com/metao/book/order/domain/service/OrderManagementService.java`
- `order-microservice/src/main/java/com/metao/book/order/application/listener/PaymentEventListener.java`

### Why DB Idempotency In Payment Event Consumer

Kafka is at-least-once, so duplicate payment events are normal (retry/rebalance/replay). Locking alone does not stop duplicate deliveries. DB idempotency guarantees each event is applied once.

Implemented by:

- `processed_payment_event` table.
- `INSERT ... ON CONFLICT DO NOTHING` gate before business logic.

Files:

- `order-microservice/src/main/resources/migration/V3__processed_payment_event.sql`
- `order-microservice/src/main/java/com/metao/book/order/infrastructure/persistence/repository/ProcessedPaymentEventRepository.java`
- `order-microservice/src/main/java/com/metao/book/order/application/listener/PaymentEventListener.java`

---

## Payment Microservice

### Why Pessimistic Lock For Payment Lifecycle

Payment state transitions (`process`, `retry`, `cancel`) are stateful and non-commutative. Concurrent transitions must not interleave.

Implemented by:

- `findByIdForUpdate` with `PESSIMISTIC_WRITE`.

Files:

- `payment-microservice/src/main/java/com/metao/book/payment/infrastructure/persistence/repository/JpaPaymentRepository.java`
- `payment-microservice/src/main/java/com/metao/book/payment/domain/service/PaymentDomainService.java`

### Why DB Uniqueness For One Payment Per Order

Business invariant: one payment per order. This must be enforced at DB level for cross-instance race safety.

Implemented by:

- Unique index on `payment(order_id)`.

File:

- `payment-microservice/src/main/resources/migration/V3__payment_order_id_unique.sql`

### Why DB Idempotency In Order-Created Consumer

Duplicate `OrderCreatedEvent` deliveries can otherwise trigger duplicate processing and noisy/incorrect outcomes. Consumer-level dedup is required.

Implemented by:

- `processed_order_created_event` table.
- `INSERT ... ON CONFLICT DO NOTHING` gate in listener.

Files:

- `payment-microservice/src/main/resources/migration/V4__processed_order_created_event.sql`
- `payment-microservice/src/main/java/com/metao/book/payment/infrastructure/persistence/repository/ProcessedOrderCreatedEventRepository.java`
- `payment-microservice/src/main/java/com/metao/book/payment/listener/OrderCreatedEventListener.java`

---

## Inventory Microservice

### Why DB Idempotency For Inventory Reduction Events

Inventory updates are event-driven and can be replayed. Dedup is mandatory to prevent double-decrement.

Implemented by:

- `processed_inventory_event` table.
- `INSERT ... ON CONFLICT DO NOTHING` per event key.

Files:

- `inventory-microservice/src/main/resources/migration/V4__processed_inventory_event.sql`
- `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/persistence/repository/ProcessedInventoryEventRepository.java`
- `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/factory/handler/ProductKafkaListenerComponent.java`

### Why Atomic SQL Update For Stock Decrement

Stock decrement is a hot path under concurrency. A single SQL statement is faster and safer than read-modify-write at the entity level.

Implemented by:

- `UPDATE ... SET volume = volume - :quantity ... WHERE volume >= :quantity`.

Files:

- `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/persistence/repository/JpaProductRepository.java`
- `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/persistence/repository/ProductRepositoryImpl.java`
- `inventory-microservice/src/main/java/com/metao/book/product/application/service/ProductApplicationService.java`

### Why DB-Enforced Idempotency For Product Create

Check-then-insert (`exists + save`) is race-prone across instances. Insert-if-absent is safe and deterministic.

Implemented by:

- `insertIfAbsent` with `ON CONFLICT DO NOTHING`.
- `createProduct` uses `insertIfAbsent` and throws duplicate business error when not inserted.

Files:

- `inventory-microservice/src/main/java/com/metao/book/product/infrastructure/persistence/repository/JpaProductRepository.java`
- `inventory-microservice/src/main/java/com/metao/book/product/application/service/ProductApplicationService.java`

---

## Tradeoff Summary

- Optimistic lock: high throughput, low lock overhead, retry on conflict.
- Pessimistic lock: stronger serialization, use only where side effects make races expensive.
- DB idempotency: essential for message-driven correctness in at-least-once systems.

This combination gives correctness under retries/replays and concurrency while keeping lock contention localized.

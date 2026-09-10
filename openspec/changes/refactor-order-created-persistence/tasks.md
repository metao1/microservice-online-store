## 1. Implementation
- [x] 1.1 Document the inbound order-created persistence flow and define the required DDD boundaries.
- [x] 1.2 Create the domain-side order-created event model and aggregate factory for inbound persistence.
- [x] 1.3 Add the application input port and transactional service for persisting orders from inbound events.
- [x] 1.4 Refactor persistence into a dedicated infrastructure adapter that implements the domain repository.
- [x] 1.5 Refactor the Kafka consumer into a thin adapter with manual acknowledgment.
- [x] 1.6 Update Kafka consumer configuration for manual acknowledgment semantics.
- [x] 1.7 Remove or replace legacy classes involved in the old order-created persistence flow.
- [x] 1.8 Add or update tests covering consumer mapping, application orchestration, and persistence.
- [x] 1.9 Extend the shared order-created event contract with `product_title` and domain-aligned status values.

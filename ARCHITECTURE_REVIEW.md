# Architecture Review & Implementation Status

## Executive Summary

The e-commerce microservices architecture implements Domain-Driven Design (DDD) with event-driven communication using the transactional outbox pattern. The system consists of three main microservices (Order, Payment, Inventory) with shared libraries for cross-cutting concerns.

## Current Implementation Status

### ✅ Completed Phases

#### Phase 4: Event-Driven Communication
- **Transactional Outbox Pattern**: Implemented in `outbox-messaging` module
  - Service-local outbox tables with pessimistic locking
  - Claim-and-publish mechanism with worker leases
  - Configurable polling interval (default: 1000ms)
  - Batch processing (100 messages per poll)
  - Retry logic with exponential backoff
  
- **Event Contracts**: Protobuf-based event schemas
  - `OrderCreatedEvent`, `OrderUpdatedEvent`, `OrderPaymentUpdatedEvent`
  - `InventoryReductionRequestedEvent`, `ProductUpdatedEvent`
  - Schema versioning support
  
- **Kafka Integration**: 
  - Protobuf serialization/deserialization
  - Topic configuration per event type
  - Consumer groups with manual acknowledgment

#### Phase 5: Domain-Driven Design
- **Shared Kernel**: `shared-kernel` module
  - Domain primitives: `Money`, `VAT`, `Quantity`, `ProductSku`, `ProductTitle`
  - Architecture annotations: `@DomainComponent`, `@InboundAdapter`, `@OutboundAdapter`
  - Security: JWT authentication with OAuth2 Resource Server
  - Exception handling with standardized error responses

- **Microservices Structure**:
  - **Order Service**: Order aggregates, shopping cart, order creation workflow
  - **Payment Service**: Payment aggregates, payment processing, state machine
  - **Inventory Service**: Product aggregates, category management, stock control

- **Observability**:
  - Micrometer metrics with OTLP export
  - OpenTelemetry tracing
  - Structured logging with trace/span IDs
  - Health endpoints (liveness/readiness)

- **Resilience**:
  - Resilience4j circuit breakers
  - Configurable timeouts and retry policies
  - Idempotency support via idempotency keys

### ⚠️ Incomplete/Needs Work

#### Phase 1: Security Foundation (PARTIAL)
- ✅ Spring Security with JWT auto-configuration
- ✅ OAuth2 Resource Server setup
- ✅ Role-based access control (@PreAuthorize)
- ✅ CurrentUser utility for JWT claims extraction
- ❌ **Missing**: User microservice (externalized to Keycloak)
- ❌ **Missing**: API gateway integration
- ⚠️ **Note**: All services configured to use Keycloak at `http://localhost:8080/realms/bookstore`

#### Phase 2: API Migration (PARTIAL)
- ✅ ShoppingCartController: Removed userId from paths, uses `CurrentUser.subject()`
- ✅ OrderManagementController: Uses authenticated user subject
- ✅ PaymentController: Extracts userId from JWT (TODO comment for audit trail)
- ❌ **Issue**: PaymentController.createPayment() doesn't include userId in command
- ⚠️ **Recommendation**: Add userId field to CreatePaymentCommand for complete audit trail

#### Phase 3: Cache Layer (NOT STARTED)
- ❌ No caching implementation
- ❌ No Redis/Spring Cache integration
- ❌ No cache invalidation strategy

## Performance Optimizations

### Current Configuration
```yaml
# Outbox Publisher
outbox.publisher.fixed-delay-ms: 1000  # Can be reduced to 500ms
outbox.publisher.retry-delay-seconds: 30
BATCH_SIZE: 100
SEND_TIMEOUT_SECONDS: 10
LEASE_DURATION: 1 minute

# Kafka Consumer
session.timeout.ms: 30000
max.poll.interval.ms: 300000
heartbeat.interval.ms: 1000
auto.offset.reset: earliest

# Database Connection Pool (HikariCP)
minimum-idle: 5
maximum-pool-size: 10
connection-timeout: 20000
```

### Recommended Optimizations

#### 1. Outbox Polling Optimization
**Current**: Fixed 1000ms delay
**Recommended**: Adaptive batching based on load
- Reduce fixed delay to 500ms for lower latency
- Implement exponential backoff when queue is empty
- Add configurable batch size based on throughput requirements

#### 2. Database Indexes
✅ Already implemented:
```sql
CREATE INDEX ix_domain_event_outbox_pending
    ON domain_event_outbox (status, next_attempt_at, occurred_at);
```
This index supports the claim query efficiently.

#### 3. Kafka Consumer Concurrency
**Current**: Single-threaded consumer per partition
**Recommended**: 
```yaml
spring.kafka.listener.concurrency: 3  # Match partition count
spring.kafka.listener.max-poll-records: 500
```

#### 4. Connection Pool Tuning
**Current**: max-pool-size: 10
**Recommended**: Increase to 20-30 for high-load scenarios
```yaml
hikari:
  maximum-pool-size: 20
  minimum-idle: 10
  max-lifetime: 600000
```

## Architecture Gaps & Recommendations

### 1. Security Hardening (HIGH PRIORITY)
- **Issue**: Payment service doesn't propagate userId to command
- **Fix**: Update `CreatePaymentCommand` to include userId field
- **Location**: `payment-microservice/src/main/java/com/metao/book/payment/application/dto/CreatePaymentCommand.java`

### 2. API Consistency
- **Issue**: Mixed authorization patterns across services
- **Recommendation**: Standardize on scope-based authorization
- **Pattern**: `hasAuthority('SCOPE_<resource>:<action>')` as primary, roles as fallback

### 3. Missing Cache Layer (MEDIUM PRIORITY)
- **Candidates for caching**:
  - Product catalog (read-heavy)
  - Category listings
  - Shopping cart (Redis for distributed sessions)
  
- **Implementation**:
```java
// Add to inventory-microservice build.gradle
implementation "org.springframework.boot:spring-boot-starter-cache"
implementation "org.springframework.boot:spring-boot-starter-data-redis"
```

### 4. Saga Orchestration (FUTURE)
- **Current**: Choreography-based sagas via events
- **Limitation**: No compensation coordinator
- **Recommendation**: Implement saga orchestrator for complex workflows
  - Order cancellation with inventory restoration
  - Payment refund coordination

### 5. Distributed Locking
- **Current**: Pessimistic DB locks for outbox claiming
- **Risk**: Potential contention under high load
- **Alternative**: Consider Redis-based distributed locks for horizontal scaling

### 6. Event Versioning Strategy
- **Current**: Schema version field in outbox
- **Gap**: No migration strategy for old events
- **Recommendation**: Implement upcasters for schema evolution

## Java Version Alignment

**Current**: Java 17
```gradle
java {
  toolchain {
    languageVersion = JavaLanguageVersion.of(25)
  }
}
```

**Recommended**: Upgrade to Java 21 (LTS)
- Virtual threads for improved concurrency
- Pattern matching enhancements
- Better performance characteristics

## Testing Coverage

### Unit Tests
✅ ArchUnit tests for architecture enforcement
✅ Domain model tests
✅ Application service tests

### Integration Tests
✅ Testcontainers for PostgreSQL
✅ Testcontainers for Kafka
⚠️ **Gap**: Limited end-to-end scenario tests

### Performance Tests
✅ Load test framework in `performance-loadtest`
✅ Latency histogram tracking
⚠️ **Gap**: No automated performance regression tests in CI

## Deployment Considerations

### Docker Configuration
- Multi-stage builds configured via Jib plugin
- Environment variables for all configuration
- Health check endpoints exposed

### Kubernetes Readiness
✅ Liveness/readiness probes configured
✅ Graceful shutdown with lease release
⚠️ **Missing**: Horizontal Pod Autoscaler metrics

### Monitoring Stack
- Prometheus metrics endpoint: `/actuator/prometheus`
- Jaeger/Tempo tracing via OTLP
- Grafana dashboards needed

## Next Steps Priority Order

1. **IMMEDIATE**: Fix PaymentController userId propagation
2. **HIGH**: Implement cache layer for product catalog
3. **HIGH**: Tune outbox polling interval (1000ms → 500ms)
4. **MEDIUM**: Add integration tests for crash recovery scenarios
5. **MEDIUM**: Upgrade to Java 21
6. **LOW**: Implement saga orchestrator
7. **LOW**: Add distributed tracing dashboards

## Conclusion

The architecture demonstrates solid DDD principles with well-implemented event-driven communication. The transactional outbox pattern ensures data consistency across service boundaries. Primary gaps are in caching, complete security audit trails, and advanced orchestration patterns. Performance optimizations should focus on outbox polling frequency and connection pool tuning before considering more complex changes.

# Roadmap Implementation Plan

## Goal
Build a horizontally scalable architecture (10+ instances per service) with shared cache consistency, safe concurrency controls, and Java 21 runtime improvements.

## Phase 1: Shared Cache Foundation (Redis)

### Scope
- Introduce Redis as shared cache of record (L2) for read-heavy paths.
- Keep local in-memory cache disabled as primary source of truth.

### Tasks
1. Add Redis dependencies and environment configuration in `inventory-microservice` and `payment-microservice`.
2. Create a common cache configuration in `shared-kernel`:
   - serializer strategy
   - key namespace conventions
   - per-cache TTL policy
3. Define initial cache regions:
   - product-by-sku
   - payment-by-id
   - payment-by-order-id
   - short-lived query/list caches

### Acceptance Criteria
- Both services can read/write Redis cache in local and container environments.
- Cache keys are namespaced by service and entity.
- Read endpoints can be toggled on/off for cache usage via config flag.

---

## Phase 2: Transaction-Safe Cache Consistency

### Scope
- Ensure cache updates are synchronized with committed DB writes.

### Tasks
1. Use cache-aside pattern for reads.
2. On write flows, evict/update cache only **after transaction commit**:
   - `@TransactionalEventListener(phase = AFTER_COMMIT)` for domain events
   - or transaction synchronization callback
3. Introduce version-aware payloads (`version`/`updatedAt`) to prevent stale overwrite races.
4. Add targeted invalidation rules:
   - exact entity keys on single-row mutations
   - broad list/search key invalidation on writes (short TTL fallback)

### Acceptance Criteria
- No cache mutation occurs when DB transaction rolls back.
- Read-after-write consistency is validated in integration tests.
- Concurrent write scenarios do not leave stale cache values.

---

## Phase 3: Multi-Instance Invalidation Strategy

### Scope
- Keep cache coherent across services/instances when data changes.

### Tasks
1. Publish invalidation events from write-owning services (domain event/outbox).
2. Subscribe in dependent services and evict impacted keys.
3. Add retry/dead-letter handling for invalidation events.
4. Add operational dashboard metrics for invalidation lag/failures.

### Acceptance Criteria
- Cross-service cache invalidation events are processed reliably.
- Stale cross-service reads are below agreed threshold.
- Invalidation lag is observable and alertable.

---

## Phase 4: Locking and Concurrency Hardening

### Scope
- Ensure correctness under concurrent access at scale.

### Tasks
1. Keep DB uniqueness constraints as source-of-truth guards:
   - payment `order_id` unique
2. Use row-level pessimistic locking only in mutate-critical paths.
3. Keep read paths lock-free unless explicitly required.
4. Add idempotency keys where externally retried operations exist.

### Acceptance Criteria
- Concurrent create/update operations preserve business invariants.
- Duplicate payment creation attempts resolve deterministically.
- Contention hotspots are measured and acceptable.

---

## Phase 5: Java 21 Runtime Rollout

### Scope
- Adopt Java 21 capabilities with controlled risk.

### Tasks
1. Keep feature flag for virtual threads:
   - `spring.threads.virtual.enabled=${VIRTUAL_THREADS_ENABLED:false}`
2. Run controlled load tests for each service with virtual threads OFF vs ON.
3. Add structured concurrency (`StructuredTaskScope`) in fan-out orchestration paths where useful.
4. Continue migrating mutable DTO boundaries to immutable records where possible.

### Acceptance Criteria
- Virtual threads rollout decision is based on measured latency/throughput and error behavior.
- Structured concurrency is used only where it reduces latency or simplifies cancellation/timeouts.
- No regression in correctness or observability.

---

## Phase 6: Performance and Profiling Governance

### Scope
- Make load/perf testing repeatable and traceable.

### Tasks
1. Use `performance-loadtest` module for endpoint-level load tests.
2. Run separate profiling workflows (JFR + async-profiler) per scenario.
3. Record baselines and trends per endpoint:
   - throughput
   - p95/p99 latency
   - error rate
   - lock contention
   - allocation/GC hotspots
4. Define and enforce performance SLO gates in CI for key endpoints.

### Acceptance Criteria
- Baseline reports are produced and versioned.
- Regressions are detectable before release.
- Team has a documented playbook for investigating regressions.

---

## Risks and Mitigations

1. Risk: stale cache due to missed invalidation.
- Mitigation: after-commit invalidation + TTL safety net + versioned payload checks.

2. Risk: Redis outage impact.
- Mitigation: graceful fallback to DB reads with circuit-breaker/rate limits.

3. Risk: over-invalidation reducing cache benefit.
- Mitigation: key design review and endpoint-level hit-rate monitoring.

4. Risk: locking reduces throughput.
- Mitigation: lock only mutation-critical paths, benchmark contention behavior.

---

## Recommended Execution Order

1. Phase 1 and Phase 2
2. Phase 4
3. Phase 3
4. Phase 5
5. Phase 6

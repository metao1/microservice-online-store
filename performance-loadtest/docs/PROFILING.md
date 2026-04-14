# Java Load Test + Profiling (Separate Workflow)

This load test is intentionally separate from unit/integration tests.

## 1) Run the target microservice

Example: payment service

```bash
./gradlew :payment-microservice:bootRun
```

## 2) Run Java load test (separate process)

```bash
  ./gradlew :performance-loadtest:run --args='--url http://localhost:8084/payments/status/PENDING?offset=0&limit=10 --users 100 --target-rps 120 --warmup-sec 15 --duration-sec 120 --think-ms 5'
```

The runner writes JSON reports under `performance-loadtest/reports/`.

Preferred repeatable workflow:

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario payment-status-page'
```

You can still override selected values from the CLI, for example:

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario inventory-category-page --users 120 --max-p95-ms 150'
```

### Scenario format

Scenario files are JSON and contain a `scenarios` array. Each scenario can define:

- `name`
- `variables`
- `request.url`
- `request.method`
- `request.headers`
- `request.body` or `request.bodyFile`
- `steps[]` for multi-step workflows
- `steps[].extract` for simple response extraction such as `$.value` or `$.paymentId`
- `steps[].expectedStatus`
- `steps[].maxAttempts`
- `steps[].retryDelayMs`
- `load.users`
- `load.targetRps`
- `load.durationSec`
- `load.warmupSec`
- `load.timeoutSec`
- `load.thinkMs`
- `thresholds.maxErrorRatePct`
- `thresholds.minThroughputRps`
- `thresholds.maxP95Ms`
- `thresholds.maxP99Ms`
- `comparison.compareTo`
- `comparison.maxThroughputDropPct`
- `comparison.maxP95RegressionPct`
- `comparison.maxP99RegressionPct`
- `comparison.maxErrorRateIncreasePct`

If a threshold is violated, the runner exits with a non-zero status so it can be used as a CI gate.

### End-to-end checkout workflow

The sample scenario `bookstore-checkout-flow` models a lightweight checkout transaction across services:

1. add an item to the cart in `order-microservice`
2. create an order
3. poll `payment-microservice` until the payment record exists
4. process the payment
5. read the product from `inventory-microservice`

It uses:

- scenario variables such as `userId` and `sku`
- `${...}` templating in URLs and bodies
- response extraction from JSON fields
- retries for eventual-consistency steps like payment lookup

Run it with:

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario bookstore-checkout-flow'
```

For strict validation across order/payment/inventory (including inventory reduction assertion), use:

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario bookstore-e2e-order-payment-inventory'
```

This scenario:

- captures product volume before checkout
- creates order and processes payment
- asserts `$.status == SUCCESSFUL` on payment processing response
- retries inventory read and asserts `$.volume < ${beforeVolume}`

### Artifacts

Each run now writes:

- `*.json` machine-readable report
- `*.txt` human-readable summary
- per-step latency stats (`stepLatencyMs`) for multi-step scenarios

`throughputRps` and `totalWorkflows` represent completed workflow iterations per second/count (not raw HTTP request count).

Both are stored under `performance-loadtest/reports/` by default, or a custom directory via `--report-dir`.

## 3) Profile separately with JFR

Start service with JFR enabled:

```bash
JAVA_TOOL_OPTIONS='-XX:StartFlightRecording=filename=payment-loadtest.jfr,settings=profile,dumponexit=true' \
./gradlew :payment-microservice:bootRun
```

Then run the load test as in step 2.

Inspect the recording in JDK Mission Control:

```bash
jmc
```

## 4) Profile separately with async-profiler (optional)

```bash
# find PID
jcmd | grep payment-microservice

# CPU flamegraph (example)
./profiler.sh -d 60 -e cpu -f payment-cpu.svg <PID>

# Allocation flamegraph (example)
./profiler.sh -d 60 -e alloc -f payment-alloc.svg <PID>
```

## 5) What to analyze

- Throughput (`throughputRps`) and error ratio (`failures / requests`) from load-test JSON.
- Tail latency (`p95`, `p99`) rather than average only.
- JFR hot methods and blocked threads:
  - lock contention
  - socket waits
  - JDBC time
- async-profiler hotspots:
  - CPU flamegraph for expensive code paths
  - allocation flamegraph for object churn/GC pressure

## 6) Pass/fail baseline suggestions

- Error rate: `< 1%`
- p95 latency: `< 200ms` for read endpoints
- p99 latency: `< 500ms`
- No sustained lock contention spikes
- No long GC pauses during steady-state load

## 7) Example CI-style gate

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario payment-status-page --max-error-rate-pct 1 --max-p95-ms 200 --max-p99-ms 500'
```

If the scenario breaches any configured threshold, the process exits non-zero and the report records the failed checks.

## 8) Baseline regression gate

Compare the current run to a previous report and fail on drift:

```bash
./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario inventory-category-page --compare-to performance-loadtest/reports/inventory-category-page-20260414-101530.json --max-throughput-drop-pct 10 --max-p95-regression-pct 15 --max-p99-regression-pct 20 --max-error-rate-increase-pct 1'
```

This checks:

- throughput drop percentage versus baseline
- p95 and p99 latency regression percentages
- error-rate increase percentage points

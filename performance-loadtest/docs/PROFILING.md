# Java Load Test + Profiling (Separate Workflow)

This load test is intentionally separate from unit/integration tests.

## 1) Run the target microservice

Example: payment service

```bash
./gradlew :payment-microservice:bootRun
```

## 2) Run Java load test (separate process)

```bash
./gradlew :performance-loadtest:run --args='--url http://localhost:8084/payments/status/PENDING?offset=0&limit=10 --users 100 --warmup-sec 15 --duration-sec 120 --think-ms 5'
```

The runner writes JSON reports under `performance-loadtest/reports/`.

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

# performance-loadtest

A self-contained, zero-agent Java 21 load-test harness for the online-store
microservices. It lives alongside the services in this repo so a scenario can
exercise the full order → payment → inventory choreography end-to-end.

- Separate Gradle module, no Spring runtime — starts in milliseconds.
- Virtual threads: one per virtual user, thousands of concurrent workflows.
- HDR Histogram with coordinated-omission correction for honest tail latency.
- Multi-step workflows with JSON extraction, templating, assertions, retries.
- W3C `traceparent` injection so load samples correlate in Jaeger / OTEL.
- CI-friendly thresholds + baseline regression gates (non-zero exit on drift).

For deep-dive topics (JFR profiling, async-profiler, runbooks, analysis
guidance) see [`docs/PROFILING.md`](docs/PROFILING.md).

---

## Quickstart

Start the stack, then run a scenario:

```bash
docker-compose up -d
./gradlew :performance-loadtest:run \
  --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario inventory-category-page'
```

Reports land in `performance-loadtest/reports/` as a timestamped pair:

- `*.json` — machine-readable, consumed by the baseline comparator
- `*.txt`  — human summary (same content as stdout)

Exit code is `0` on pass, non-zero on threshold or baseline regression failure,
so the same command doubles as a CI gate.

---

## Usage modes

### 1. Scenario file (recommended, repeatable)

```bash
./gradlew :performance-loadtest:run \
  --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario bookstore-checkout-flow'
```

CLI flags override individual scenario fields, e.g.
`--users 120 --max-p95-ms 150`.

### 2. Ad-hoc single request

```bash
./gradlew :performance-loadtest:run \
  --args='--url http://localhost:8084/payments/status/PENDING?offset=0&limit=10 --users 100 --duration-sec 60'
```

Useful for spot-checking a single endpoint without editing scenario JSON.

### 3. CI regression gate

```bash
./gradlew :performance-loadtest:run --args='\
  --scenario-file performance-loadtest/scenarios/bookstore-scenarios.json \
  --scenario inventory-category-page \
  --compare-to performance-loadtest/reports/inventory-category-page-baseline.json \
  --max-throughput-drop-pct 10 \
  --max-p95-regression-pct 15 \
  --max-p99-regression-pct 20 \
  --max-error-rate-increase-pct 1'
```

The run fails CI if any configured threshold or baseline drift limit is
breached. See [baseline regression gate](docs/PROFILING.md#8-baseline-regression-gate)
for details on how mismatched scenario labels are refused.

---

## Scenario format

A scenario file is `{ "scenarios": [ ... ] }`. Each entry supports:

| Field | Purpose |
| --- | --- |
| `name` | Identifier passed via `--scenario` |
| `variables` | Rendered with `${...}` into URLs/bodies; VU/iteration/uuid are injected automatically |
| `request` | Single-shot request (shortcut for a one-step scenario) |
| `steps[]` | Multi-step workflow; executed in order, extracted values flow forward |
| `steps[].request.{url,method,headers,body,bodyFile}` | Templated request spec |
| `steps[].expectedStatus` | Override the default 2xx-success rule |
| `steps[].extract` | `{ orderId: "$.value" }` pulls fields from the response into the workflow context |
| `steps[].assertions[]` | `{ path, operator, expected }` body assertions (see below) |
| `steps[].maxAttempts` / `retryDelayMs` | Retry network/HTTP errors |
| `steps[].retryOnAssertion` | Opt-in retry for assertion failures — use for eventual-consistency polling |
| `load.{users, durationSec, warmupSec, timeoutSec, thinkMs}` | Closed-model defaults |
| `load.targetRps` | Switches the stage into open-model pacing |
| `load.stages[]` | Ramped profile (overrides the top-level load fields) |
| `thresholds.{maxErrorRatePct, minThroughputRps, maxP95Ms, maxP99Ms}` | Pass/fail gate |
| `comparison.*` | Baseline regression gate config (see `--compare-to`) |

### Assertions

```json
"assertions": [
  { "path": "$.status",  "operator": "eq", "expected": "SUCCESSFUL" },
  { "path": "$.volume",  "operator": "lt", "expected": "${beforeVolume}" }
]
```

Operators: `eq`, `ne`, `contains` (strings); `lt`, `lte`, `gt`, `gte` (numeric
coercion when both sides parse as numbers). `expected` is templated against the
workflow context, so you can reference values extracted from previous steps.

By default, assertion mismatches fail fast (a genuine logical error shouldn't
burn the entire retry budget). Set `retryOnAssertion: true` to poll for
eventually-consistent state.

### Ramped load (stages)

```json
"load": {
  "warmupSec": 10,
  "timeoutSec": 5,
  "thinkMs": 5,
  "stages": [
    { "durationSec": 30, "users": 25, "targetRps": 25 },
    { "durationSec": 60, "users": 75, "targetRps": 100 },
    { "durationSec": 30, "users": 150, "targetRps": 200 },
    { "durationSec": 30, "users": 25, "targetRps": 25 }
  ]
}
```

Stages execute sequentially. Each stage gets its own VU pool and pacing window,
so a fast "cruise" stage doesn't inherit a slow "ramp" stage's workflow counter.
Latency and throughput are aggregated across all stages into a single report.

See [`scenarios/bookstore-scenarios.json`](scenarios/bookstore-scenarios.json)
for working examples covering single-request, multi-step checkout, strict
end-to-end validation, and staged ramp profiles.

---

## Report shape

Every run writes a JSON report with this structure (simplified):

```json
{
  "scenario": { "label": "...", "load": { "stages": [...] }, "steps": [...] },
  "result": {
    "totalWorkflows": 14523,
    "workflowThroughputRps": 241.5,
    "errorRatePct": 0.021,
    "paceMissCount": 0,
    "latencyMs": { "p50": 18.2, "p95": 87.4, "p99": 142.1, "p999": 410.0 },
    "stepLatencyMs": {
      "create-order": {
        "samples": 14523, "successes": 14520, "failures": 3, "retries": 5,
        "p50Ms": 12.1, "p95Ms": 44.7, "p99Ms": 78.3,
        "statusCodeCounts": { "201": 14520, "500": 3 }
      }
    },
    "errors": { "create-order:HTTP_500": 3 }
  },
  "thresholds": { "passed": true, "limits": {...} },
  "baselineComparison": { "configured": false }
}
```

Key conventions:

- **`throughputRps` counts completed workflows, not raw HTTP requests.** Use
  `stepLatencyMs[name].samples` for per-endpoint counts.
- **Latency percentiles are success-only.** A 5 s timeout failure won't
  masquerade as a 5 s "slow but successful" tail sample.
- **`paceMissCount > 0`** means the generator (or the target) couldn't sustain
  `--target-rps` — read p95/p99 with that caveat.
- **`statusCodeCounts`** uses `0` as sentinel for "no response received"
  (connection error / timeout); the CLI table renders that as `ERR:N`.

---

## CLI flags

```
--scenario-file <path>           JSON scenario file (scenarios[] array)
--scenario <name>                Named scenario in the file (required when > 1)
--url <URL>                      Ad-hoc single-request mode (incompatible with --scenario-file)
--method <GET|POST|PUT|PATCH|DELETE>
--header <Name:Value>            Repeatable
--body <json> | --body-file <path>
--label <name>                   Override report / scenario label
--users <int>                    Default 50
--target-rps <double>            Open-model pacing target (workflow starts per second)
--duration-sec <int>             Default 60
--warmup-sec <int>               Default 10
--timeout-sec <int>              Default 5
--think-ms <long>                Default 0
--report-dir <path>              Default performance-loadtest/reports
# Threshold gate (any breach → non-zero exit)
--max-error-rate-pct <double>
--min-throughput-rps <double>
--max-p95-ms <double>
--max-p99-ms <double>
# Baseline regression gate
--compare-to <path>              Baseline JSON report
--max-throughput-drop-pct <double>       (default 10)
--max-p95-regression-pct <double>        (default 15)
--max-p99-regression-pct <double>        (default 20)
--max-error-rate-increase-pct <double>   (default 1)
--force-compare                  Bypass scenario-label match guard
--help
```

---

## Design notes

- `HttpLoadTestRunner` is just a composition root; real work lives in
  `WorkloadDriver` (stages + pacing), `ScenarioExecutor` (per-workflow HTTP
  pipeline), `AssertionEvaluator`, `BaselineComparator`, `LoadTestReportWriter`.
- No mutable state on `ScenarioExecutor`; one instance is shared across all VUs.
- HDR Histograms with 3 significant digits up to 5 minutes. Success-only
  percentiles + coordinated-omission correction under open-model pacing.
- Stop conditions use `System.nanoTime()`, not wall-clock, so NTP adjustments
  can't extend or truncate the measured window.
- Body is decoded to a UTF-8 string only when the step has an assertion or
  extraction — decode cost is measurable at 10k+ RPS.
- `traceparent` is injected per request (fresh span id) with a shared trace id
  across all steps of a workflow, so an outlier can be opened directly in
  Jaeger from the report's step-level status codes.

---

## Related docs

- [`docs/PROFILING.md`](docs/PROFILING.md) — JFR / async-profiler workflow, CI
  gate recipes, analysis checklist.
- [`../docs/REGRESSION_PLAYBOOK.md`](../docs/REGRESSION_PLAYBOOK.md) — what to do
  when the SLO gate or a baseline comparison fails.
- [`../docs/ROADMAP_IMPLEMENTATION_PLAN.md`](../docs/ROADMAP_IMPLEMENTATION_PLAN.md) — where this module fits in the broader scaling plan.
- [`../docs/TRACING_DEBUG_RUNBOOK.md`](../docs/TRACING_DEBUG_RUNBOOK.md) — how
  to follow a `traceparent` emitted here into the Jaeger UI.

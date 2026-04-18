# Performance Regression Playbook

Closes the Phase 6 acceptance criterion
> Team has a documented playbook for investigating regressions.

Use this when the `Performance SLO Gate` workflow fails or when
`performance-loadtest` exits non-zero locally.

---

## 1. Classify the failure

The runner exits with one of three outcomes:

| Signal                                          | Meaning                                           |
| ----------------------------------------------- | ------------------------------------------------- |
| `Threshold violations` block in console summary | Absolute SLO breach (p95 / p99 / error / RPS)     |
| `Baseline regression` block                     | Delta against `comparison.compareTo` baseline     |
| `paceMisses > 0` with throughput below target   | Generator couldn't keep up (host-side saturation) |

Artifacts available for every run:

- `*.json` — machine-readable report (used for diffs + baseline tracking)
- `*.txt` — human-readable summary
- Per-step latency table + status-code distribution (`ERR:N` = no response)
- Nightly CI also uploads `services.log` (the Docker stack's stdout)

---

## 2. Decide: real regression or noise?

Before opening a bug, rule out noise:

1. **Re-run the same scenario twice.** CI runners vary; two consecutive
   failures against the same baseline is the bar for "signal".
2. **Check `paceMisses`.** If it is > 0 and throughput dropped, the generator
   host was saturated, not the service. Lower `targetRps` or switch runners.
3. **Check `statusCodeCounts`.** A sudden spike in `ERR:N` means connection
   failures (timeouts / reset), not latency — treat as availability incident.
4. **Check the baseline age.** A baseline older than ~30 days against a
   refactored hot path is often stale; re-baseline deliberately.

---

## 3. Localize the regression

Walk the report top-down:

1. **Scenario-level p95 / p99** — which window moved?
2. **Per-step latency table** — a single step usually dominates. The step name
   points directly at the HTTP call that regressed.
3. **`errorsByKey`** in the JSON report — format is `stepName:ERROR_KEY`.
   - `HTTP_5xx` → server-side failure
   - `HTTP_4xx` → contract drift (scenario or service)
   - `ASSERTION_FAILED` → logical regression (status is 200 but payload wrong)
   - `HttpConnectTimeoutException` / `HttpTimeoutException` → capacity /
     thread-pool / connection-pool starvation
4. **Attempts counter** — `avgAttempts` per step climbing above 1 means the
   service is flaky or eventually consistent (and the scenario was patient
   enough to retry).

---

## 4. Attach a profiler

Once the failing step is known, collect a profile from the target service
running under the *same* scenario. See
[`performance-loadtest/docs/PROFILING.md`](../performance-loadtest/docs/PROFILING.md)
for the exact JFR + async-profiler commands.

Prioritized signals to inspect (in order):

1. **Flamegraph CPU hotspots** — a single method at the top usually explains
   a p95 regression.
2. **Allocation flamegraph** — explains GC-pressure-driven tail latency
   without changing CPU share.
3. **JFR → Lock Instances** — explains contention-driven p99 under load.
4. **JFR → Socket / HTTP Client events** — explains latency that is not on
   our CPU at all (upstream DB / Kafka / Redis).
5. **DB slow-query log** when inventory / payment / order queries are
   suspected.

---

## 5. Confirm the fix

A candidate fix is accepted only when:

1. The same scenario is rerun with `--compare-to` pointing at the **pre-fix**
   baseline (not the last green one).
2. All thresholds pass **and** no regression is reported.
3. `paceMisses == 0` and `statusCodeCounts` match the baseline distribution.

Then, and only then, update the committed baseline JSON so the next run
compares against the new normal. Baselines are checked in under
`performance-loadtest/reports/`; a PR that changes one needs an explicit
reviewer call-out ("baseline bump: +3ms p95, justified by…").

---

## 6. When to raise the alarm

Open an incident (not just a bug) when **any** of these hold:

- `errorRatePct > 1.0` on any scenario that historically ran < 0.1%
- p99 more than doubled against a baseline less than 14 days old
- Error-key distribution flipped (e.g. `HTTP_500` appears on a step that
  previously only saw `HTTP_200`)
- Nightly SLO workflow has failed three nights in a row without a known PR
  cause — the regression is almost certainly environmental (Kafka image
  bump, runner change, dependency CVE patch) and needs an owner.

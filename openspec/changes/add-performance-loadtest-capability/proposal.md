# Change: Complete the performance-loadtest module

## Why
The current `performance-loadtest` module is only a minimal HTTP loop runner. It can hit a single URL and emit a JSON summary, but it does not yet provide the repeatable, scenario-driven workflow described in the roadmap for measuring service behavior, comparing runs, and enforcing performance gates.

## What Changes
- Add first-class scenario definitions so load tests can be run from versioned config files instead of only ad-hoc CLI flags.
- Extend the runner to support realistic HTTP request configuration, including headers, request bodies from files, and named scenarios.
- Extend the runner to support multi-step workflows that extract values from one response and reuse them in later requests.
- Add step-level response assertions so end-to-end scenarios can verify business side effects such as inventory volume reduction.
- Add threshold evaluation so the runner can fail a run when latency, throughput, or error-rate SLOs are breached.
- Produce a richer report set suitable for baseline tracking and CI consumption.
- Add automated tests for config parsing, threshold evaluation, and report generation.
- Document the intended usage for local profiling, baseline capture, CI-style gating, and end-to-end checkout scenarios.

## Impact
- Affected specs: `performance-loadtest`
- Affected code:
  - `performance-loadtest/src/main/java/com/metao/book/performance/HttpLoadTestRunner.java`
  - `performance-loadtest/build.gradle`
  - `performance-loadtest/docs/PROFILING.md`
  - scenario/test support for multi-step execution and checkout samples
  - new tests and sample scenarios under `performance-loadtest/`

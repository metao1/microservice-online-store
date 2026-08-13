## Context
The project roadmap expects the `performance-loadtest` module to support repeatable endpoint-level performance measurement, baseline comparison, and pass/fail SLO gating. The current module only supports a single command-line target with no versioned scenarios, no threshold enforcement, and no tests.

## Goals / Non-Goals
- Goals:
  - Make load-test runs reproducible through scenario files committed to the repository.
  - Support realistic request definitions without forcing shell-escaped JSON for every run.
  - Allow the module to fail fast when agreed latency, throughput, or error-rate thresholds are missed.
  - Preserve the simple “single URL from CLI” workflow for quick manual runs.
- Non-Goals:
  - Full browser or multi-step user journey testing.
  - Distributed load generation across multiple hosts.
  - Replacing JFR or async-profiler; this module remains the request generator/report source.

## Decisions
- Decision: Support both direct CLI options and scenario-file-driven execution.
  - Why: developers need a quick ad-hoc mode locally, while CI and baselines need versioned, named scenarios.
- Decision: Keep the implementation as a plain Java application, not a Spring Boot service.
  - Why: startup overhead should stay minimal and the runner should remain independent from the target services.
- Decision: Evaluate thresholds inside the runner and return a non-zero exit code on failure.
  - Why: this makes the module usable in CI and avoids fragile shell-side parsing of JSON reports.
- Decision: Emit a richer report payload that includes scenario metadata, thresholds, and pass/fail status.
  - Why: baseline tracking and regression analysis need more context than the current latency summary alone.

## Risks / Trade-offs
- More configuration surface can make the runner harder to use.
  - Mitigation: keep a minimal default CLI path and document scenario examples clearly.
- Strict threshold failure may be noisy on unstable local environments.
  - Mitigation: make thresholds optional and encourage separate local vs CI scenarios.
- Loading product/payment/order endpoints with nested enrichment can distort results if test data is inconsistent.
  - Mitigation: document scenario prerequisites and keep scenario definitions explicit.

## Migration Plan
1. Introduce scenario parsing and threshold config without removing existing CLI flags.
2. Keep the current `--url` execution path working as a backwards-compatible fallback.
3. Add sample scenarios and documentation that point users to the preferred workflow.
4. Use threshold-enabled scenarios in CI only after the baseline is agreed.

## Open Questions
- Which initial scenarios should be committed by default: one per service, or only a small curated set?
- Do we want a Markdown summary artifact in addition to JSON for CI readability?

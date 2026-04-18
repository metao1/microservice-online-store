package com.metao.book.performance;

/**
 * Per-step outcome + latency summary surfaced in the JSON and text reports.
 * <p>
 * {@code samples} is the total number of step executions (successes + failures).
 * Latency percentiles are computed from successful executions only so that a
 * failing retry that ran to timeout doesn't pollute the step's tail latency —
 * the same convention used for workflow-level latency in {@link LoadTestResult}.
 * {@code retries} counts extra attempts beyond the first, regardless of outcome.
 */
record StepLatencyStats(
    long samples,
    long successes,
    long failures,
    long retries,
    double minMs,
    double p50Ms,
    double p95Ms,
    double p99Ms,
    double p999Ms,
    double maxMs
) {
}

package com.metao.book.performance;

import java.util.Map;

/**
 * Per-step outcome + latency summary surfaced in the JSON and text reports.
 * <p>
 * {@code samples} is the total number of step executions (successes + failures).
 * Latency percentiles are computed from successful executions only so that a
 * failing retry that ran to timeout doesn't pollute the step's tail latency —
 * the same convention used for workflow-level latency in {@link LoadTestResult}.
 * {@code retries} counts extra attempts beyond the first, regardless of outcome.
 * <p>
 * {@code statusCodeCounts} records the distribution of terminal HTTP status
 * codes observed for this step, keyed by code. A sentinel code of {@code 0}
 * represents executions that never received a response (connection failure,
 * timeout, DNS error). Only the final attempt's status code per step execution
 * is recorded, so the total equals {@code samples}.
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
    double maxMs,
    Map<Integer, Long> statusCodeCounts
) {
}

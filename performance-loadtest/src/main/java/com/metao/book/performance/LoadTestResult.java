package com.metao.book.performance;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

record LoadTestResult(
    Instant start,
    Instant end,
    long durationMs,
    long totalWorkflows,
    long success,
    long failures,
    long responseBytes,
    // Workflows per second (1 workflow may execute multiple HTTP requests).
    double throughputRps,
    double minMs,
    double p50Ms,
    double p95Ms,
    double p99Ms,
    // Tail percentiles expose GC pauses, Kafka lag, and DB lock contention
    // that p99 often hides in distributed microservice choreography.
    double p999Ms,
    double p9999Ms,
    double maxMs,
    double errorRatePct,
    // Count of workflow starts that missed their paced target time. Nonzero
    // means the target service (or the load generator itself) couldn't sustain
    // the configured --target-rps; the reported p95/p99 should be read with
    // that caveat. Zero on closed-model runs (no --target-rps).
    long paceMissCount,
    Map<String, StepLatencyStats> stepLatencyMs,
    Map<String, Long> errors
) {

    static LoadTestResult from(
        Instant start,
        Instant end,
        LatencyHistogram.Snapshot latenciesMicros,
        long success,
        long failures,
        long responseBytes,
        long paceMissCount,
        Map<String, StepLatencyStats> stepLatencyMs,
        ConcurrentHashMap<String, LongAdder> errors
    ) {
        long durationMs = Math.max(1, Duration.between(start, end).toMillis());
        long totalWorkflows = success + failures;
        double throughputRps = totalWorkflows * 1000.0 / durationMs;
        double errorRatePct = totalWorkflows == 0 ? 0.0 : (failures * 100.0) / totalWorkflows;

        Map<String, Long> errorSnapshot = new java.util.TreeMap<>();
        errors.forEach((key, value) -> errorSnapshot.put(key, value.sum()));

        return new LoadTestResult(
            start,
            end,
            durationMs,
            totalWorkflows,
            success,
            failures,
            responseBytes,
            throughputRps,
            latenciesMicros.percentileMs(0),
            latenciesMicros.percentileMs(50),
            latenciesMicros.percentileMs(95),
            latenciesMicros.percentileMs(99),
            latenciesMicros.percentileMs(99.9),
            latenciesMicros.percentileMs(99.99),
            latenciesMicros.percentileMs(100),
            errorRatePct,
            paceMissCount,
            Map.copyOf(stepLatencyMs),
            Map.copyOf(errorSnapshot)
        );
    }
}

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
    double maxMs,
    double errorRatePct,
    Map<String, Long> errors
) {

    static LoadTestResult from(
        Instant start,
        Instant end,
        LatencyHistogram.Snapshot latenciesMicros,
        long success,
        long failures,
        long responseBytes,
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
            latenciesMicros.percentileMs(100),
            errorRatePct,
            Map.copyOf(errorSnapshot)
        );
    }
}

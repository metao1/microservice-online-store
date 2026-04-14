package com.metao.book.performance;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.LongAdder;

record LoadTestResult(
    Instant start,
    Instant end,
    long durationMs,
    long totalRequests,
    long success,
    long failures,
    long responseBytes,
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
        ConcurrentLinkedQueue<Long> latenciesMicros,
        long success,
        long failures,
        long responseBytes,
        ConcurrentHashMap<String, LongAdder> errors
    ) {
        long durationMs = Math.max(1, Duration.between(start, end).toMillis());
        long totalRequests = success + failures;
        double throughputRps = totalRequests * 1000.0 / durationMs;
        long[] sorted = latenciesMicros.stream().mapToLong(Long::longValue).sorted().toArray();
        double errorRatePct = totalRequests == 0 ? 0.0 : (failures * 100.0) / totalRequests;

        Map<String, Long> errorSnapshot = new java.util.TreeMap<>();
        errors.forEach((key, value) -> errorSnapshot.put(key, value.sum()));

        return new LoadTestResult(
            start,
            end,
            durationMs,
            totalRequests,
            success,
            failures,
            responseBytes,
            throughputRps,
            percentileMs(sorted, 0),
            percentileMs(sorted, 50),
            percentileMs(sorted, 95),
            percentileMs(sorted, 99),
            percentileMs(sorted, 100),
            errorRatePct,
            Map.copyOf(errorSnapshot)
        );
    }

    static double percentileMs(long[] sortedMicros, int percentile) {
        if (sortedMicros.length == 0) {
            return 0.0;
        }
        if (percentile <= 0) {
            return sortedMicros[0] / 1000.0;
        }
        if (percentile >= 100) {
            return sortedMicros[sortedMicros.length - 1] / 1000.0;
        }
        int index = (int) Math.ceil((percentile / 100.0) * sortedMicros.length) - 1;
        index = Math.max(0, Math.min(index, sortedMicros.length - 1));
        return sortedMicros[index] / 1000.0;
    }
}

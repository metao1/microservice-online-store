package com.metao.book.performance;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

final class StepLatencyCollector {

    /**
     * Sentinel status code recorded when a step failed before getting any
     * response (exception, timeout, DNS error, connection reset, …). Kept as
     * {@code 0} so JSON readers can treat it as a numeric bucket consistent
     * with real HTTP codes.
     */
    static final int NO_RESPONSE_STATUS = 0;

    private final List<String> orderedStepNames;
    private final ConcurrentHashMap<String, LatencyHistogram> histograms;
    private final ConcurrentHashMap<String, LongAdder> successCounts;
    private final ConcurrentHashMap<String, LongAdder> failureCounts;
    private final ConcurrentHashMap<String, LongAdder> retryCounts;
    private final ConcurrentHashMap<String, ConcurrentHashMap<Integer, LongAdder>> statusCodeCounts;

    StepLatencyCollector(List<ScenarioStep> steps) {
        this.orderedStepNames = steps.stream().map(ScenarioStep::name).toList();
        this.histograms = new ConcurrentHashMap<>();
        this.successCounts = new ConcurrentHashMap<>();
        this.failureCounts = new ConcurrentHashMap<>();
        this.retryCounts = new ConcurrentHashMap<>();
        this.statusCodeCounts = new ConcurrentHashMap<>();
        for (String stepName : orderedStepNames) {
            histograms.put(stepName, new LatencyHistogram());
            successCounts.put(stepName, new LongAdder());
            failureCounts.put(stepName, new LongAdder());
            retryCounts.put(stepName, new LongAdder());
            statusCodeCounts.put(stepName, new ConcurrentHashMap<>());
        }
    }

    /**
     * Records the outcome of a single step execution.
     *
     * @param stepName      scenario-defined step identifier
     * @param elapsedMicros wall-clock step duration including any retry back-offs
     * @param success       whether the final attempt succeeded
     * @param attempts      total number of attempts (1 = no retries)
     * @param statusCode    terminal HTTP status from the final attempt, or
     *                      {@link #NO_RESPONSE_STATUS} when the step never
     *                      received a response
     */
    void record(String stepName, long elapsedMicros, boolean success, int attempts, int statusCode) {
        if (success) {
            // Only successful executions feed the latency histogram so a timed-out
            // failing attempt can't masquerade as a slow-but-successful sample.
            LatencyHistogram histogram = histograms.computeIfAbsent(stepName, ignored -> new LatencyHistogram());
            histogram.record(elapsedMicros);
            successCounts.computeIfAbsent(stepName, ignored -> new LongAdder()).increment();
        } else {
            failureCounts.computeIfAbsent(stepName, ignored -> new LongAdder()).increment();
        }
        if (attempts > 1) {
            retryCounts.computeIfAbsent(stepName, ignored -> new LongAdder()).add(attempts - 1);
        }
        statusCodeCounts
            .computeIfAbsent(stepName, ignored -> new ConcurrentHashMap<>())
            .computeIfAbsent(statusCode, ignored -> new LongAdder())
            .increment();
    }

    Map<String, StepLatencyStats> snapshot() {
        Map<String, StepLatencyStats> stats = new LinkedHashMap<>();
        // Preserve scenario order but also include steps that only surfaced at runtime.
        List<String> allNames = new ArrayList<>(orderedStepNames);
        for (String dynamicName : histograms.keySet()) {
            if (!allNames.contains(dynamicName)) {
                allNames.add(dynamicName);
            }
        }
        for (String stepName : failureCounts.keySet()) {
            if (!allNames.contains(stepName)) {
                allNames.add(stepName);
            }
        }

        for (String stepName : allNames) {
            LatencyHistogram histogram = histograms.get(stepName);
            LatencyHistogram.Snapshot snapshot = histogram == null ? null : histogram.snapshot();
            long successes = sumOrZero(successCounts, stepName);
            long failures = sumOrZero(failureCounts, stepName);
            long retries = sumOrZero(retryCounts, stepName);
            stats.put(stepName, new StepLatencyStats(
                successes + failures,
                successes,
                failures,
                retries,
                snapshot == null ? 0.0 : snapshot.percentileMs(0),
                snapshot == null ? 0.0 : snapshot.percentileMs(50),
                snapshot == null ? 0.0 : snapshot.percentileMs(95),
                snapshot == null ? 0.0 : snapshot.percentileMs(99),
                snapshot == null ? 0.0 : snapshot.percentileMs(99.9),
                snapshot == null ? 0.0 : snapshot.percentileMs(100),
                snapshotStatusCodes(stepName)
            ));
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(stats));
    }

    private Map<Integer, Long> snapshotStatusCodes(String stepName) {
        ConcurrentHashMap<Integer, LongAdder> raw = statusCodeCounts.get(stepName);
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        // TreeMap for deterministic ascending order in JSON and table output.
        TreeMap<Integer, Long> snapshot = new TreeMap<>();
        raw.forEach((code, adder) -> snapshot.put(code, adder.sum()));
        return Collections.unmodifiableMap(new TreeMap<>(snapshot));
    }

    private static long sumOrZero(Map<String, LongAdder> source, String key) {
        LongAdder adder = source.get(key);
        return adder == null ? 0L : adder.sum();
    }
}

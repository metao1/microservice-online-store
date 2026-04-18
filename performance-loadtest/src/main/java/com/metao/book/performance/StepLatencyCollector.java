package com.metao.book.performance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

final class StepLatencyCollector {

    private final List<String> orderedStepNames;
    private final ConcurrentHashMap<String, LatencyHistogram> histograms;
    private final ConcurrentHashMap<String, LongAdder> successCounts;
    private final ConcurrentHashMap<String, LongAdder> failureCounts;
    private final ConcurrentHashMap<String, LongAdder> retryCounts;

    StepLatencyCollector(List<ScenarioStep> steps) {
        this.orderedStepNames = steps.stream().map(ScenarioStep::name).toList();
        this.histograms = new ConcurrentHashMap<>();
        this.successCounts = new ConcurrentHashMap<>();
        this.failureCounts = new ConcurrentHashMap<>();
        this.retryCounts = new ConcurrentHashMap<>();
        for (String stepName : orderedStepNames) {
            histograms.put(stepName, new LatencyHistogram());
            successCounts.put(stepName, new LongAdder());
            failureCounts.put(stepName, new LongAdder());
            retryCounts.put(stepName, new LongAdder());
        }
    }

    /**
     * Records the outcome of a single step execution.
     *
     * @param stepName      scenario-defined step identifier
     * @param elapsedMicros wall-clock step duration including any retry back-offs
     * @param success       whether the final attempt succeeded
     * @param attempts      total number of attempts (1 = no retries)
     */
    void record(String stepName, long elapsedMicros, boolean success, int attempts) {
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
                snapshot == null ? 0.0 : snapshot.percentileMs(100)
            ));
        }
        return Map.copyOf(stats);
    }

    private static long sumOrZero(Map<String, LongAdder> source, String key) {
        LongAdder adder = source.get(key);
        return adder == null ? 0L : adder.sum();
    }
}

package com.metao.book.performance;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Immutable configuration for a single load-test run.
 * <p>
 * {@code virtualUsers}, {@code durationSec}, and {@code targetRps} describe the
 * default workload. When {@code stages} is non-empty, the driver iterates those
 * stages instead and the top-level numeric fields are ignored by the driver
 * (they remain on the record for report back-compat). When {@code stages} is
 * empty the canonical constructor synthesizes a single-stage list from the
 * top-level fields so {@link WorkloadDriver} can always iterate stages
 * unconditionally.
 */
record LoadTestConfig(
    String label,
    HttpRequestSpec request,
    List<ScenarioStep> steps,
    int virtualUsers,
    Double targetRps,
    int durationSec,
    int warmupSec,
    int requestTimeoutSec,
    long thinkTimeMs,
    Path reportDir,
    LoadTestThresholds thresholds,
    BaselineComparisonConfig baselineComparison,
    String sourceDescription,
    Map<String, String> variables,
    List<LoadStage> stages
) {

    LoadTestConfig {
        steps = List.copyOf(steps == null ? List.of() : steps);
        if (request == null && steps.isEmpty()) {
            throw new IllegalArgumentException("request or steps must be provided");
        }
        request = request == null ? steps.getFirst().request() : request;
        steps = steps.isEmpty() ? List.of(new ScenarioStep("request", request, Map.of(), List.of(), null, 1, 0L)) : steps;
        if (virtualUsers < 1 || durationSec < 1 || warmupSec < 0 || requestTimeoutSec < 1 || thinkTimeMs < 0) {
            throw new IllegalArgumentException("Invalid load test numeric configuration");
        }
        if (targetRps != null && targetRps <= 0.0) {
            throw new IllegalArgumentException("targetRps must be greater than 0 when provided");
        }
        label = label == null || label.isBlank() ? "ad-hoc" : label.trim();
        reportDir = reportDir == null ? Path.of("performance-loadtest/reports") : reportDir;
        thresholds = thresholds == null ? LoadTestThresholds.none() : thresholds;
        baselineComparison = baselineComparison == null ? BaselineComparisonConfig.none() : baselineComparison;
        sourceDescription = sourceDescription == null || sourceDescription.isBlank() ? "cli" : sourceDescription;
        variables = Map.copyOf(new LinkedHashMap<>(variables == null ? Map.of() : variables));
        // Canonicalize: stages is always at least one element so the driver
        // can iterate unconditionally.
        stages = (stages == null || stages.isEmpty())
            ? List.of(new LoadStage(durationSec, virtualUsers, targetRps))
            : List.copyOf(stages);
    }

    /** Legacy constructor without stages — synthesizes a single stage. */
    LoadTestConfig(
        String label,
        HttpRequestSpec request,
        List<ScenarioStep> steps,
        int virtualUsers,
        Double targetRps,
        int durationSec,
        int warmupSec,
        int requestTimeoutSec,
        long thinkTimeMs,
        Path reportDir,
        LoadTestThresholds thresholds,
        BaselineComparisonConfig baselineComparison,
        String sourceDescription,
        Map<String, String> variables
    ) {
        this(
            label,
            request,
            steps,
            virtualUsers,
            targetRps,
            durationSec,
            warmupSec,
            requestTimeoutSec,
            thinkTimeMs,
            reportDir,
            thresholds,
            baselineComparison,
            sourceDescription,
            variables,
            List.of()
        );
    }

    /**
     * Sum of all stage durations. Useful for reports that want a single "total
     * measured time" number regardless of whether the user configured stages.
     */
    int totalStageDurationSec() {
        int total = 0;
        for (LoadStage stage : stages) {
            total += stage.durationSec();
        }
        return total;
    }

    /** Maximum virtual-user count across all stages (useful for report summaries). */
    int peakVirtualUsers() {
        int peak = 0;
        for (LoadStage stage : stages) {
            peak = Math.max(peak, stage.users());
        }
        return peak;
    }
}

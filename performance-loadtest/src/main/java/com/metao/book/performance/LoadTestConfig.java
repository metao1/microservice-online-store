package com.metao.book.performance;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    Map<String, String> variables
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
    }
}

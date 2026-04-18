package com.metao.book.performance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LoadTestReportWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .enable(SerializationFeature.INDENT_OUTPUT);

    private LoadTestReportWriter() {
    }

    static ReportArtifacts write(
        LoadTestConfig config,
        LoadTestResult result,
        List<ThresholdFailure> thresholdFailures,
        BaselineComparisonResult baselineComparison
    )
        throws IOException {
        Files.createDirectories(config.reportDir());
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(LocalDateTime.now());
        String safeLabel = config.label().replaceAll("[^a-zA-Z0-9-_]+", "-").toLowerCase();
        Path jsonReport = config.reportDir().resolve(safeLabel + "-" + timestamp + ".json");
        Path textReport = config.reportDir().resolve(safeLabel + "-" + timestamp + ".txt");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("scenario", scenarioPayload(config));
        payload.put("result", resultPayload(result));
        payload.put("thresholds", thresholdsPayload(config.thresholds(), thresholdFailures));
        payload.put("baselineComparison", baselinePayload(config, baselineComparison));

        OBJECT_MAPPER.writeValue(jsonReport.toFile(), payload);
        Files.writeString(textReport, renderSummary(config, result, thresholdFailures, baselineComparison, jsonReport));
        return new ReportArtifacts(jsonReport, textReport);
    }

    private static Map<String, Object> scenarioPayload(LoadTestConfig config) {
        Map<String, Object> scenario = new LinkedHashMap<>();
        scenario.put("label", config.label());
        scenario.put("source", config.sourceDescription());
        scenario.put("variables", config.variables());
        scenario.put("request", Map.of(
            "url", config.request().url(),
            "method", config.request().method(),
            "headers", config.request().headers(),
            "bodyPresent", config.request().hasBody(),
            "bodySource", config.request().bodySource()
        ));
        scenario.put("steps", config.steps().stream()
            .map(step -> {
                Map<String, Object> payload = new LinkedHashMap<>();
                payload.put("name", step.name());
                payload.put("url", step.request().url());
                payload.put("method", step.request().method());
                payload.put("headers", step.request().headers());
                payload.put("bodyPresent", step.request().hasBody());
                payload.put("bodySource", step.request().bodySource());
                payload.put("expectedStatus", step.expectedStatus());
                payload.put("maxAttempts", step.maxAttempts());
                payload.put("retryDelayMs", step.retryDelayMs());
                payload.put("extract", step.extract());
                payload.put("assertions", step.assertions().stream()
                    .map(assertion -> Map.of(
                        "path", assertion.path(),
                        "operator", assertion.operator(),
                        "expected", assertion.expected()
                    ))
                    .toList());
                return payload;
            })
            .toList());
        Map<String, Object> load = new LinkedHashMap<>();
        load.put("virtualUsers", config.virtualUsers());
        load.put("targetRps", config.targetRps());
        load.put("durationSec", config.durationSec());
        load.put("warmupSec", config.warmupSec());
        load.put("requestTimeoutSec", config.requestTimeoutSec());
        load.put("thinkTimeMs", config.thinkTimeMs());
        // Always emit stages so report consumers can rely on a single shape;
        // for a non-staged run this is a 1-element list synthesized from the
        // top-level fields above.
        load.put("stages", config.stages().stream()
            .map(stage -> {
                Map<String, Object> stagePayload = new LinkedHashMap<>();
                stagePayload.put("durationSec", stage.durationSec());
                stagePayload.put("users", stage.users());
                stagePayload.put("targetRps", stage.targetRps());
                return stagePayload;
            })
            .toList());
        load.put("peakVirtualUsers", config.peakVirtualUsers());
        load.put("totalStageDurationSec", config.totalStageDurationSec());
        scenario.put("load", load);
        return scenario;
    }

    private static Map<String, Object> resultPayload(LoadTestResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("start", result.start().toString());
        payload.put("end", result.end().toString());
        payload.put("durationMs", result.durationMs());
        payload.put("totalWorkflows", result.totalWorkflows());
        // Backward-compatible alias; this value counts completed workflows, not raw HTTP requests.
        payload.put("totalRequests", result.totalWorkflows());
        payload.put("success", result.success());
        payload.put("failures", result.failures());
        payload.put("errorRatePct", result.errorRatePct());
        payload.put("workflowThroughputRps", result.throughputRps());
        // Backward-compatible alias.
        payload.put("throughputRps", result.throughputRps());
        Map<String, Object> latencyMs = new LinkedHashMap<>();
        latencyMs.put("min", result.minMs());
        latencyMs.put("p50", result.p50Ms());
        latencyMs.put("p95", result.p95Ms());
        latencyMs.put("p99", result.p99Ms());
        latencyMs.put("p999", result.p999Ms());
        latencyMs.put("p9999", result.p9999Ms());
        latencyMs.put("max", result.maxMs());
        payload.put("latencyMs", latencyMs);
        payload.put("stepLatencyMs", result.stepLatencyMs());
        payload.put("responseBytes", result.responseBytes());
        payload.put("paceMissCount", result.paceMissCount());
        payload.put("errors", result.errors());
        return payload;
    }

    private static Map<String, Object> baselinePayload(
        LoadTestConfig config,
        BaselineComparisonResult baselineComparison
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("configured", baselineComparison.enabled());
        if (!baselineComparison.enabled()) {
            return payload;
        }
        payload.put("baselineReport", baselineComparison.baselineReportPath().toString());
        payload.put("passed", baselineComparison.passed());
        payload.put("metrics", baselineComparison.baselineMetrics());
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("maxThroughputDropPct", config.baselineComparison().maxThroughputDropPct());
        limits.put("maxP95RegressionPct", config.baselineComparison().maxP95RegressionPct());
        limits.put("maxP99RegressionPct", config.baselineComparison().maxP99RegressionPct());
        limits.put("maxErrorRateIncreasePct", config.baselineComparison().maxErrorRateIncreasePct());
        limits.put("forceCompare", config.baselineComparison().forceCompare());
        payload.put("limits", limits);
        payload.put("failures", baselineComparison.failures());
        return payload;
    }

    private static Map<String, Object> thresholdsPayload(
        LoadTestThresholds thresholds,
        List<ThresholdFailure> failures
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        Map<String, Object> limits = new LinkedHashMap<>();
        limits.put("maxErrorRatePct", thresholds.maxErrorRatePct());
        limits.put("minThroughputRps", thresholds.minThroughputRps());
        limits.put("maxP95Ms", thresholds.maxP95Ms());
        limits.put("maxP99Ms", thresholds.maxP99Ms());
        payload.put("configured", thresholds.isConfigured());
        payload.put("passed", failures.isEmpty());
        payload.put("limits", limits);
        payload.put("failures", failures);
        return payload;
    }

    private static String renderSummary(
        LoadTestConfig config,
        LoadTestResult result,
        List<ThresholdFailure> thresholdFailures,
        BaselineComparisonResult baselineComparison,
        Path jsonReport
    ) {
        StringBuilder summary = new StringBuilder();
        summary.append("Load test summary").append(System.lineSeparator());
        summary.append("label=").append(config.label()).append(System.lineSeparator());
        summary.append("source=").append(config.sourceDescription()).append(System.lineSeparator());
        summary.append("url=").append(config.request().url()).append(System.lineSeparator());
        summary.append("method=").append(config.request().method()).append(System.lineSeparator());
        summary.append("workflows=").append(result.totalWorkflows())
            .append(", success=").append(result.success())
            .append(", failures=").append(result.failures()).append(System.lineSeparator());
        summary.append("workflowThroughputRps=").append(String.format("%.2f", result.throughputRps())).append(System.lineSeparator());
        summary.append("errorRatePct=").append(String.format("%.3f", result.errorRatePct())).append(System.lineSeparator());
        if (result.paceMissCount() > 0) {
            summary.append("paceMissCount=").append(result.paceMissCount()).append(System.lineSeparator());
        }
        summary.append("latencyMs (success-only) min=").append(String.format("%.3f", result.minMs()))
            .append(" p50=").append(String.format("%.3f", result.p50Ms()))
            .append(" p95=").append(String.format("%.3f", result.p95Ms()))
            .append(" p99=").append(String.format("%.3f", result.p99Ms()))
            .append(" p99.9=").append(String.format("%.3f", result.p999Ms()))
            .append(" p99.99=").append(String.format("%.3f", result.p9999Ms()))
            .append(" max=").append(String.format("%.3f", result.maxMs()))
            .append(System.lineSeparator());
        if (!result.stepLatencyMs().isEmpty()) {
            summary.append("Per-step breakdown:").append(System.lineSeparator());
            summary.append(StepReportTable.render(config, result));
        }

        if (thresholdFailures.isEmpty()) {
            summary.append("thresholds=passed").append(System.lineSeparator());
        } else {
            summary.append("thresholds=failed").append(System.lineSeparator());
            for (ThresholdFailure failure : thresholdFailures) {
                summary.append(" - ")
                    .append(failure.metric())
                    .append(": expected ")
                    .append(failure.expected())
                    .append(", actual ")
                    .append(failure.actual())
                    .append(System.lineSeparator());
            }
        }

        if (baselineComparison.enabled()) {
            if (baselineComparison.passed()) {
                summary.append("baselineComparison=passed").append(System.lineSeparator());
            } else {
                summary.append("baselineComparison=failed").append(System.lineSeparator());
                for (ThresholdFailure failure : baselineComparison.failures()) {
                    summary.append(" - ")
                        .append(failure.metric())
                        .append(": expected ")
                        .append(failure.expected())
                        .append(", actual ")
                        .append(failure.actual())
                        .append(System.lineSeparator());
                }
            }
        }

        summary.append("jsonReport=").append(jsonReport.toAbsolutePath()).append(System.lineSeparator());
        return summary.toString();
    }

    record ReportArtifacts(Path jsonReport, Path textReport) {
    }
}

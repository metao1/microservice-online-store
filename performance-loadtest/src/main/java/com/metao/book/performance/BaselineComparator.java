package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class BaselineComparator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private BaselineComparator() {
    }

    static BaselineComparisonResult evaluate(LoadTestConfig config, LoadTestResult current) throws IOException {
        BaselineComparisonConfig baselineConfig = config.baselineComparison();
        if (baselineConfig == null || !baselineConfig.enabled()) {
            return BaselineComparisonResult.disabled();
        }

        Path path = baselineConfig.baselineReportPath();
        JsonNode root = OBJECT_MAPPER.readTree(path.toFile());
        guardScenarioLabel(root, path, config, baselineConfig);
        JsonNode result = root.path("result");
        JsonNode latency = result.path("latencyMs");

        double baselineThroughputRps = firstNumber(result, path, "workflowThroughputRps", "throughputRps");
        double baselineP95Ms = requiredNumber(latency, "p95", path);
        double baselineP99Ms = requiredNumber(latency, "p99", path);
        double baselineErrorRatePct = requiredNumber(result, "errorRatePct", path);

        Map<String, Double> baselineMetrics = new LinkedHashMap<>();
        baselineMetrics.put("throughputRps", baselineThroughputRps);
        baselineMetrics.put("p95Ms", baselineP95Ms);
        baselineMetrics.put("p99Ms", baselineP99Ms);
        baselineMetrics.put("errorRatePct", baselineErrorRatePct);

        List<ThresholdFailure> failures = new ArrayList<>();
        double throughputDropPct = decreasePct(baselineThroughputRps, current.throughputRps());
        if (throughputDropPct > baselineConfig.maxThroughputDropPct()) {
            failures.add(new ThresholdFailure(
                "baseline.throughputDropPct",
                "<= %.3f".formatted(baselineConfig.maxThroughputDropPct()),
                "%.3f".formatted(throughputDropPct)
            ));
        }

        double p95RegressionPct = increasePct(baselineP95Ms, current.p95Ms());
        if (p95RegressionPct > baselineConfig.maxP95RegressionPct()) {
            failures.add(new ThresholdFailure(
                "baseline.p95RegressionPct",
                "<= %.3f".formatted(baselineConfig.maxP95RegressionPct()),
                "%.3f".formatted(p95RegressionPct)
            ));
        }

        double p99RegressionPct = increasePct(baselineP99Ms, current.p99Ms());
        if (p99RegressionPct > baselineConfig.maxP99RegressionPct()) {
            failures.add(new ThresholdFailure(
                "baseline.p99RegressionPct",
                "<= %.3f".formatted(baselineConfig.maxP99RegressionPct()),
                "%.3f".formatted(p99RegressionPct)
            ));
        }

        double errorIncreasePct = current.errorRatePct() - baselineErrorRatePct;
        if (errorIncreasePct > baselineConfig.maxErrorRateIncreasePct()) {
            failures.add(new ThresholdFailure(
                "baseline.errorRateIncreasePct",
                "<= %.3f".formatted(baselineConfig.maxErrorRateIncreasePct()),
                "%.3f".formatted(errorIncreasePct)
            ));
        }

        return new BaselineComparisonResult(
            true,
            path,
            Map.copyOf(baselineMetrics),
            List.copyOf(failures)
        );
    }

    /**
     * Refuses to compare reports from different scenarios unless the caller
     * explicitly opts in with {@code forceCompare}. Without this guard, running
     * {@code --compare-to payment-status-page-report.json} against a
     * {@code inventory-category-page} run would produce meaningless deltas and
     * silently pass a CI gate. Baseline reports generated before this feature
     * existed will not carry a scenario label; in that case we log a warning
     * but let the comparison proceed so historical baselines remain usable.
     */
    private static void guardScenarioLabel(
        JsonNode root,
        Path baselinePath,
        LoadTestConfig config,
        BaselineComparisonConfig baselineConfig
    ) {
        JsonNode scenarioLabelNode = root.path("scenario").path("label");
        if (!scenarioLabelNode.isTextual()) {
            System.err.println(
                "[baseline] warning: baseline report " + baselinePath
                    + " has no scenario.label; cannot verify it matches current scenario '"
                    + config.label() + "'. Proceeding."
            );
            return;
        }
        String baselineLabel = scenarioLabelNode.textValue();
        if (baselineLabel.equals(config.label())) {
            return;
        }
        String message = "Baseline scenario label '" + baselineLabel + "' does not match current scenario '"
            + config.label() + "' (" + baselinePath + ")";
        if (baselineConfig.forceCompare()) {
            System.err.println("[baseline] warning: " + message + " — forceCompare=true, comparing anyway.");
            return;
        }
        throw new IllegalStateException(
            message + ". Re-run with --force-compare or point --compare-to at the right baseline."
        );
    }

    private static double requiredNumber(JsonNode parent, String field, Path path) {
        JsonNode value = parent.path(field);
        if (!value.isNumber()) {
            throw new IllegalArgumentException("Baseline report is missing numeric field '%s' in %s".formatted(field, path));
        }
        return value.asDouble();
    }

    private static double firstNumber(JsonNode parent, Path path, String... fields) {
        for (String field : fields) {
            JsonNode value = parent.path(field);
            if (value.isNumber()) {
                return value.asDouble();
            }
        }
        throw new IllegalArgumentException(
            "Baseline report is missing numeric field. Expected one of %s in %s".formatted(List.of(fields), path)
        );
    }

    private static double increasePct(double baseline, double current) {
        if (baseline <= 0.0) {
            return current <= 0.0 ? 0.0 : 100.0;
        }
        return ((current - baseline) / baseline) * 100.0;
    }

    private static double decreasePct(double baseline, double current) {
        if (baseline <= 0.0) {
            return 0.0;
        }
        return ((baseline - current) / baseline) * 100.0;
    }
}

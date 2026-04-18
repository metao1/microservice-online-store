package com.metao.book.performance;

import java.util.List;

/**
 * Prints the final load-test summary to stdout. Extracted from
 * {@code HttpLoadTestRunner} so the composition root stays compact and this
 * rendering can evolve (colorized output, structured log lines, etc.) without
 * touching orchestration code.
 */
final class ConsoleSummaryPrinter {

    private ConsoleSummaryPrinter() {
    }

    static void print(
        LoadTestResult result,
        List<ThresholdFailure> thresholdFailures,
        BaselineComparisonResult baselineComparison,
        LoadTestReportWriter.ReportArtifacts artifacts
    ) {
        System.out.println("Load test finished");
        System.out.println("workflows=" + result.totalWorkflows()
            + ", success=" + result.success()
            + ", failures=" + result.failures());
        System.out.println("workflowThroughput(rps)=" + String.format("%.2f", result.throughputRps()));
        System.out.println("errorRatePct=" + String.format("%.3f", result.errorRatePct()));
        System.out.println("latency(ms, success-only): min=" + String.format("%.3f", result.minMs())
            + " p50=" + String.format("%.3f", result.p50Ms())
            + " p95=" + String.format("%.3f", result.p95Ms())
            + " p99=" + String.format("%.3f", result.p99Ms())
            + " p99.9=" + String.format("%.3f", result.p999Ms())
            + " p99.99=" + String.format("%.3f", result.p9999Ms())
            + " max=" + String.format("%.3f", result.maxMs()));
        if (!result.stepLatencyMs().isEmpty()) {
            System.out.println("stepLatency(ms)");
            result.stepLatencyMs().forEach((stepName, step) -> System.out.println(
                " - " + stepName + ": samples=" + step.samples()
                    + " success=" + step.successes()
                    + " failures=" + step.failures()
                    + " retries=" + step.retries()
                    + " p95=" + String.format("%.3f", step.p95Ms())
                    + " p99=" + String.format("%.3f", step.p99Ms())
                    + " p99.9=" + String.format("%.3f", step.p999Ms())
            ));
        }

        if (thresholdFailures.isEmpty()) {
            System.out.println("thresholds=passed");
        } else {
            System.out.println("thresholds=failed");
            thresholdFailures.forEach(failure -> System.out.println(
                " - " + failure.metric() + ": expected " + failure.expected() + ", actual " + failure.actual()
            ));
        }

        if (baselineComparison.enabled()) {
            System.out.println("baselineComparison=" + (baselineComparison.passed() ? "passed" : "failed"));
            if (!baselineComparison.passed()) {
                baselineComparison.failures().forEach(failure -> System.out.println(
                    " - " + failure.metric() + ": expected " + failure.expected() + ", actual " + failure.actual()
                ));
            }
        }

        System.out.println("jsonReport=" + artifacts.jsonReport().toAbsolutePath());
        System.out.println("textReport=" + artifacts.textReport().toAbsolutePath());
    }
}

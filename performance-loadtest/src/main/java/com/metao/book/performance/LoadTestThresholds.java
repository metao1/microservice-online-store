package com.metao.book.performance;

import java.util.ArrayList;
import java.util.List;

record LoadTestThresholds(
    Double maxErrorRatePct,
    Double minThroughputRps,
    Double maxP95Ms,
    Double maxP99Ms
) {

    static LoadTestThresholds none() {
        return new LoadTestThresholds(null, null, null, null);
    }

    boolean isConfigured() {
        return maxErrorRatePct != null || minThroughputRps != null || maxP95Ms != null || maxP99Ms != null;
    }

    List<ThresholdFailure> evaluate(LoadTestResult result) {
        List<ThresholdFailure> failures = new ArrayList<>();
        if (maxErrorRatePct != null && result.errorRatePct() > maxErrorRatePct) {
            failures.add(new ThresholdFailure(
                "errorRatePct",
                "<= %.3f".formatted(maxErrorRatePct),
                "%.3f".formatted(result.errorRatePct())
            ));
        }
        if (minThroughputRps != null && result.throughputRps() < minThroughputRps) {
            failures.add(new ThresholdFailure(
                "workflowThroughputRps",
                ">= %.3f".formatted(minThroughputRps),
                "%.3f".formatted(result.throughputRps())
            ));
        }
        if (maxP95Ms != null && result.p95Ms() > maxP95Ms) {
            failures.add(new ThresholdFailure(
                "p95Ms",
                "<= %.3f".formatted(maxP95Ms),
                "%.3f".formatted(result.p95Ms())
            ));
        }
        if (maxP99Ms != null && result.p99Ms() > maxP99Ms) {
            failures.add(new ThresholdFailure(
                "p99Ms",
                "<= %.3f".formatted(maxP99Ms),
                "%.3f".formatted(result.p99Ms())
            ));
        }
        return failures;
    }
}

record ThresholdFailure(String metric, String expected, String actual) {
}

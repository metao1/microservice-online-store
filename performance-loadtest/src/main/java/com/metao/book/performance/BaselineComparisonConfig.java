package com.metao.book.performance;

import java.nio.file.Path;

record BaselineComparisonConfig(
    Path baselineReportPath,
    double maxThroughputDropPct,
    double maxP95RegressionPct,
    double maxP99RegressionPct,
    double maxErrorRateIncreasePct,
    // When true, skip the scenario-label match guard. Useful for ad-hoc
    // cross-scenario comparisons; never leave enabled in CI.
    boolean forceCompare
) {

    static final double DEFAULT_MAX_THROUGHPUT_DROP_PCT = 10.0;
    static final double DEFAULT_MAX_P95_REGRESSION_PCT = 15.0;
    static final double DEFAULT_MAX_P99_REGRESSION_PCT = 20.0;
    static final double DEFAULT_MAX_ERROR_RATE_INCREASE_PCT = 1.0;

    static BaselineComparisonConfig none() {
        return new BaselineComparisonConfig(
            null,
            DEFAULT_MAX_THROUGHPUT_DROP_PCT,
            DEFAULT_MAX_P95_REGRESSION_PCT,
            DEFAULT_MAX_P99_REGRESSION_PCT,
            DEFAULT_MAX_ERROR_RATE_INCREASE_PCT,
            false
        );
    }

    boolean enabled() {
        return baselineReportPath != null;
    }

    BaselineComparisonConfig {
        if (maxThroughputDropPct < 0.0 || maxP95RegressionPct < 0.0 || maxP99RegressionPct < 0.0 || maxErrorRateIncreasePct < 0.0) {
            throw new IllegalArgumentException("Baseline regression thresholds must be non-negative");
        }
    }
}

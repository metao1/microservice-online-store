package com.metao.book.performance;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

record BaselineComparisonResult(
    boolean enabled,
    Path baselineReportPath,
    Map<String, Double> baselineMetrics,
    List<ThresholdFailure> failures
) {

    static BaselineComparisonResult disabled() {
        return new BaselineComparisonResult(false, null, Map.of(), List.of());
    }

    boolean passed() {
        return failures.isEmpty();
    }
}


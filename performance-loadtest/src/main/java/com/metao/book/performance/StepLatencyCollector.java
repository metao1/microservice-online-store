package com.metao.book.performance;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

final class StepLatencyCollector {

    private final List<String> orderedStepNames;
    private final ConcurrentHashMap<String, LatencyHistogram> histograms;

    StepLatencyCollector(List<ScenarioStep> steps) {
        this.orderedStepNames = steps.stream().map(ScenarioStep::name).toList();
        this.histograms = new ConcurrentHashMap<>();
        orderedStepNames.forEach(stepName -> histograms.put(stepName, new LatencyHistogram()));
    }

    void record(String stepName, long elapsedMicros) {
        LatencyHistogram histogram = histograms.computeIfAbsent(stepName, ignored -> new LatencyHistogram());
        histogram.record(elapsedMicros);
    }

    Map<String, StepLatencyStats> snapshot() {
        Map<String, StepLatencyStats> stats = new LinkedHashMap<>();
        for (String stepName : orderedStepNames) {
            LatencyHistogram histogram = histograms.get(stepName);
            if (histogram == null) {
                continue;
            }
            LatencyHistogram.Snapshot snapshot = histogram.snapshot();
            stats.put(stepName, new StepLatencyStats(
                snapshot.sampleCount(),
                snapshot.percentileMs(0),
                snapshot.percentileMs(50),
                snapshot.percentileMs(95),
                snapshot.percentileMs(99),
                snapshot.percentileMs(100)
            ));
        }
        return Map.copyOf(stats);
    }
}


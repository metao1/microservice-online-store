package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Map;
import org.junit.jupiter.api.Test;

class LoadTestThresholdsTest {

    @Test
    void shouldReportThresholdFailures() {
        LoadTestResult result = new LoadTestResult(
            Instant.parse("2026-04-12T10:00:00Z"),
            Instant.parse("2026-04-12T10:01:00Z"),
            60_000,
            100,
            95,
            5,
            10_000,
            8.0,
            10.0,
            30.0,
            250.0,
            600.0,
            700.0,
            750.0,
            800.0,
            5.0,
            0L,
            Map.of(),
            Map.of("HTTP_500", 5L)
        );

        var failures = new LoadTestThresholds(1.0, 10.0, 200.0, 500.0).evaluate(result);

        assertEquals(4, failures.size());
        assertTrue(failures.stream().anyMatch(failure -> failure.metric().equals("errorRatePct")));
        assertTrue(failures.stream().anyMatch(failure -> failure.metric().equals("workflowThroughputRps")));
        assertTrue(failures.stream().anyMatch(failure -> failure.metric().equals("p95Ms")));
        assertTrue(failures.stream().anyMatch(failure -> failure.metric().equals("p99Ms")));
    }
}

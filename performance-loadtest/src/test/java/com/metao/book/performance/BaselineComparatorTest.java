package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BaselineComparatorTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldDetectRegressionAgainstBaselineReport() throws Exception {
        Path baseline = tempDir.resolve("baseline.json");
        Files.writeString(baseline, """
            {
              "result": {
                "throughputRps": 100.0,
                "errorRatePct": 0.5,
                "latencyMs": {
                  "p95": 120.0,
                  "p99": 180.0
                }
              }
            }
            """);

        LoadTestConfig config = new LoadTestConfig(
            "regression-check",
            new HttpRequestSpec("GET", "http://localhost:8083/products/category/books", "", "none", Map.of()),
            List.of(new ScenarioStep(
                "request",
                new HttpRequestSpec("GET", "http://localhost:8083/products/category/books", "", "none", Map.of()),
                Map.of(),
                List.of(),
                null,
                1,
                0L
            )),
            1,
            null,
            1,
            0,
            5,
            0L,
            tempDir,
            LoadTestThresholds.none(),
            new BaselineComparisonConfig(baseline, 5.0, 5.0, 5.0, 0.25),
            "test",
            Map.of()
        );

        LoadTestResult current = new LoadTestResult(
            Instant.parse("2026-04-12T10:00:00Z"),
            Instant.parse("2026-04-12T10:00:30Z"),
            30_000,
            1000,
            950,
            50,
            100_000,
            80.0,
            10.0,
            30.0,
            140.0,
            220.0,
            300.0,
            1.0,
            Map.of(),
            Map.of("HTTP_500", 50L)
        );

        BaselineComparisonResult comparison = BaselineComparator.evaluate(config, current);

        assertTrue(comparison.enabled());
        assertFalse(comparison.passed());
        assertTrue(comparison.failures().stream().anyMatch(failure -> failure.metric().equals("baseline.throughputDropPct")));
        assertTrue(comparison.failures().stream().anyMatch(failure -> failure.metric().equals("baseline.p95RegressionPct")));
        assertTrue(comparison.failures().stream().anyMatch(failure -> failure.metric().equals("baseline.p99RegressionPct")));
        assertTrue(comparison.failures().stream().anyMatch(failure -> failure.metric().equals("baseline.errorRateIncreasePct")));
    }
}


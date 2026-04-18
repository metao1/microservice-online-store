package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.junit.jupiter.api.Test;

class StepReportTableTest {

    @Test
    void stripsProtocolAndHostFromEndpoint() {
        assertEquals("/payments/123/status",
            StepReportTable.shortenEndpoint("http://payment-svc:8080/payments/123/status"));
    }

    @Test
    void returnsBareUrlWhenNoProtocolPresent() {
        assertEquals("/already-a-path",
            StepReportTable.shortenEndpoint("/already-a-path"));
    }

    @Test
    void truncatesLongEndpointWithMiddleEllipsis() {
        String longUrl = "http://svc/very/long/segment/that/exceeds/the/sixty/character/ceiling/set/for/endpoint/display";
        String shortened = StepReportTable.shortenEndpoint(longUrl);
        assertTrue(shortened.length() <= 60,
            () -> "shortened url must fit in 60 chars, got " + shortened.length() + ": " + shortened);
        assertTrue(shortened.contains("..."),
            () -> "shortened url should include middle ellipsis: " + shortened);
        // Leading and trailing path segments preserved so both the service
        // prefix and the resource suffix remain visible.
        assertTrue(shortened.startsWith("/very/"),
            () -> "leading segment dropped: " + shortened);
        assertTrue(shortened.endsWith("display"),
            () -> "trailing segment dropped: " + shortened);
    }

    @Test
    void formatsStatusCodesDescendingByCountWithSentinelLabel() {
        Map<Integer, Long> counts = new TreeMap<>();
        counts.put(200, 42L);
        counts.put(500, 3L);
        counts.put(StepLatencyCollector.NO_RESPONSE_STATUS, 1L);

        String formatted = StepReportTable.formatStatusCodes(counts);
        // Highest-count code leads so the dominant outcome is visible first.
        assertTrue(formatted.startsWith("200:42"), () -> formatted);
        // No-response sentinel renders as "ERR:N", not "0:N", so operators
        // don't confuse it with a real HTTP code bucket.
        assertTrue(formatted.contains("ERR:1"), () -> formatted);
        assertFalse(formatted.contains("0:1"), () -> formatted);
    }

    @Test
    void returnsDashForEmptyCounts() {
        assertEquals("-", StepReportTable.formatStatusCodes(Map.of()));
    }

    @Test
    void rendersEmptyStringWhenResultHasNoSteps() {
        LoadTestConfig config = configWithSteps(List.of());
        LoadTestResult empty = resultWithSteps(Map.of());
        assertEquals("", StepReportTable.render(config, empty));
    }

    @Test
    void rendersOneRowPerStepInDeclarationOrder() {
        ScenarioStep first = new ScenarioStep(
            "lookup-payment",
            new HttpRequestSpec("GET", "http://svc/payments/${id}", "", "none", Map.of()),
            Map.of(), List.of(), null, 1, 0L, false
        );
        ScenarioStep second = new ScenarioStep(
            "read-product",
            new HttpRequestSpec("GET", "http://svc/products/${sku}", "", "none", Map.of()),
            Map.of(), List.of(), null, 1, 0L, false
        );

        Map<String, StepLatencyStats> stats = new LinkedHashMap<>();
        stats.put("lookup-payment", new StepLatencyStats(
            100, 95, 5, 2,
            1.0, 12.3, 45.6, 89.0, 95.0, 120.0,
            Map.of(200, 95L, 500, 5L)
        ));
        stats.put("read-product", new StepLatencyStats(
            95, 95, 0, 0,
            0.5, 8.1, 22.3, 33.0, 41.0, 50.0,
            Map.of(200, 95L)
        ));

        String table = StepReportTable.render(
            configWithSteps(List.of(first, second)),
            resultWithSteps(stats)
        );

        // One data row per step, in declaration order.
        assertTrue(table.indexOf("lookup-payment") < table.indexOf("read-product"),
            () -> "scenario declaration order not preserved, got:\n" + table);
        // Endpoint column shows the path-only form.
        assertTrue(table.contains("/payments/${id}"), () -> table);
        assertTrue(table.contains("/products/${sku}"), () -> table);
        // Method column present.
        assertTrue(table.contains(" GET "), () -> table);
        // Status distribution rendered.
        assertTrue(table.contains("200:95"), () -> table);
        assertTrue(table.contains("500:5"), () -> table);
    }

    private static LoadTestConfig configWithSteps(List<ScenarioStep> steps) {
        // Minimal config: parser-independent constructor that synthesizes a
        // single load stage from the scalar fields.
        return new LoadTestConfig(
            "step-table-test",
            steps.isEmpty()
                ? new HttpRequestSpec("GET", "http://svc/", "", "none", Map.of())
                : steps.get(0).request(),
            steps,
            1,
            null,
            1,
            0,
            5,
            0L,
            Path.of("build/tmp/step-table-test"),
            LoadTestThresholds.none(),
            BaselineComparisonConfig.none(),
            "step-table-test",
            Map.of()
        );
    }

    private static LoadTestResult resultWithSteps(Map<String, StepLatencyStats> steps) {
        return new LoadTestResult(
            Instant.EPOCH,
            Instant.EPOCH.plusMillis(1),
            /* durationMs */ 1L,
            /* totalWorkflows */ 100L,
            /* success */ 100L,
            /* failures */ 0L,
            /* responseBytes */ 0L,
            /* throughputRps */ 100.0,
            /* minMs */ 0.0,
            /* p50Ms */ 0.0,
            /* p95Ms */ 0.0,
            /* p99Ms */ 0.0,
            /* p999Ms */ 0.0,
            /* p9999Ms */ 0.0,
            /* maxMs */ 0.0,
            /* errorRatePct */ 0.0,
            /* paceMissCount */ 0L,
            steps,
            Map.of()
        );
    }
}

package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StepLatencyCollectorTest {

    @Test
    void recordsStatusCodeDistributionPerStep() {
        ScenarioStep step = new ScenarioStep(
            "lookup-payment",
            new HttpRequestSpec("GET", "http://svc/p/${id}", "", "none", Map.of()),
            Map.of(),
            List.of(),
            null,
            1,
            0L,
            false
        );
        StepLatencyCollector collector = new StepLatencyCollector(List.of(step));

        collector.record("lookup-payment", 1_000, true, 1, 200);
        collector.record("lookup-payment", 2_000, true, 1, 200);
        collector.record("lookup-payment", 3_000, false, 2, 500);
        collector.record("lookup-payment", 4_000, false, 1, StepLatencyCollector.NO_RESPONSE_STATUS);

        StepLatencyStats stats = collector.snapshot().get("lookup-payment");
        assertEquals(4, stats.samples());
        assertEquals(2, stats.successes());
        assertEquals(2, stats.failures());
        // One extra attempt beyond the first on the retry row → 1 retry total.
        assertEquals(1, stats.retries());

        Map<Integer, Long> codes = stats.statusCodeCounts();
        assertEquals(2L, codes.get(200));
        assertEquals(1L, codes.get(500));
        assertEquals(1L, codes.get(StepLatencyCollector.NO_RESPONSE_STATUS));
        // Sum matches samples — one code per step execution, no double-counting.
        long total = codes.values().stream().mapToLong(Long::longValue).sum();
        assertEquals(stats.samples(), total);
    }

    @Test
    void snapshotReturnsEmptyMapForStepWithNoSamples() {
        ScenarioStep step = new ScenarioStep(
            "unused",
            new HttpRequestSpec("GET", "http://svc/", "", "none", Map.of()),
            Map.of(),
            List.of(),
            null,
            1,
            0L,
            false
        );
        StepLatencyCollector collector = new StepLatencyCollector(List.of(step));

        StepLatencyStats stats = collector.snapshot().get("unused");
        assertEquals(0, stats.samples());
        assertTrue(stats.statusCodeCounts().isEmpty(),
            "steps that never ran should have an empty status-code map, not a {0: 0} entry");
    }
}

package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class LatencyHistogramTest {

    @Test
    void shouldTrackPercentilesUsingFixedMemoryHistogram() {
        LatencyHistogram histogram = new LatencyHistogram();

        histogram.record(1_000);
        histogram.record(2_000);
        histogram.record(3_000);
        histogram.record(4_000);
        histogram.record(5_000);

        LatencyHistogram.Snapshot snapshot = histogram.snapshot();

        assertEquals(5L, snapshot.sampleCount());
        assertEquals(1_000L, snapshot.minMicros());
        assertEquals(5_000L, snapshot.maxMicros());
        assertTrue(snapshot.percentileMs(50) >= 2.0);
        assertTrue(snapshot.percentileMs(50) <= 4.0);
        assertTrue(snapshot.percentileMs(95) >= 4.0);
        assertTrue(snapshot.percentileMs(100) >= 5.0);
    }

    @Test
    void shouldInflateTailPercentilesWhenLatencyExceedsExpectedInterval() {
        // Scenario: 100 fast samples at 1ms, then one 500ms stall, at a target
        // arrival interval of 10ms. Without CO correction, p99 would be ~1ms
        // because only one sample is "slow". With CO correction, HdrHistogram
        // back-fills synthetic samples representing the workflows that should
        // have been sampled during the 500ms stall window, and p99 should
        // reflect the stall.
        LatencyHistogram baseline = new LatencyHistogram();
        LatencyHistogram corrected = new LatencyHistogram();

        long expectedIntervalMicros = 10_000L; // 10 ms between arrivals
        for (int i = 0; i < 100; i += 1) {
            baseline.record(1_000L);
            corrected.recordWithExpectedInterval(1_000L, expectedIntervalMicros);
        }
        baseline.record(500_000L);
        corrected.recordWithExpectedInterval(500_000L, expectedIntervalMicros);

        double baselineP99 = baseline.snapshot().percentileMs(99);
        double correctedP99 = corrected.snapshot().percentileMs(99);

        // The corrected histogram must show a dramatically higher tail.
        assertTrue(correctedP99 > baselineP99 * 10,
            "expected CO-corrected p99 to dwarf uncorrected p99, got corrected="
                + correctedP99 + "ms, uncorrected=" + baselineP99 + "ms");

        // Real min/max stay tied to actual recorded samples, not synthetic ones.
        assertEquals(1_000L, corrected.snapshot().minMicros());
        assertEquals(500_000L, corrected.snapshot().maxMicros());
    }
}


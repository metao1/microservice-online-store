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
}


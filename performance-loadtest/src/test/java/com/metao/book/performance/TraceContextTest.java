package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class TraceContextTest {

    @Test
    void shouldProduceWellFormedTraceAndSpanIds() {
        String traceId = TraceContext.nextTraceId();
        String spanId = TraceContext.nextSpanId();

        assertEquals(32, traceId.length());
        assertEquals(16, spanId.length());
        assertTrue(traceId.matches("[0-9a-f]+"));
        assertTrue(spanId.matches("[0-9a-f]+"));
    }

    @Test
    void shouldProduceDistinctIdsAcrossCalls() {
        Set<String> seen = new HashSet<>();
        for (int i = 0; i < 1000; i += 1) {
            seen.add(TraceContext.nextTraceId());
        }
        // With 128 bits of entropy we should never collide in 1000 draws; a
        // collision almost certainly indicates a seeding / RNG regression.
        assertEquals(1000, seen.size());
    }

    @Test
    void shouldBuildW3CTraceparentWithSampledFlag() {
        String traceId = "0123456789abcdef0123456789abcdef";
        String spanId = "abcdef0123456789";

        String traceparent = TraceContext.buildTraceparent(traceId, spanId);

        assertEquals("00-0123456789abcdef0123456789abcdef-abcdef0123456789-01", traceparent);
    }

    @Test
    void shouldExposeReservedTraceIdKey() {
        // The key must be reserved so user scenario variables can't overwrite
        // the workflow trace id. If someone renames it, this test catches it.
        assertEquals("__traceId", TraceContext.TRACE_ID_KEY);
        assertNotEquals("traceId", TraceContext.TRACE_ID_KEY);
    }
}

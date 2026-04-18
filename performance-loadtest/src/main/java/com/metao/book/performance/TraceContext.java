package com.metao.book.performance;

import java.util.concurrent.ThreadLocalRandom;

/**
 * W3C Trace Context helpers. One trace id per workflow (placed in the context
 * map under {@link #TRACE_ID_KEY}), one fresh span id per HTTP request. Using
 * {@link ThreadLocalRandom} avoids the contention that {@link java.util.Random}
 * would introduce at 10k+ RPS on virtual threads.
 */
final class TraceContext {

    /**
     * Reserved key under which the workflow-scoped trace id is stored in the
     * per-workflow context map. The leading underscores keep it out of the way
     * of user-defined scenario variables.
     */
    static final String TRACE_ID_KEY = "__traceId";

    private static final char[] HEX = "0123456789abcdef".toCharArray();

    private TraceContext() {
    }

    /** 128-bit trace id, hex-encoded (32 chars). */
    static String nextTraceId() {
        return randomHex(32);
    }

    /** 64-bit span id, hex-encoded (16 chars). */
    static String nextSpanId() {
        return randomHex(16);
    }

    /**
     * Builds a W3C {@code traceparent} header value with sampled flag set.
     * Format: {@code 00-<traceId>-<spanId>-01}.
     */
    static String buildTraceparent(String traceId, String spanId) {
        return "00-" + traceId + "-" + spanId + "-01";
    }

    private static String randomHex(int length) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        char[] chars = new char[length];
        for (int index = 0; index < length; index += 1) {
            chars[index] = HEX[random.nextInt(16)];
        }
        return new String(chars);
    }
}

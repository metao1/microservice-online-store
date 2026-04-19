package com.metao.kafka;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.slf4j.MDC;

public final class KafkaTraceHeaders {

    public static final String TRACE_ID_HEADER = "x-trace-id";
    public static final String TRACE_PARENT_HEADER = "traceparent";
    private static final String TRACE_ID_MDC_KEY = "traceId";
    private static final String SPAN_ID_MDC_KEY = "spanId";

    private KafkaTraceHeaders() {
    }

    public static void enrich(Headers headers) {
        String traceId = MDC.get(TRACE_ID_MDC_KEY);
        if (traceId == null || traceId.isBlank()) {
            return;
        }

        if (!hasHeader(headers, TRACE_ID_HEADER)) {
            headers.add(TRACE_ID_HEADER, traceId.getBytes(StandardCharsets.UTF_8));
        }

        if (!hasHeader(headers, TRACE_PARENT_HEADER)) {
            Optional<String> spanId = Optional.ofNullable(MDC.get(SPAN_ID_MDC_KEY));
            spanId.filter(id -> !id.isBlank())
                .map(id -> "00-" + traceId + "-" + id + "-01")
                .ifPresent(tp -> headers.add(TRACE_PARENT_HEADER, tp.getBytes(StandardCharsets.UTF_8)));
        }
    }

    private static boolean hasHeader(Headers headers, String key) {
        Header existing = headers.lastHeader(key);
        return existing != null && existing.value() != null && existing.value().length > 0;
    }
}

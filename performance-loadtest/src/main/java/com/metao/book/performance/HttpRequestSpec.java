package com.metao.book.performance;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

record HttpRequestSpec(
    String method,
    String url,
    String body,
    String bodySource,
    Map<String, String> headers
) {

    HttpRequestSpec {
        method = normalizeMethod(method);
        url = url == null || url.isBlank() ? "" : url.trim();
        body = body == null ? "" : body;
        bodySource = bodySource == null || bodySource.isBlank() ? "none" : bodySource;
        headers = Map.copyOf(new LinkedHashMap<>(headers == null ? Map.of() : headers));
    }

    boolean hasBody() {
        return !body.isBlank();
    }

    private static String normalizeMethod(String rawMethod) {
        String normalized = rawMethod == null ? "GET" : rawMethod.trim().toUpperCase(Locale.ROOT);
        return normalized.isBlank() ? "GET" : normalized;
    }
}

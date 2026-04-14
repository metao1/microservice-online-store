package com.metao.book.performance;

import java.util.LinkedHashMap;
import java.util.Map;

record ScenarioStep(
    String name,
    HttpRequestSpec request,
    Map<String, String> extract,
    java.util.List<StepAssertion> assertions,
    Integer expectedStatus,
    int maxAttempts,
    long retryDelayMs
) {

    ScenarioStep {
        if (request == null) {
            throw new IllegalArgumentException("step request must be provided");
        }
        name = name == null || name.isBlank() ? "request" : name.trim();
        extract = Map.copyOf(new LinkedHashMap<>(extract == null ? Map.of() : extract));
        assertions = java.util.List.copyOf(assertions == null ? java.util.List.of() : assertions);
        maxAttempts = Math.max(1, maxAttempts);
        retryDelayMs = Math.max(0L, retryDelayMs);
    }

    boolean accepts(int statusCode) {
        if (expectedStatus != null) {
            return expectedStatus == statusCode;
        }
        return statusCode >= 200 && statusCode < 300;
    }
}

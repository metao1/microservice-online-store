package com.metao.book.performance;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One step of a scenario workflow.
 * <p>
 * {@code maxAttempts} / {@code retryDelayMs} apply to network errors and HTTP
 * status-code rejections. Assertion mismatches fail fast by default so a
 * genuine logical error (e.g. "status should be SUCCESSFUL but was FAILED")
 * doesn't waste the full retry budget before surfacing. Scenarios that poll
 * eventually-consistent state can set {@code retryOnAssertion} to {@code true}
 * to opt into retrying assertion failures under the same budget.
 */
record ScenarioStep(
    String name,
    HttpRequestSpec request,
    Map<String, String> extract,
    java.util.List<StepAssertion> assertions,
    Integer expectedStatus,
    int maxAttempts,
    long retryDelayMs,
    boolean retryOnAssertion
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

    /** Legacy constructor for call sites created before {@code retryOnAssertion} existed. */
    ScenarioStep(
        String name,
        HttpRequestSpec request,
        Map<String, String> extract,
        java.util.List<StepAssertion> assertions,
        Integer expectedStatus,
        int maxAttempts,
        long retryDelayMs
    ) {
        this(name, request, extract, assertions, expectedStatus, maxAttempts, retryDelayMs, false);
    }

    boolean accepts(int statusCode) {
        if (expectedStatus != null) {
            return expectedStatus == statusCode;
        }
        return statusCode >= 200 && statusCode < 300;
    }
}

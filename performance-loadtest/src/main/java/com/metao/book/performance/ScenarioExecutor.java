package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Executes a single workflow (sequence of {@link ScenarioStep}s) against a
 * target service. Thread-safe by construction: no mutable state is held on the
 * instance; every call builds its own context map. One executor instance can
 * be shared across all virtual users.
 * <p>
 * Responsibilities:
 * <ul>
 *     <li>Build the initial per-workflow context (VU, iteration, uuid, trace id, scenario variables).</li>
 *     <li>Render each step's request via {@link TemplateRenderer}.</li>
 *     <li>Inject W3C {@code traceparent} headers when not overridden by the scenario.</li>
 *     <li>Retry per-step on {@link Exception} up to {@code maxAttempts} with {@code retryDelayMs} backoff.</li>
 *     <li>Record per-step latency + success/failure/retry counters into a {@link StepLatencyCollector}.</li>
 *     <li>Feed {@code extract} values forward into the context for subsequent steps.</li>
 * </ul>
 */
final class ScenarioExecutor {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final HttpClient client;
    private final LoadTestConfig config;

    ScenarioExecutor(HttpClient client, LoadTestConfig config) {
        this.client = client;
        this.config = config;
    }

    /** Overload for callers (warmup, tests) that don't need step metrics. */
    WorkflowOutcome executeWorkflow(int virtualUser, long iteration) {
        return executeWorkflow(virtualUser, iteration, null);
    }

    WorkflowOutcome executeWorkflow(int virtualUser, long iteration, StepLatencyCollector stepLatencies) {
        Map<String, String> context = initializeContext(virtualUser, iteration);
        long responseBytes = 0L;

        for (ScenarioStep step : config.steps()) {
            long stepStart = System.nanoTime();
            StepOutcome outcome = executeStep(step, context);
            long stepElapsedMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - stepStart);
            if (stepLatencies != null) {
                stepLatencies.record(step.name(), stepElapsedMicros, outcome.success(), outcome.attempts());
            }
            responseBytes += outcome.responseBytes();
            if (!outcome.success()) {
                return new WorkflowOutcome(false, responseBytes, step.name() + ":" + outcome.errorKey());
            }
            context.putAll(outcome.extractedValues());
        }

        return new WorkflowOutcome(true, responseBytes, "none");
    }

    private StepOutcome executeStep(ScenarioStep step, Map<String, String> context) {
        String lastErrorKey = "UNKNOWN";
        long lastBytes = 0L;
        int attempt = 0;

        for (attempt = 1; attempt <= step.maxAttempts(); attempt += 1) {
            try {
                HttpRequestSpec renderedRequest = TemplateRenderer.renderRequest(step.request(), context);
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(renderedRequest.url()))
                    .timeout(Duration.ofSeconds(config.requestTimeoutSec()));

                renderedRequest.headers().forEach(requestBuilder::header);

                // Inject a W3C traceparent so the target service's OpenTelemetry
                // pipeline can correlate this synthetic request with the span
                // tree it produces. Same trace id across all steps of a workflow,
                // fresh span id per request. A user-provided traceparent in the
                // scenario always wins.
                if (!renderedRequest.headers().containsKey("traceparent")) {
                    String traceId = context.get(TraceContext.TRACE_ID_KEY);
                    if (traceId != null) {
                        requestBuilder.header(
                            "traceparent",
                            TraceContext.buildTraceparent(traceId, TraceContext.nextSpanId())
                        );
                    }
                }

                if (requiresRequestBody(renderedRequest.method())) {
                    requestBuilder.method(renderedRequest.method(),
                        HttpRequest.BodyPublishers.ofString(renderedRequest.body()));
                    if (!renderedRequest.headers().containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                } else {
                    requestBuilder.method(renderedRequest.method(), HttpRequest.BodyPublishers.noBody());
                }

                HttpResponse<String> response = client.send(
                    requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                String body = response.body() == null ? "" : response.body();
                lastBytes = body.length();

                if (step.accepts(response.statusCode())) {
                    AssertionEvaluator.evaluate(step, body, context);
                    return new StepOutcome(true, lastBytes, Map.copyOf(extractValues(step, body)), "none", attempt);
                }

                lastErrorKey = "HTTP_" + response.statusCode();
            } catch (Exception exception) {
                lastErrorKey = exception.getClass().getSimpleName();
            }

            if (attempt < step.maxAttempts()) {
                sleep(step.retryDelayMs());
            }
        }

        return new StepOutcome(false, lastBytes, Map.of(), lastErrorKey, Math.max(1, attempt - 1));
    }

    private static Map<String, String> extractValues(ScenarioStep step, String body) throws Exception {
        if (step.extract().isEmpty()) {
            return Map.of();
        }
        JsonNode root = OBJECT_MAPPER.readTree(body);
        Map<String, String> extracted = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : step.extract().entrySet()) {
            JsonNode current = JsonPathResolver.resolve(root, entry.getValue());
            if (current.isNull()) {
                throw new IllegalArgumentException("Extraction path not found: " + entry.getValue());
            }
            extracted.put(entry.getKey(), current.isTextual() ? current.textValue() : current.toString());
        }
        return extracted;
    }

    private Map<String, String> initializeContext(int virtualUser, long iteration) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("uuid", UUID.randomUUID().toString());
        context.put("vu", Integer.toString(virtualUser));
        context.put("iteration", Long.toString(iteration));
        context.put("timestampEpochMs", Long.toString(System.currentTimeMillis()));
        context.put(TraceContext.TRACE_ID_KEY, TraceContext.nextTraceId());
        context.putAll(TemplateRenderer.resolveVariables(config.variables(), context));
        return context;
    }

    private static boolean requiresRequestBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Result of executing all steps of a single workflow. {@code errorKey} is
     * {@code "none"} on success; otherwise it encodes the failing step name
     * plus its terminal error (e.g. {@code "lookup-payment:HTTP_500"}).
     */
    record WorkflowOutcome(boolean success, long responseBytes, String errorKey) {
    }

    private record StepOutcome(
        boolean success,
        long responseBytes,
        Map<String, String> extractedValues,
        String errorKey,
        int attempts
    ) {
    }
}

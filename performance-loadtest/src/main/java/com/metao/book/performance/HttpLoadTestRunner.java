package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class HttpLoadTestRunner {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private HttpLoadTestRunner() {
    }

    public static void main(String[] args) throws Exception {
        int exitCode = run(args);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    static int run(String[] args) throws Exception {
        LoadTestConfigParser.ParsedCommand parsedCommand = LoadTestConfigParser.parse(args);
        if (parsedCommand.helpRequested()) {
            System.out.println(LoadTestConfigParser.helpText());
            return 0;
        }

        LoadTestConfig config = parsedCommand.config();
        System.out.println("Starting load test");
        System.out.println("label=" + config.label() + ", source=" + config.sourceDescription());
        System.out.println("primaryTarget=" + config.request().url());
        System.out.println("steps=" + config.steps().size() + ", method=" + config.request().method());
        System.out.println("durationSec=" + config.durationSec() + ", warmupSec=" + config.warmupSec()
            + ", users=" + config.virtualUsers() + ", thinkMs=" + config.thinkTimeMs());

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        if (config.warmupSec() > 0) {
            runWarmup(client, config);
        }

        LoadTestResult result = runLoad(client, config);
        List<ThresholdFailure> thresholdFailures = config.thresholds().evaluate(result);
        LoadTestReportWriter.ReportArtifacts artifacts = LoadTestReportWriter.write(config, result, thresholdFailures);
        printSummary(result, thresholdFailures, artifacts);
        return thresholdFailures.isEmpty() ? 0 : 2;
    }

    private static void runWarmup(HttpClient client, LoadTestConfig config) {
        Instant stopAt = Instant.now().plusSeconds(config.warmupSec());
        AtomicLong workflowCounter = new AtomicLong();
        while (Instant.now().isBefore(stopAt)) {
            executeWorkflow(client, config, 0, workflowCounter.incrementAndGet());
            sleep(config.thinkTimeMs());
        }
    }

    private static LoadTestResult runLoad(HttpClient client, LoadTestConfig config) throws Exception {
        ConcurrentLinkedQueue<Long> latenciesMicros = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, LongAdder> errors = new ConcurrentHashMap<>();
        LongAdder success = new LongAdder();
        LongAdder failures = new LongAdder();
        LongAdder bytes = new LongAdder();

        Instant start = Instant.now();
        Instant stopAt = start.plusSeconds(config.durationSec());
        AtomicLong workflowCounter = new AtomicLong();

        try (ExecutorService executor = java.util.concurrent.Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < config.virtualUsers(); index += 1) {
                final int virtualUser = index + 1;
                futures.add(executor.submit(() -> {
                    while (Instant.now().isBefore(stopAt)) {
                        long workflowStart = System.nanoTime();
                        WorkflowOutcome outcome = executeWorkflow(client, config, virtualUser, workflowCounter.incrementAndGet());
                        long elapsedMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - workflowStart);

                        latenciesMicros.add(elapsedMicros);
                        bytes.add(outcome.responseBytes());
                        if (outcome.success()) {
                            success.increment();
                        } else {
                            failures.increment();
                            incrementError(errors, outcome.errorKey());
                        }
                        sleep(config.thinkTimeMs());
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        return LoadTestResult.from(start, Instant.now(), latenciesMicros, success.sum(), failures.sum(), bytes.sum(), errors);
    }

    static WorkflowOutcome executeWorkflow(HttpClient client, LoadTestConfig config, int virtualUser, long iteration) {
        Map<String, String> context = initializeContext(config, virtualUser, iteration);
        long responseBytes = 0L;

        for (ScenarioStep step : config.steps()) {
            StepOutcome outcome = executeStep(client, config, step, context);
            responseBytes += outcome.responseBytes();
            if (!outcome.success()) {
                return new WorkflowOutcome(false, responseBytes, step.name() + ":" + outcome.errorKey());
            }
            context.putAll(outcome.extractedValues());
        }

        return new WorkflowOutcome(true, responseBytes, "none");
    }

    private static StepOutcome executeStep(
        HttpClient client,
        LoadTestConfig config,
        ScenarioStep step,
        Map<String, String> context
    ) {
        String lastErrorKey = "UNKNOWN";
        long lastBytes = 0L;

        for (int attempt = 1; attempt <= step.maxAttempts(); attempt += 1) {
            try {
                HttpRequestSpec renderedRequest = renderRequest(step.request(), context);
                HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(renderedRequest.url()))
                    .timeout(Duration.ofSeconds(config.requestTimeoutSec()));

                renderedRequest.headers().forEach(requestBuilder::header);

                if (requiresRequestBody(renderedRequest.method())) {
                    requestBuilder.method(renderedRequest.method(), HttpRequest.BodyPublishers.ofString(renderedRequest.body()));
                    if (!renderedRequest.headers().containsKey("Content-Type")) {
                        requestBuilder.header("Content-Type", "application/json");
                    }
                } else {
                    requestBuilder.method(renderedRequest.method(), HttpRequest.BodyPublishers.noBody());
                }

                HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
                String body = response.body() == null ? "" : response.body();
                lastBytes = body.length();

                if (step.accepts(response.statusCode())) {
                    evaluateAssertions(step, body, context);
                    return new StepOutcome(true, lastBytes, Map.copyOf(extractValues(step, body)), "none");
                }

                lastErrorKey = "HTTP_" + response.statusCode();
            } catch (Exception exception) {
                lastErrorKey = exception.getClass().getSimpleName();
            }

            if (attempt < step.maxAttempts()) {
                sleep(step.retryDelayMs());
            }
        }

        return new StepOutcome(false, lastBytes, Map.of(), lastErrorKey);
    }

    private static Map<String, String> extractValues(ScenarioStep step, String body) throws Exception {
        if (step.extract().isEmpty()) {
            return Map.of();
        }
        JsonNode root = OBJECT_MAPPER.readTree(body);
        Map<String, String> extracted = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : step.extract().entrySet()) {
            JsonNode current = resolveJsonPath(root, entry.getValue());
            if (current.isNull()) {
                throw new IllegalArgumentException("Extraction path not found: " + entry.getValue());
            }
            extracted.put(entry.getKey(), current.isTextual() ? current.textValue() : current.toString());
        }
        return extracted;
    }

    private static void evaluateAssertions(ScenarioStep step, String body, Map<String, String> context) throws Exception {
        if (step.assertions().isEmpty()) {
            return;
        }
        JsonNode root = OBJECT_MAPPER.readTree(body);
        for (StepAssertion assertion : step.assertions()) {
            JsonNode actualNode = resolveJsonPath(root, assertion.path());
            if (!assertionMatches(actualNode, assertion, context)) {
                String actualValue = actualNode == null || actualNode.isNull() ? "null"
                    : actualNode.isTextual() ? actualNode.textValue() : actualNode.toString();
                throw new IllegalStateException(
                    "Assertion failed for %s: expected %s %s but was %s"
                        .formatted(assertion.path(), assertion.operator(), resolveTemplate(assertion.expected(), context), actualValue)
                );
            }
        }
    }

    private static boolean assertionMatches(JsonNode actualNode, StepAssertion assertion, Map<String, String> context) {
        String expectedValue = resolveTemplate(assertion.expected(), context);
        String operator = assertion.operator().trim().toLowerCase();

        if (actualNode == null || actualNode.isMissingNode() || actualNode.isNull()) {
            return "eq".equals(operator) && "null".equalsIgnoreCase(expectedValue);
        }

        if (isNumeric(actualNode, expectedValue)) {
            double actual = actualNode.asDouble();
            double expected = Double.parseDouble(expectedValue);
            return switch (operator) {
                case "eq" -> Double.compare(actual, expected) == 0;
                case "ne" -> Double.compare(actual, expected) != 0;
                case "lt" -> actual < expected;
                case "lte" -> actual <= expected;
                case "gt" -> actual > expected;
                case "gte" -> actual >= expected;
                default -> throw new IllegalArgumentException("Unsupported numeric assertion operator: " + assertion.operator());
            };
        }

        String actual = actualNode.isTextual() ? actualNode.textValue() : actualNode.toString();
        return switch (operator) {
            case "eq" -> actual.equals(expectedValue);
            case "ne" -> !actual.equals(expectedValue);
            case "contains" -> actual.contains(expectedValue);
            default -> throw new IllegalArgumentException("Unsupported assertion operator: " + assertion.operator());
        };
    }

    private static boolean isNumeric(JsonNode actualNode, String expectedValue) {
        if (!actualNode.isNumber()) {
            return false;
        }
        try {
            Double.parseDouble(expectedValue);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static JsonNode resolveJsonPath(JsonNode root, String path) {
        if (!path.startsWith("$.") || path.length() <= 2) {
            throw new IllegalArgumentException("Only simple JSON field paths are supported: " + path);
        }
        JsonNode current = root;
        for (String segment : path.substring(2).split("\\.")) {
            if (current == null) break;
            if (current.isArray()) {
                try {
                    int index = Integer.parseInt(segment);
                    current = current.get(index);
                } catch (NumberFormatException e) {
                    current = current.get(segment);
                }
            } else {
                current = current.get(segment);
            }
        }
        if (current == null || current.isMissingNode()) {
            throw new IllegalArgumentException("JSON path not found: " + path);
        }
        return current;
    }
        if (!path.startsWith("$.") || path.length() <= 2) {
            throw new IllegalArgumentException("Only simple JSON field paths are supported: " + path);
        }
        JsonNode current = root;
        for (String segment : path.substring(2).split("\\.")) {
            current = current == null ? null : current.get(segment);
        }
        if (current == null || current.isMissingNode()) {
            throw new IllegalArgumentException("JSON path not found: " + path);
        }
        return current;
    }

    private static Map<String, String> initializeContext(LoadTestConfig config, int virtualUser, long iteration) {
        Map<String, String> context = new LinkedHashMap<>();
        context.put("uuid", UUID.randomUUID().toString());
        context.put("vu", Integer.toString(virtualUser));
        context.put("iteration", Long.toString(iteration));
        context.put("timestampEpochMs", Long.toString(System.currentTimeMillis()));
        context.putAll(resolveScenarioVariables(config.variables(), context));
        return context;
    }

    private static Map<String, String> resolveScenarioVariables(Map<String, String> variables, Map<String, String> baseContext) {
        if (variables.isEmpty()) {
            return Map.of();
        }
        Map<String, String> resolved = new LinkedHashMap<>();
        Map<String, String> workingContext = new LinkedHashMap<>(baseContext);
        List<String> pending = new ArrayList<>(variables.keySet());

        for (int pass = 0; pass < variables.size(); pass += 1) {
            boolean progressed = false;
            List<String> stillPending = new ArrayList<>();

            for (String key : pending) {
                try {
                    String value = resolveTemplate(variables.get(key), workingContext);
                    resolved.put(key, value);
                    workingContext.put(key, value);
                    progressed = true;
                } catch (MissingTemplateValueException exception) {
                    stillPending.add(key);
                }
            }

            if (stillPending.isEmpty()) {
                return resolved;
            }
            if (!progressed) {
                throw new IllegalArgumentException("Unable to resolve scenario variables: " + stillPending);
            }
            pending = stillPending;
        }

        throw new IllegalArgumentException("Unable to resolve scenario variables: " + pending);
    }

    private static HttpRequestSpec renderRequest(HttpRequestSpec request, Map<String, String> context) {
        Map<String, String> headers = new LinkedHashMap<>();
        request.headers().forEach((key, value) -> headers.put(resolveTemplate(key, context), resolveTemplate(value, context)));
        return new HttpRequestSpec(
            request.method(),
            resolveTemplate(request.url(), context),
            resolveTemplate(request.body(), context),
            request.bodySource(),
            headers
        );
    }

    static String resolveTemplate(String template, Map<String, String> context) {
        if (template == null || template.isBlank()) {
            return template == null ? "" : template;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder resolved = new StringBuilder();
        int cursor = 0;
        while (matcher.find()) {
            resolved.append(template, cursor, matcher.start());
            String key = matcher.group(1);
            String value = context.get(key);
            if (value == null) {
                throw new MissingTemplateValueException(key);
            }
            resolved.append(value);
            cursor = matcher.end();
        }
        resolved.append(template.substring(cursor));
        return resolved.toString();
    }

    private static boolean requiresRequestBody(String method) {
        return "POST".equals(method) || "PUT".equals(method) || "PATCH".equals(method);
    }

    private static void incrementError(Map<String, LongAdder> errors, String key) {
        errors.computeIfAbsent(key, ignored -> new LongAdder()).increment();
    }

    private static void printSummary(
        LoadTestResult result,
        List<ThresholdFailure> thresholdFailures,
        LoadTestReportWriter.ReportArtifacts artifacts
    ) {
        System.out.println("Load test finished");
        System.out.println("workflows=" + result.totalRequests() + ", success=" + result.success() + ", failures=" + result.failures());
        System.out.println("throughput(rps)=" + String.format("%.2f", result.throughputRps()));
        System.out.println("errorRatePct=" + String.format("%.3f", result.errorRatePct()));
        System.out.println("latency(ms): min=" + String.format("%.3f", result.minMs())
            + " p50=" + String.format("%.3f", result.p50Ms())
            + " p95=" + String.format("%.3f", result.p95Ms())
            + " p99=" + String.format("%.3f", result.p99Ms())
            + " max=" + String.format("%.3f", result.maxMs()));

        if (thresholdFailures.isEmpty()) {
            System.out.println("thresholds=passed");
        } else {
            System.out.println("thresholds=failed");
            thresholdFailures.forEach(failure -> System.out.println(
                " - " + failure.metric() + ": expected " + failure.expected() + ", actual " + failure.actual()
            ));
        }

        System.out.println("jsonReport=" + artifacts.jsonReport().toAbsolutePath());
        System.out.println("textReport=" + artifacts.textReport().toAbsolutePath());
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

    record WorkflowOutcome(boolean success, long responseBytes, String errorKey) {
    }

    private record StepOutcome(boolean success, long responseBytes, Map<String, String> extractedValues, String errorKey) {
    }

    static final class MissingTemplateValueException extends RuntimeException {
        MissingTemplateValueException(String key) {
            super("Missing template value: " + key);
        }
    }
}

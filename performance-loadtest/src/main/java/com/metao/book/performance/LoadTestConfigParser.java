package com.metao.book.performance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class LoadTestConfigParser {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private LoadTestConfigParser() {
    }

    static ParsedCommand parse(String[] args) throws IOException {
        ParsedOptions options = parseOptions(args);
        if (options.helpRequested()) {
            return ParsedCommand.help();
        }
        LoadTestConfig config = options.has("--scenario-file")
            ? parseScenarioConfig(options)
            : parseDirectConfig(options);
        return ParsedCommand.run(config);
    }

    static String helpText() {
        return String.join(System.lineSeparator(),
            "Usage:",
            "  ./gradlew :performance-loadtest:run --args='--url http://localhost:8084/payments/status/PENDING?offset=0&limit=10 --users 100 --duration-sec 120'",
            "  ./gradlew :performance-loadtest:run --args='--scenario-file performance-loadtest/scenarios/bookstore-scenarios.json --scenario payments-pending'",
            "",
            "Options:",
            "  --url <URL>                       Required in direct CLI mode",
            "  --method <GET|POST|PUT|PATCH|DELETE>  Optional. Default GET",
            "  --header <Name:Value>            Optional. Repeatable",
            "  --body <json>                    Optional. Inline request body",
            "  --body-file <path>               Optional. Read request body from file",
            "  --scenario-file <path>           Optional. JSON file containing named scenarios",
            "  --scenario <name>                Optional. Scenario name inside scenario file",
            "  --label <name>                   Optional. Override report/scenario label",
            "  --users <int>                    Optional. Default 50",
            "  --target-rps <double>            Optional. Global workflow start pacing target",
            "  --duration-sec <int>             Optional. Default 60",
            "  --warmup-sec <int>               Optional. Default 10",
            "  --timeout-sec <int>              Optional. Default 5",
            "  --think-ms <long>                Optional. Default 0",
            "  --report-dir <path>              Optional. Default performance-loadtest/reports",
            "  --max-error-rate-pct <double>    Optional. Example 1.0",
            "  --min-throughput-rps <double>    Optional. Example 50.0",
            "  --max-p95-ms <double>            Optional. Example 200.0",
            "  --max-p99-ms <double>            Optional. Example 500.0",
            "  --compare-to <path>              Optional. Baseline JSON report for regression checks",
            "  --max-throughput-drop-pct <double> Optional. Default 10.0",
            "  --max-p95-regression-pct <double>  Optional. Default 15.0",
            "  --max-p99-regression-pct <double>  Optional. Default 20.0",
            "  --max-error-rate-increase-pct <double> Optional. Default 1.0",
            "  --force-compare                   Optional. Bypass scenario-label match guard",
            "  --help"
        );
    }

    private static LoadTestConfig parseDirectConfig(ParsedOptions options) throws IOException {
        HttpRequestSpec request = toRequestDefinition(
            new RequestDefinition(required(options, "--url")),
            null,
            options.single("--body"),
            options.single("--body-file"),
            options.values("--header")
        );
        ScenarioStep step = new ScenarioStep("request", request, Map.of(), List.of(), null, 1, 0L);

        return new LoadTestConfig(
            options.singleOrDefault("--label", "ad-hoc"),
            request,
            List.of(step),
            options.intValue("--users", 50),
            options.doubleValue("--target-rps", null),
            options.intValue("--duration-sec", 60),
            options.intValue("--warmup-sec", 10),
            options.intValue("--timeout-sec", 5),
            options.longValue("--think-ms", 0L),
            Path.of(options.singleOrDefault("--report-dir", "performance-loadtest/reports")),
            parseThresholds(options),
            parseBaselineComparison(options, null),
            "cli",
            Map.of()
        );
    }

    private static LoadTestConfig parseScenarioConfig(ParsedOptions options) throws IOException {
        Path scenarioFile = Path.of(required(options, "--scenario-file"));
        ScenarioFileDefinition definition = OBJECT_MAPPER.readValue(scenarioFile.toFile(), ScenarioFileDefinition.class);
        if (definition.scenarios == null || definition.scenarios.isEmpty()) {
            throw new IllegalArgumentException("Scenario file contains no scenarios: " + scenarioFile);
        }

        String requestedScenario = options.single("--scenario");
        ScenarioDefinition selectedScenario = selectScenario(definition.scenarios, requestedScenario, scenarioFile);
        if (selectedScenario.load == null) {
            selectedScenario.load = new LoadDefinition();
        }
        Path scenarioBaseDir = scenarioFile.getParent() == null ? Path.of(".") : scenarioFile.getParent();

        List<ScenarioStep> steps = toScenarioSteps(selectedScenario, scenarioBaseDir);
        LoadTestThresholds scenarioThresholds = new LoadTestThresholds(
            selectedScenario.thresholds == null ? null : selectedScenario.thresholds.maxErrorRatePct,
            selectedScenario.thresholds == null ? null : selectedScenario.thresholds.minThroughputRps,
            selectedScenario.thresholds == null ? null : selectedScenario.thresholds.maxP95Ms,
            selectedScenario.thresholds == null ? null : selectedScenario.thresholds.maxP99Ms
        );

        return new LoadTestConfig(
            options.singleOrDefault("--label", selectedScenario.name),
            steps.getFirst().request(),
            steps,
            options.intValue("--users", selectedScenario.load.users),
            options.doubleValue("--target-rps", selectedScenario.load.targetRps),
            options.intValue("--duration-sec", selectedScenario.load.durationSec),
            options.intValue("--warmup-sec", selectedScenario.load.warmupSec),
            options.intValue("--timeout-sec", selectedScenario.load.timeoutSec),
            options.longValue("--think-ms", selectedScenario.load.thinkMs),
            Path.of(options.singleOrDefault("--report-dir", defaultReportDir(selectedScenario.reportDir))),
            overrideThresholds(scenarioThresholds, options),
            parseBaselineComparison(options, selectedScenario.comparison, scenarioBaseDir),
            scenarioFile + "#" + selectedScenario.name,
            selectedScenario.variables == null ? Map.of() : selectedScenario.variables
        );
    }

    private static List<ScenarioStep> toScenarioSteps(ScenarioDefinition scenario, Path scenarioBaseDir) throws IOException {
        if (scenario.steps != null && !scenario.steps.isEmpty()) {
            List<ScenarioStep> parsedSteps = new ArrayList<>();
            for (StepDefinition stepDefinition : scenario.steps) {
                if (stepDefinition.request == null) {
                    throw new IllegalArgumentException(
                        "Step '%s' in scenario '%s' is missing request configuration"
                            .formatted(stepDefinition.name, scenario.name)
                    );
                }
                HttpRequestSpec request = toRequestDefinition(
                    stepDefinition.request,
                    scenarioBaseDir,
                    stepDefinition.request.body,
                    stepDefinition.request.bodyFile,
                    List.of()
                );
                parsedSteps.add(new ScenarioStep(
                    stepDefinition.name,
                    request,
                    stepDefinition.extract,
                    toAssertions(stepDefinition.assertions),
                    stepDefinition.expectedStatus,
                    stepDefinition.maxAttempts,
                    stepDefinition.retryDelayMs
                ));
            }
            return parsedSteps;
        }

        if (scenario.request == null) {
            throw new IllegalArgumentException("Scenario '%s' is missing request configuration".formatted(scenario.name));
        }

        HttpRequestSpec request = toRequestDefinition(
            scenario.request,
            scenarioBaseDir,
            scenario.request.body,
            scenario.request.bodyFile,
            List.of()
        );
        return List.of(new ScenarioStep("request", request, Map.of(), List.of(), null, 1, 0L));
    }

    private static List<StepAssertion> toAssertions(List<AssertionDefinition> definitions) {
        if (definitions == null || definitions.isEmpty()) {
            return List.of();
        }
        return definitions.stream()
            .map(definition -> new StepAssertion(definition.path, definition.operator, definition.expected))
            .toList();
    }

    private static HttpRequestSpec toRequestDefinition(
        RequestDefinition definition,
        Path baseDir,
        String inlineBody,
        String bodyFile,
        List<String> rawHeaders
    ) throws IOException {
        Map<String, String> headers = definition.headers == null || definition.headers.isEmpty()
            ? parseHeaders(rawHeaders)
            : definition.headers;
        String body = resolveBody(inlineBody, bodyFile, baseDir);
        String bodySource = bodySource(inlineBody, bodyFile);
        return new HttpRequestSpec(
            definition.method,
            definition.url,
            body,
            bodySource,
            headers
        );
    }

    private static String defaultReportDir(String reportDir) {
        return reportDir == null || reportDir.isBlank() ? "performance-loadtest/reports" : reportDir;
    }

    private static LoadTestThresholds overrideThresholds(LoadTestThresholds base, ParsedOptions options) {
        return new LoadTestThresholds(
            options.doubleValue("--max-error-rate-pct", base.maxErrorRatePct()),
            options.doubleValue("--min-throughput-rps", base.minThroughputRps()),
            options.doubleValue("--max-p95-ms", base.maxP95Ms()),
            options.doubleValue("--max-p99-ms", base.maxP99Ms())
        );
    }

    private static ScenarioDefinition selectScenario(
        List<ScenarioDefinition> scenarios,
        String requestedScenario,
        Path scenarioFile
    ) {
        if (requestedScenario == null || requestedScenario.isBlank()) {
            if (scenarios.size() == 1) {
                return scenarios.getFirst();
            }
            throw new IllegalArgumentException("Scenario file contains multiple scenarios; use --scenario with " + scenarioFile);
        }
        return scenarios.stream()
            .filter(scenario -> requestedScenario.equals(scenario.name))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException(
                "Scenario '%s' not found in %s".formatted(requestedScenario, scenarioFile)
            ));
    }

    private static String resolveBody(String inlineBody, String bodyFile, Path baseDir) throws IOException {
        if (inlineBody != null && bodyFile != null) {
            throw new IllegalArgumentException("Specify either inline body or body file, not both");
        }
        if (bodyFile != null && !bodyFile.isBlank()) {
            Path path = Path.of(bodyFile);
            if (baseDir != null && !path.isAbsolute()) {
                path = baseDir.resolve(path).normalize();
            }
            return Files.readString(path);
        }
        return inlineBody == null ? "" : inlineBody;
    }

    private static String bodySource(String inlineBody, String bodyFile) {
        if (bodyFile != null && !bodyFile.isBlank()) {
            return bodyFile;
        }
        return inlineBody != null && !inlineBody.isBlank() ? "inline" : "none";
    }

    private static String required(ParsedOptions options, String key) {
        String value = options.single(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Missing required option: " + key);
        }
        return value;
    }

    private static LoadTestThresholds parseThresholds(ParsedOptions options) {
        return new LoadTestThresholds(
            options.doubleValue("--max-error-rate-pct", null),
            options.doubleValue("--min-throughput-rps", null),
            options.doubleValue("--max-p95-ms", null),
            options.doubleValue("--max-p99-ms", null)
        );
    }

    private static BaselineComparisonConfig parseBaselineComparison(
        ParsedOptions options,
        ComparisonDefinition scenarioComparison,
        Path scenarioBaseDir
    ) {
        String compareToOption = options.single("--compare-to");
        Path baselinePath = null;
        if (compareToOption != null && !compareToOption.isBlank()) {
            baselinePath = Path.of(compareToOption);
        } else if (scenarioComparison != null && scenarioComparison.compareTo != null && !scenarioComparison.compareTo.isBlank()) {
            baselinePath = Path.of(scenarioComparison.compareTo);
            if (scenarioBaseDir != null && !baselinePath.isAbsolute()) {
                baselinePath = scenarioBaseDir.resolve(baselinePath).normalize();
            }
        }

        if (baselinePath == null) {
            return BaselineComparisonConfig.none();
        }

        double maxThroughputDropPct = options.doubleValue(
            "--max-throughput-drop-pct",
            scenarioComparison != null && scenarioComparison.maxThroughputDropPct != null
                ? scenarioComparison.maxThroughputDropPct
                : BaselineComparisonConfig.DEFAULT_MAX_THROUGHPUT_DROP_PCT
        );
        double maxP95RegressionPct = options.doubleValue(
            "--max-p95-regression-pct",
            scenarioComparison != null && scenarioComparison.maxP95RegressionPct != null
                ? scenarioComparison.maxP95RegressionPct
                : BaselineComparisonConfig.DEFAULT_MAX_P95_REGRESSION_PCT
        );
        double maxP99RegressionPct = options.doubleValue(
            "--max-p99-regression-pct",
            scenarioComparison != null && scenarioComparison.maxP99RegressionPct != null
                ? scenarioComparison.maxP99RegressionPct
                : BaselineComparisonConfig.DEFAULT_MAX_P99_REGRESSION_PCT
        );
        double maxErrorRateIncreasePct = options.doubleValue(
            "--max-error-rate-increase-pct",
            scenarioComparison != null && scenarioComparison.maxErrorRateIncreasePct != null
                ? scenarioComparison.maxErrorRateIncreasePct
                : BaselineComparisonConfig.DEFAULT_MAX_ERROR_RATE_INCREASE_PCT
        );

        boolean forceCompare = options.has("--force-compare")
            || (scenarioComparison != null && Boolean.TRUE.equals(scenarioComparison.forceCompare));

        return new BaselineComparisonConfig(
            baselinePath,
            maxThroughputDropPct,
            maxP95RegressionPct,
            maxP99RegressionPct,
            maxErrorRateIncreasePct,
            forceCompare
        );
    }

    private static BaselineComparisonConfig parseBaselineComparison(
        ParsedOptions options,
        Path scenarioBaseDir
    ) {
        return parseBaselineComparison(options, null, scenarioBaseDir);
    }

    private static Map<String, String> parseHeaders(List<String> rawHeaders) {
        Map<String, String> headers = new LinkedHashMap<>();
        for (String rawHeader : rawHeaders) {
            int separator = rawHeader.indexOf(':');
            if (separator <= 0 || separator == rawHeader.length() - 1) {
                throw new IllegalArgumentException("Invalid header format, expected Name:Value -> " + rawHeader);
            }
            String name = rawHeader.substring(0, separator).trim();
            String value = rawHeader.substring(separator + 1).trim();
            headers.put(name, value);
        }
        return headers;
    }

    // Flags that take no value; their presence alone is truthy.
    private static final java.util.Set<String> BOOLEAN_FLAGS = java.util.Set.of("--force-compare");

    private static ParsedOptions parseOptions(String[] args) {
        Map<String, List<String>> options = new LinkedHashMap<>();
        boolean helpRequested = false;

        for (int index = 0; index < args.length; index += 1) {
            String key = args[index];
            if ("--help".equals(key)) {
                helpRequested = true;
                continue;
            }
            if (!key.startsWith("--")) {
                throw new IllegalArgumentException("Unexpected argument: " + key);
            }
            if (BOOLEAN_FLAGS.contains(key)) {
                options.computeIfAbsent(key, ignored -> new ArrayList<>()).add("true");
                continue;
            }
            if (index + 1 >= args.length) {
                throw new IllegalArgumentException("Missing value for: " + key);
            }
            options.computeIfAbsent(key, ignored -> new ArrayList<>()).add(args[++index]);
        }

        return new ParsedOptions(options, helpRequested);
    }

    record ParsedCommand(boolean helpRequested, LoadTestConfig config) {
        static ParsedCommand help() {
            return new ParsedCommand(true, null);
        }

        static ParsedCommand run(LoadTestConfig config) {
            return new ParsedCommand(false, config);
        }
    }

    record ParsedOptions(Map<String, List<String>> values, boolean helpRequested) {

        boolean has(String key) {
            return values.containsKey(key);
        }

        String single(String key) {
            List<String> entries = values(key);
            if (entries.isEmpty()) {
                return null;
            }
            if (entries.size() > 1) {
                throw new IllegalArgumentException("Option provided multiple times: " + key);
            }
            return entries.getFirst();
        }

        String singleOrDefault(String key, String defaultValue) {
            String value = single(key);
            return value == null ? defaultValue : value;
        }

        List<String> values(String key) {
            return values.getOrDefault(key, List.of());
        }

        int intValue(String key, int defaultValue) {
            String raw = single(key);
            return raw == null ? defaultValue : Integer.parseInt(raw);
        }

        long longValue(String key, long defaultValue) {
            String raw = single(key);
            return raw == null ? defaultValue : Long.parseLong(raw);
        }

        Double doubleValue(String key, Double defaultValue) {
            String raw = single(key);
            if (raw == null) {
                return defaultValue;
            }
            return Double.valueOf(raw);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ScenarioFileDefinition {
        public List<ScenarioDefinition> scenarios = List.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ScenarioDefinition {
        public String name;
        public RequestDefinition request;
        public List<StepDefinition> steps = List.of();
        public LoadDefinition load;
        public ThresholdDefinition thresholds;
        public ComparisonDefinition comparison;
        public String reportDir;
        public Map<String, String> variables = Map.of();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class StepDefinition {
        public String name;
        public RequestDefinition request;
        public Map<String, String> extract = Map.of();
        public List<AssertionDefinition> assertions = List.of();
        public Integer expectedStatus;
        public int maxAttempts = 1;
        public long retryDelayMs = 0L;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class AssertionDefinition {
        public String path;
        public String operator;
        public String expected;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class RequestDefinition {
        public String url;
        public String method = "GET";
        public String body;
        public String bodyFile;
        public Map<String, String> headers = Map.of();

        RequestDefinition() {
        }

        RequestDefinition(String url) {
            this.url = url;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class LoadDefinition {
        public int users = 50;
        public Double targetRps;
        public int durationSec = 60;
        public int warmupSec = 10;
        public int timeoutSec = 5;
        public long thinkMs = 0L;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ThresholdDefinition {
        public Double maxErrorRatePct;
        public Double minThroughputRps;
        public Double maxP95Ms;
        public Double maxP99Ms;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static final class ComparisonDefinition {
        public String compareTo;
        public Double maxThroughputDropPct;
        public Double maxP95RegressionPct;
        public Double maxP99RegressionPct;
        public Double maxErrorRateIncreasePct;
        public Boolean forceCompare;
    }
}

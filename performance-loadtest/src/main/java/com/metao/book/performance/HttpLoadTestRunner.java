package com.metao.book.performance;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

/**
 * Composition root for the end-to-end microservice load tester.
 * <p>
 * Orchestration only: parses CLI + scenario JSON into a {@link LoadTestConfig},
 * builds an {@link HttpClient}, delegates warmup / measured load to
 * {@link WorkloadDriver}, runs threshold + baseline checks, writes reports,
 * and exits with the right code for CI. All real work lives in the extracted
 * collaborators ({@link ScenarioExecutor}, {@link TemplateRenderer},
 * {@link JsonPathResolver}, {@link AssertionEvaluator}, {@link TraceContext},
 * {@link ConsoleSummaryPrinter}).
 */
public final class HttpLoadTestRunner {

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
        announceStart(config);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        WorkloadDriver driver = new WorkloadDriver(config, client);
        driver.runWarmup();
        LoadTestResult result = driver.runLoad();

        List<ThresholdFailure> thresholdFailures = config.thresholds().evaluate(result);
        BaselineComparisonResult baselineComparison = BaselineComparator.evaluate(config, result);
        LoadTestReportWriter.ReportArtifacts artifacts = LoadTestReportWriter.write(
            config,
            result,
            thresholdFailures,
            baselineComparison
        );
        ConsoleSummaryPrinter.print(result, thresholdFailures, baselineComparison, artifacts);
        return thresholdFailures.isEmpty() && baselineComparison.passed() ? 0 : 2;
    }

    private static void announceStart(LoadTestConfig config) {
        System.out.println("Starting load test");
        System.out.println("label=" + config.label() + ", source=" + config.sourceDescription());
        System.out.println("primaryTarget=" + config.request().url());
        System.out.println("steps=" + config.steps().size() + ", method=" + config.request().method());
        System.out.println("durationSec=" + config.durationSec()
            + ", warmupSec=" + config.warmupSec()
            + ", users=" + config.virtualUsers()
            + ", thinkMs=" + config.thinkTimeMs());
        if (config.targetRps() != null) {
            System.out.println("targetRps=" + String.format("%.2f", config.targetRps()));
        }
        if (config.baselineComparison().enabled()) {
            System.out.println("baselineReport=" + config.baselineComparison().baselineReportPath());
        }
    }

    /**
     * Test-only helper: executes one workflow against {@code client} using the
     * supplied {@code config} and returns the outcome. Retained as a thin
     * facade so {@code HttpLoadTestRunnerFlowTest} does not need to reach into
     * {@link ScenarioExecutor} directly. Production code should use
     * {@link WorkloadDriver}.
     */
    static ScenarioExecutor.WorkflowOutcome executeWorkflow(
        HttpClient client,
        LoadTestConfig config,
        int virtualUser,
        long iteration
    ) {
        return new ScenarioExecutor(client, config).executeWorkflow(virtualUser, iteration);
    }
}

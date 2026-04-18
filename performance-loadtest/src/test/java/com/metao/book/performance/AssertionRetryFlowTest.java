package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Verifies the {@code retryOnAssertion} step flag changes behaviour as
 * documented in {@link ScenarioStep}. Both paths must still produce a single
 * step-level {@code "ASSERTION_FAILED"} error key when the assertion never
 * passes, and the retry path must actually call the endpoint
 * {@code maxAttempts} times.
 */
class AssertionRetryFlowTest {

    @TempDir
    Path tempDir;

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void shouldFailFastOnAssertionByDefault() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/poll", exchange -> {
            calls.incrementAndGet();
            respond(exchange, 200, "{\"status\":\"PENDING\"}");
        });
        server.start();

        LoadTestConfig config = singleStepConfig(
            "/poll",
            new StepAssertion("$.status", "eq", "SUCCESSFUL"),
            /* maxAttempts */ 5,
            /* retryOnAssertion */ false
        );

        ScenarioExecutor.WorkflowOutcome outcome = HttpLoadTestRunner.executeWorkflow(
            HttpClient.newHttpClient(), config, 1, 1
        );

        assertFalse(outcome.success());
        assertTrue(outcome.errorKey().endsWith(":ASSERTION_FAILED"));
        // Fail-fast: no retry budget consumed for a logical mismatch.
        assertEquals(1, calls.get());
    }

    @Test
    void shouldRetryWhenRetryOnAssertionIsEnabled() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/poll", exchange -> {
            int attempt = calls.incrementAndGet();
            // Eventually consistent: third attempt succeeds.
            String status = attempt < 3 ? "PENDING" : "SUCCESSFUL";
            respond(exchange, 200, "{\"status\":\"" + status + "\"}");
        });
        server.start();

        LoadTestConfig config = singleStepConfig(
            "/poll",
            new StepAssertion("$.status", "eq", "SUCCESSFUL"),
            /* maxAttempts */ 5,
            /* retryOnAssertion */ true
        );

        ScenarioExecutor.WorkflowOutcome outcome = HttpLoadTestRunner.executeWorkflow(
            HttpClient.newHttpClient(), config, 1, 1
        );

        assertTrue(outcome.success(), "should succeed on third attempt");
        assertEquals(3, calls.get());
    }

    @Test
    void shouldExhaustBudgetWhenAssertionNeverPasses() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/poll", exchange -> {
            calls.incrementAndGet();
            respond(exchange, 200, "{\"status\":\"PENDING\"}");
        });
        server.start();

        LoadTestConfig config = singleStepConfig(
            "/poll",
            new StepAssertion("$.status", "eq", "SUCCESSFUL"),
            /* maxAttempts */ 4,
            /* retryOnAssertion */ true
        );

        ScenarioExecutor.WorkflowOutcome outcome = HttpLoadTestRunner.executeWorkflow(
            HttpClient.newHttpClient(), config, 1, 1
        );

        assertFalse(outcome.success());
        assertTrue(outcome.errorKey().endsWith(":ASSERTION_FAILED"));
        assertEquals(4, calls.get(), "retry budget should be fully consumed");
    }

    private LoadTestConfig singleStepConfig(
        String path,
        StepAssertion assertion,
        int maxAttempts,
        boolean retryOnAssertion
    ) {
        int port = server.getAddress().getPort();
        HttpRequestSpec request = new HttpRequestSpec(
            "GET",
            "http://localhost:" + port + path,
            "",
            "none",
            Map.of("Accept", "application/json")
        );
        ScenarioStep step = new ScenarioStep(
            "poll",
            request,
            Map.of(),
            List.of(assertion),
            200,
            maxAttempts,
            // Retry delay short enough not to slow the suite; large enough not
            // to spin tight against the in-process server.
            5L,
            retryOnAssertion
        );
        return new LoadTestConfig(
            "assertion-retry",
            request,
            List.of(step),
            1,
            null,
            1,
            0,
            5,
            0L,
            tempDir,
            LoadTestThresholds.none(),
            BaselineComparisonConfig.none(),
            "test",
            Map.of()
        );
    }

    private static void respond(HttpExchange exchange, int statusCode, String body) throws IOException {
        byte[] payload = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(statusCode, payload.length);
        try (OutputStream outputStream = exchange.getResponseBody()) {
            outputStream.write(payload);
        } finally {
            exchange.close();
        }
    }
}

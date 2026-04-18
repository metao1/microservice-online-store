package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
 * End-to-end check that {@link WorkloadDriver} iterates {@link LoadStage}s
 * sequentially and that all of them feed the shared accumulators in the
 * resulting {@link LoadTestResult}.
 */
class StagedWorkloadDriverTest {

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
    void shouldRunStagesSequentiallyAndAggregateResults() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/ping", exchange -> {
            calls.incrementAndGet();
            respond(exchange, 200, "{\"ok\":true}");
        });
        server.start();

        int port = server.getAddress().getPort();
        HttpRequestSpec request = new HttpRequestSpec(
            "GET",
            "http://localhost:" + port + "/ping",
            "",
            "none",
            Map.of()
        );
        ScenarioStep step = new ScenarioStep("ping", request, Map.of(), List.of(), 200, 1, 0L);

        LoadTestConfig config = new LoadTestConfig(
            "staged",
            request,
            List.of(step),
            // Top-level virtualUsers/durationSec/targetRps are intentionally
            // small — the canonical constructor will keep them unused since we
            // pass an explicit stages list.
            1,
            null,
            1,
            // No warmup so the test is fast and focused on the staged pass.
            0,
            5,
            0L,
            tempDir,
            LoadTestThresholds.none(),
            BaselineComparisonConfig.none(),
            "test",
            Map.of(),
            List.of(
                new LoadStage(1, 2, null),
                new LoadStage(1, 4, null)
            )
        );

        WorkloadDriver driver = new WorkloadDriver(config, HttpClient.newHttpClient());
        LoadTestResult result = driver.runLoad();

        assertTrue(result.totalWorkflows() > 0, "driver should produce at least one workflow per stage");
        assertEquals(0L, result.failures(), "all responses are 200 OK");
        assertEquals(result.totalWorkflows(), result.success());
        assertEquals(result.totalWorkflows(), calls.get(),
            "every workflow should hit the server exactly once");
        assertEquals(0L, result.paceMissCount(),
            "closed-model run should have no pace misses");
        // Wall-clock time covers both stages, not just one.
        assertTrue(result.durationMs() >= 1_500,
            "two 1s stages should take at least ~1.5s wall time");
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

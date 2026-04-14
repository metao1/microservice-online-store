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

class HttpLoadTestRunnerFlowTest {

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
    void shouldExecuteCheckoutWorkflowWithExtractionAndRetry() throws Exception {
        AtomicInteger paymentLookupAttempts = new AtomicInteger();
        AtomicInteger productReadAttempts = new AtomicInteger();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.setExecutor(Executors.newCachedThreadPool());
        server.createContext("/cart", exchange -> respond(exchange, 201, "{\"ok\":true}"));
        server.createContext("/api/order", exchange -> respond(exchange, 201, "{\"value\":\"order-123\"}"));
        server.createContext("/payments/order/order-123", exchange -> {
            if (paymentLookupAttempts.incrementAndGet() == 1) {
                respond(exchange, 404, "{\"error\":\"missing\"}");
                return;
            }
            respond(exchange, 200, "{\"paymentId\":\"pay-456\"}");
        });
        server.createContext("/payments/pay-456/process", exchange -> respond(exchange, 200, "{\"status\":\"SUCCESSFUL\"}"));
        server.createContext("/products/0594511488", exchange -> {
            if (productReadAttempts.incrementAndGet() == 1) {
                respond(exchange, 200, "{\"sku\":\"0594511488\",\"volume\":10}");
                return;
            }
            respond(exchange, 200, "{\"sku\":\"0594511488\",\"volume\":9}");
        });
        server.start();

        int port = server.getAddress().getPort();
        LoadTestConfig config = new LoadTestConfig(
            "checkout-flow",
            new HttpRequestSpec("POST", "http://localhost:" + port + "/cart", "", "none", Map.of()),
            List.of(
                new ScenarioStep(
                    "read-product-before",
                    new HttpRequestSpec(
                        "GET",
                        "http://localhost:" + port + "/products/${sku}",
                        "",
                        "none",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of("beforeVolume", "$.volume"),
                    List.of(),
                    200,
                    1,
                    0L
                ),
                new ScenarioStep(
                    "add-cart",
                    new HttpRequestSpec(
                        "POST",
                        "http://localhost:" + port + "/cart",
                        "{\"user_id\":\"${userId}\",\"items\":[{\"sku\":\"${sku}\"}]}",
                        "inline",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of(),
                    List.of(),
                    201,
                    1,
                    0L
                ),
                new ScenarioStep(
                    "create-order",
                    new HttpRequestSpec(
                        "POST",
                        "http://localhost:" + port + "/api/order",
                        "{\"user_id\":\"${userId}\"}",
                        "inline",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of("orderId", "$.value"),
                    List.of(),
                    201,
                    1,
                    0L
                ),
                new ScenarioStep(
                    "lookup-payment",
                    new HttpRequestSpec(
                        "GET",
                        "http://localhost:" + port + "/payments/order/${orderId}",
                        "",
                        "none",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of("paymentId", "$.paymentId"),
                    List.of(),
                    200,
                    2,
                    10L
                ),
                new ScenarioStep(
                    "process-payment",
                    new HttpRequestSpec(
                        "POST",
                        "http://localhost:" + port + "/payments/${paymentId}/process",
                        "",
                        "none",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of(),
                    List.of(),
                    200,
                    1,
                    0L
                ),
                new ScenarioStep(
                    "verify-product",
                    new HttpRequestSpec(
                        "GET",
                        "http://localhost:" + port + "/products/${sku}",
                        "",
                        "none",
                        Map.of("Accept", "application/json")
                    ),
                    Map.of(),
                    List.of(new StepAssertion("$.volume", "lt", "${beforeVolume}")),
                    200,
                    3,
                    10L
                )
            ),
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
            Map.of(
                "sku", "0594511488",
                "userId", "loadtest-${vu}-${iteration}-${uuid}"
            )
        );

        HttpLoadTestRunner.WorkflowOutcome result = HttpLoadTestRunner.executeWorkflow(
            HttpClient.newHttpClient(),
            config,
            1,
            1
        );

        assertTrue(result.success());
        assertTrue(result.responseBytes() > 0);
        assertEquals(2, paymentLookupAttempts.get());
        assertTrue(productReadAttempts.get() >= 2);
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

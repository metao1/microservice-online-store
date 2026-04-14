package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoadTestConfigParserTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldParseDirectCliConfigWithHeadersAndThresholds() throws Exception {
        var parsed = LoadTestConfigParser.parse(new String[] {
            "--url", "http://localhost:8084/payments/status/PENDING?offset=0&limit=10",
            "--method", "get",
            "--header", "Accept: application/json",
            "--header", "X-Test: true",
            "--users", "25",
            "--duration-sec", "45",
            "--warmup-sec", "5",
            "--timeout-sec", "9",
            "--think-ms", "15",
            "--max-error-rate-pct", "1.5",
            "--min-throughput-rps", "10",
            "--max-p95-ms", "150",
            "--max-p99-ms", "250"
        });

        assertFalse(parsed.helpRequested());
        assertEquals("GET", parsed.config().request().method());
        assertEquals(2, parsed.config().request().headers().size());
        assertEquals(25, parsed.config().virtualUsers());
        assertEquals(1.5, parsed.config().thresholds().maxErrorRatePct());
        assertEquals(10.0, parsed.config().thresholds().minThroughputRps());
        assertEquals(150.0, parsed.config().thresholds().maxP95Ms());
        assertEquals(250.0, parsed.config().thresholds().maxP99Ms());
    }

    @Test
    void shouldLoadScenarioBodyFromRelativeFile() throws Exception {
        Path scenarioDir = Files.createDirectories(tempDir.resolve("scenarios"));
        Files.writeString(scenarioDir.resolve("payload.json"), "{\"hello\":\"world\"}");
        Files.writeString(scenarioDir.resolve("scenario.json"), """
            {
              "scenarios": [
                {
                  "name": "create-payment",
                  "request": {
                    "url": "http://localhost:8084/payments",
                    "method": "POST",
                    "bodyFile": "payload.json",
                    "headers": {
                      "Accept": "application/json"
                    }
                  },
                  "load": {
                    "users": 5,
                    "durationSec": 20,
                    "warmupSec": 2,
                    "timeoutSec": 4,
                    "thinkMs": 10
                  }
                }
              ]
            }
            """);

        var parsed = LoadTestConfigParser.parse(new String[] {
            "--scenario-file", scenarioDir.resolve("scenario.json").toString()
        });

        assertEquals("create-payment", parsed.config().label());
        assertTrue(parsed.config().request().hasBody());
        assertEquals("{\"hello\":\"world\"}", parsed.config().request().body());
        assertEquals("payload.json", parsed.config().request().bodySource());
    }

    @Test
    void shouldParseMultiStepScenarioWithVariablesAndExtraction() throws Exception {
        Path scenarioDir = Files.createDirectories(tempDir.resolve("multi"));
        Files.writeString(scenarioDir.resolve("add-cart.json"), """
            {"user_id":"${userId}","items":[{"sku":"${sku}","productTitle":"Book","quantity":1,"price":19.99,"currency":"EUR"}]}
            """);
        Files.writeString(scenarioDir.resolve("scenario.json"), """
            {
              "scenarios": [
                {
                  "name": "checkout-flow",
                  "variables": {
                    "sku": "0594511488",
                    "userId": "loadtest-${vu}-${iteration}-${uuid}"
                  },
                  "steps": [
                    {
                      "name": "add-cart-item",
                      "request": {
                        "url": "http://localhost:8086/cart",
                        "method": "POST",
                        "bodyFile": "add-cart.json"
                      }
                    },
                    {
                      "name": "create-order",
                      "request": {
                        "url": "http://localhost:8086/api/order",
                        "method": "POST",
                        "body": "{\\"user_id\\":\\"${userId}\\"}"
                      },
                      "extract": {
                        "orderId": "$.value"
                      }
                    }
                  ]
                }
              ]
            }
            """);

        var parsed = LoadTestConfigParser.parse(new String[] {
            "--scenario-file", scenarioDir.resolve("scenario.json").toString()
        });

        assertEquals("checkout-flow", parsed.config().label());
        assertEquals(2, parsed.config().steps().size());
        assertEquals("loadtest-${vu}-${iteration}-${uuid}", parsed.config().variables().get("userId"));
        assertEquals("add-cart-item", parsed.config().steps().getFirst().name());
        assertEquals("$.value", parsed.config().steps().get(1).extract().get("orderId"));
        assertTrue(parsed.config().steps().getFirst().request().hasBody());
    }

    @Test
    void shouldParseStepAssertions() throws Exception {
        Path scenarioFile = tempDir.resolve("assertions.json");
        Files.writeString(scenarioFile, """
            {
              "scenarios": [
                {
                  "name": "assert-volume",
                  "steps": [
                    {
                      "name": "verify",
                      "request": {
                        "url": "http://localhost:8083/products/0594511488",
                        "method": "GET"
                      },
                      "assertions": [
                        {
                          "path": "$.volume",
                          "operator": "lt",
                          "expected": "${beforeVolume}"
                        }
                      ]
                    }
                  ]
                }
              ]
            }
            """);

        var parsed = LoadTestConfigParser.parse(new String[] {
            "--scenario-file", scenarioFile.toString()
        });

        assertEquals(1, parsed.config().steps().size());
        assertEquals(1, parsed.config().steps().getFirst().assertions().size());
        assertEquals("$.volume", parsed.config().steps().getFirst().assertions().getFirst().path());
        assertEquals("lt", parsed.config().steps().getFirst().assertions().getFirst().operator());
    }
}

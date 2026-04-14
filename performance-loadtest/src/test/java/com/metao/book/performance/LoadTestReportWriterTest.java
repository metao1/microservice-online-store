package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LoadTestReportWriterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldWriteJsonAndTextReports() throws Exception {
        LoadTestConfig config = new LoadTestConfig(
            "inventory-category-page",
            new HttpRequestSpec("GET", "http://localhost:8083/products/category/books", "", "none", Map.of()),
            List.of(new ScenarioStep(
                "request",
                new HttpRequestSpec("GET", "http://localhost:8083/products/category/books", "", "none", Map.of()),
                Map.of(),
                List.of(),
                null,
                1,
                0L
            )),
            20,
            30,
            5,
            5,
            0,
            tempDir,
            new LoadTestThresholds(1.0, null, 200.0, 500.0),
            "test",
            Map.of()
        );
        LoadTestResult result = new LoadTestResult(
            Instant.parse("2026-04-12T10:00:00Z"),
            Instant.parse("2026-04-12T10:00:30Z"),
            30_000,
            1000,
            1000,
            0,
            100_000,
            33.3,
            8.0,
            25.0,
            90.0,
            120.0,
            200.0,
            0.0,
            Map.of()
        );

        var artifacts = LoadTestReportWriter.write(config, result, List.of());

        assertTrue(Files.exists(artifacts.jsonReport()));
        assertTrue(Files.exists(artifacts.textReport()));
        assertTrue(Files.readString(artifacts.jsonReport()).contains("\"label\" : \"inventory-category-page\""));
        assertTrue(Files.readString(artifacts.textReport()).contains("thresholds=passed"));
    }
}

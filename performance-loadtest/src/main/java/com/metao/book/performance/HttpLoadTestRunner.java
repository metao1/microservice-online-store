package com.metao.book.performance;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;

public final class HttpLoadTestRunner {

    private HttpLoadTestRunner() {
    }

    public static void main(String[] args) throws Exception {
        Config config = Config.parse(args);
        System.out.println("Starting load test");
        System.out.println("target=" + config.url);
        System.out.println("method=" + config.method);
        System.out.println("durationSec=" + config.durationSec + ", warmupSec=" + config.warmupSec
            + ", users=" + config.virtualUsers + ", thinkMs=" + config.thinkTimeMs);

        HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .version(HttpClient.Version.HTTP_1_1)
            .build();

        if (config.warmupSec > 0) {
            runWarmup(client, config);
        }

        Result result = runLoad(client, config);
        Path reportPath = writeReport(result, config.reportDir);
        printSummary(result, reportPath);
    }

    private static void runWarmup(HttpClient client, Config config) {
        Instant stopAt = Instant.now().plusSeconds(config.warmupSec);
        while (Instant.now().isBefore(stopAt)) {
            sendRequest(client, config, null, null);
            sleep(config.thinkTimeMs);
        }
    }

    private static Result runLoad(HttpClient client, Config config) throws Exception {
        ConcurrentLinkedQueue<Long> latenciesMicros = new ConcurrentLinkedQueue<>();
        ConcurrentHashMap<String, LongAdder> errors = new ConcurrentHashMap<>();
        LongAdder success = new LongAdder();
        LongAdder failures = new LongAdder();
        LongAdder bytes = new LongAdder();

        Instant start = Instant.now();
        Instant stopAt = start.plusSeconds(config.durationSec);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < config.virtualUsers; i++) {
                futures.add(executor.submit(() -> {
                    while (Instant.now().isBefore(stopAt)) {
                        sendRequest(client, config, new Metrics(success, failures, bytes, latenciesMicros), errors);
                        sleep(config.thinkTimeMs);
                    }
                }));
            }

            for (Future<?> future : futures) {
                future.get();
            }

            executor.shutdown();
            executor.awaitTermination(10, TimeUnit.SECONDS);
        }

        Instant end = Instant.now();
        return Result.from(start, end, latenciesMicros, success.sum(), failures.sum(), bytes.sum(), errors);
    }

    private static void sendRequest(
        HttpClient client,
        Config config,
        Metrics metrics,
        Map<String, LongAdder> errors
    ) {
        long startNanos = System.nanoTime();
        try {
            HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(config.url))
                .timeout(Duration.ofSeconds(config.requestTimeoutSec));

            if ("POST".equals(config.method) || "PUT".equals(config.method) || "PATCH".equals(config.method)) {
                requestBuilder.method(config.method, HttpRequest.BodyPublishers.ofString(config.body));
                requestBuilder.header("Content-Type", "application/json");
            } else {
                requestBuilder.method(config.method, HttpRequest.BodyPublishers.noBody());
            }

            HttpResponse<String> response = client.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
            long elapsedMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos);

            if (metrics != null) {
                metrics.latenciesMicros.add(elapsedMicros);
                metrics.bytes.add(response.body() == null ? 0 : response.body().length());
                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    metrics.success.increment();
                } else {
                    metrics.failures.increment();
                    incrementError(errors, "HTTP_" + response.statusCode());
                }
            }
        } catch (Exception e) {
            if (metrics != null) {
                long elapsedMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNanos);
                metrics.latenciesMicros.add(elapsedMicros);
                metrics.failures.increment();
                incrementError(errors, e.getClass().getSimpleName());
            }
        }
    }

    private static void incrementError(Map<String, LongAdder> errors, String key) {
        if (errors == null) {
            return;
        }
        errors.computeIfAbsent(key, ignored -> new LongAdder()).increment();
    }

    private static Path writeReport(Result result, Path reportDir) throws IOException {
        Files.createDirectories(reportDir);
        String timestamp = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss").format(java.time.LocalDateTime.now());
        Path reportPath = reportDir.resolve("load-test-" + timestamp + ".json");

        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"start\": \"").append(result.start).append("\",\n");
        json.append("  \"end\": \"").append(result.end).append("\",\n");
        json.append("  \"durationMs\": ").append(result.durationMs).append(",\n");
        json.append("  \"requests\": ").append(result.totalRequests).append(",\n");
        json.append("  \"success\": ").append(result.success).append(",\n");
        json.append("  \"failures\": ").append(result.failures).append(",\n");
        json.append("  \"throughputRps\": ").append(String.format("%.2f", result.throughputRps)).append(",\n");
        json.append("  \"latencyMs\": {\n");
        json.append("    \"min\": ").append(String.format("%.3f", result.minMs)).append(",\n");
        json.append("    \"p50\": ").append(String.format("%.3f", result.p50Ms)).append(",\n");
        json.append("    \"p95\": ").append(String.format("%.3f", result.p95Ms)).append(",\n");
        json.append("    \"p99\": ").append(String.format("%.3f", result.p99Ms)).append(",\n");
        json.append("    \"max\": ").append(String.format("%.3f", result.maxMs)).append("\n");
        json.append("  },\n");
        json.append("  \"responseBytes\": ").append(result.responseBytes).append(",\n");
        json.append("  \"errors\": {\n");

        int i = 0;
        for (Map.Entry<String, Long> entry : result.errors.entrySet()) {
            json.append("    \"").append(entry.getKey()).append("\": ").append(entry.getValue());
            if (++i < result.errors.size()) {
                json.append(",");
            }
            json.append("\n");
        }

        json.append("  }\n");
        json.append("}\n");

        Files.writeString(reportPath, json);
        return reportPath;
    }

    private static void printSummary(Result result, Path reportPath) {
        System.out.println("Load test finished");
        System.out.println("requests=" + result.totalRequests + ", success=" + result.success + ", failures=" + result.failures);
        System.out.println("throughput(rps)=" + String.format("%.2f", result.throughputRps));
        System.out.println("latency(ms): min=" + String.format("%.3f", result.minMs)
            + " p50=" + String.format("%.3f", result.p50Ms)
            + " p95=" + String.format("%.3f", result.p95Ms)
            + " p99=" + String.format("%.3f", result.p99Ms)
            + " max=" + String.format("%.3f", result.maxMs));
        System.out.println("report=" + reportPath.toAbsolutePath());
    }

    private static void sleep(long millis) {
        if (millis <= 0) {
            return;
        }
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private record Metrics(
        LongAdder success,
        LongAdder failures,
        LongAdder bytes,
        ConcurrentLinkedQueue<Long> latenciesMicros
    ) {
    }

    private static final class Result {
        private final Instant start;
        private final Instant end;
        private final long durationMs;
        private final long totalRequests;
        private final long success;
        private final long failures;
        private final long responseBytes;
        private final double throughputRps;
        private final double minMs;
        private final double p50Ms;
        private final double p95Ms;
        private final double p99Ms;
        private final double maxMs;
        private final Map<String, Long> errors;

        private Result(
            Instant start,
            Instant end,
            long durationMs,
            long totalRequests,
            long success,
            long failures,
            long responseBytes,
            double throughputRps,
            double minMs,
            double p50Ms,
            double p95Ms,
            double p99Ms,
            double maxMs,
            Map<String, Long> errors
        ) {
            this.start = start;
            this.end = end;
            this.durationMs = durationMs;
            this.totalRequests = totalRequests;
            this.success = success;
            this.failures = failures;
            this.responseBytes = responseBytes;
            this.throughputRps = throughputRps;
            this.minMs = minMs;
            this.p50Ms = p50Ms;
            this.p95Ms = p95Ms;
            this.p99Ms = p99Ms;
            this.maxMs = maxMs;
            this.errors = errors;
        }

        private static Result from(
            Instant start,
            Instant end,
            ConcurrentLinkedQueue<Long> latenciesMicros,
            long success,
            long failures,
            long responseBytes,
            ConcurrentHashMap<String, LongAdder> errors
        ) {
            long durationMs = Math.max(1, Duration.between(start, end).toMillis());
            long totalRequests = success + failures;
            double throughputRps = totalRequests * 1000.0 / durationMs;

            long[] sorted = latenciesMicros.stream().mapToLong(Long::longValue).sorted().toArray();
            double minMs = percentileMs(sorted, 0);
            double p50Ms = percentileMs(sorted, 50);
            double p95Ms = percentileMs(sorted, 95);
            double p99Ms = percentileMs(sorted, 99);
            double maxMs = percentileMs(sorted, 100);

            Map<String, Long> errorSnapshot = new ConcurrentHashMap<>();
            errors.forEach((k, v) -> errorSnapshot.put(k, v.sum()));

            return new Result(start, end, durationMs, totalRequests, success, failures, responseBytes,
                throughputRps, minMs, p50Ms, p95Ms, p99Ms, maxMs, errorSnapshot);
        }

        private static double percentileMs(long[] sortedMicros, int percentile) {
            if (sortedMicros.length == 0) {
                return 0.0;
            }
            if (percentile <= 0) {
                return sortedMicros[0] / 1000.0;
            }
            if (percentile >= 100) {
                return sortedMicros[sortedMicros.length - 1] / 1000.0;
            }
            int index = (int) Math.ceil((percentile / 100.0) * sortedMicros.length) - 1;
            index = Math.max(0, Math.min(index, sortedMicros.length - 1));
            return sortedMicros[index] / 1000.0;
        }
    }

    private static final class Config {
        private final String url;
        private final String method;
        private final String body;
        private final int virtualUsers;
        private final int durationSec;
        private final int warmupSec;
        private final int requestTimeoutSec;
        private final long thinkTimeMs;
        private final Path reportDir;

        private Config(
            String url,
            String method,
            String body,
            int virtualUsers,
            int durationSec,
            int warmupSec,
            int requestTimeoutSec,
            long thinkTimeMs,
            Path reportDir
        ) {
            this.url = url;
            this.method = method;
            this.body = body;
            this.virtualUsers = virtualUsers;
            this.durationSec = durationSec;
            this.warmupSec = warmupSec;
            this.requestTimeoutSec = requestTimeoutSec;
            this.thinkTimeMs = thinkTimeMs;
            this.reportDir = reportDir;
        }

        private static Config parse(String[] args) {
            Map<String, String> opts = parseOptions(args);
            String url = required(opts, "--url");
            String method = opts.getOrDefault("--method", "GET").toUpperCase();
            String body = opts.getOrDefault("--body", "{}");
            int users = parseInt(opts.getOrDefault("--users", "50"), "--users");
            int duration = parseInt(opts.getOrDefault("--duration-sec", "60"), "--duration-sec");
            int warmup = parseInt(opts.getOrDefault("--warmup-sec", "10"), "--warmup-sec");
            int timeout = parseInt(opts.getOrDefault("--timeout-sec", "5"), "--timeout-sec");
            long thinkMs = parseLong(opts.getOrDefault("--think-ms", "0"), "--think-ms");
            Path reportDir = Path.of(opts.getOrDefault("--report-dir", "performance-loadtest/reports"));

            if (users < 1 || duration < 1 || warmup < 0 || timeout < 1 || thinkMs < 0) {
                throw new IllegalArgumentException("Invalid numeric argument values");
            }

            return new Config(url, method, body, users, duration, warmup, timeout, thinkMs, reportDir);
        }

        private static Map<String, String> parseOptions(String[] args) {
            if (Arrays.stream(args).anyMatch("--help"::equals)) {
                printHelp();
                System.exit(0);
            }

            Map<String, String> options = new ConcurrentHashMap<>();
            for (int i = 0; i < args.length; i++) {
                String key = args[i];
                if (!key.startsWith("--")) {
                    throw new IllegalArgumentException("Unexpected argument: " + key);
                }
                if (i + 1 >= args.length) {
                    throw new IllegalArgumentException("Missing value for: " + key);
                }
                options.put(key, args[++i]);
            }
            return options;
        }

        private static String required(Map<String, String> options, String key) {
            String value = options.get(key);
            if (value == null || value.isBlank()) {
                throw new IllegalArgumentException("Missing required option: " + key);
            }
            return value;
        }

        private static int parseInt(String raw, String name) {
            try {
                return Integer.parseInt(raw);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid integer for " + name + ": " + raw, e);
            }
        }

        private static long parseLong(String raw, String name) {
            try {
                return Long.parseLong(raw);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid long for " + name + ": " + raw, e);
            }
        }

        private static void printHelp() {
            List<String> lines = Collections.unmodifiableList(List.of(
                "Usage:",
                "  ./gradlew :performance-loadtest:run --args='--url http://localhost:8084/payments/status/PENDING?offset=0&limit=10 --users 100 --duration-sec 120'",
                "",
                "Options:",
                "  --url <URL>                 Required. Full target URL",
                "  --method <GET|POST|PUT|PATCH|DELETE>  Optional. Default GET",
                "  --body <json>               Optional. For POST/PUT/PATCH",
                "  --users <int>               Optional. Default 50",
                "  --duration-sec <int>        Optional. Default 60",
                "  --warmup-sec <int>          Optional. Default 10",
                "  --timeout-sec <int>         Optional. Default 5",
                "  --think-ms <long>           Optional. Default 0",
                "  --report-dir <path>         Optional. Default performance-loadtest/reports",
                "  --help"
            ));
            lines.forEach(System.out::println);
        }
    }
}

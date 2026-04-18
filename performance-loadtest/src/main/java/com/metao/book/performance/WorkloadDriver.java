package com.metao.book.performance;

import java.net.http.HttpClient;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

/**
 * Runs the warmup pass and the measured load pass. One instance per test run.
 * <p>
 * Concurrency model: each virtual user is its own virtual thread. When
 * {@link LoadTestConfig#targetRps()} is set, the driver paces workflow starts
 * to match the target rate (open model with HdrHistogram coordinated-omission
 * correction). Otherwise every VU self-paces with {@code thinkTimeMs} between
 * iterations (closed model).
 * <p>
 * Stop conditions use {@link System#nanoTime()} rather than wall-clock time so
 * NTP adjustments can't extend or truncate the measured window.
 */
final class WorkloadDriver {

    private final LoadTestConfig config;
    private final HttpClient client;
    private final ScenarioExecutor executor;

    WorkloadDriver(LoadTestConfig config, HttpClient client) {
        this.config = config;
        this.client = client;
        this.executor = new ScenarioExecutor(client, config);
    }

    /**
     * Runs {@code warmupSec} seconds of load using the same virtual-user count
     * as the real run. This is essential so JIT, connection pools, DB caches,
     * and GC generations are primed against the correct load shape; a single-
     * threaded warmup would silently hand the first measured seconds of the
     * main run a warmup cost.
     */
    void runWarmup() throws Exception {
        if (config.warmupSec() <= 0) {
            return;
        }
        long stopAtNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(config.warmupSec());
        AtomicLong workflowCounter = new AtomicLong();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < config.virtualUsers(); index += 1) {
                final int virtualUser = index + 1;
                futures.add(pool.submit(() -> {
                    while (System.nanoTime() < stopAtNanos) {
                        long iteration = workflowCounter.incrementAndGet();
                        try {
                            executor.executeWorkflow(virtualUser, iteration);
                        } catch (RuntimeException ignored) {
                            // Warmup failures are expected against a cold system; don't fail the run.
                        }
                        sleep(config.thinkTimeMs());
                    }
                }));
            }
            awaitAll(futures);
        }
    }

    LoadTestResult runLoad() throws Exception {
        LatencyHistogram latenciesMicros = new LatencyHistogram();
        StepLatencyCollector stepLatencies = new StepLatencyCollector(config.steps());
        ConcurrentHashMap<String, LongAdder> errors = new ConcurrentHashMap<>();
        LongAdder success = new LongAdder();
        LongAdder failures = new LongAdder();
        LongAdder bytes = new LongAdder();

        // Expected inter-arrival time used for HdrHistogram's coordinated-
        // omission correction. Only meaningful under open-model pacing; under
        // closed-model (no targetRps) each VU self-paces so there is no
        // "expected start time" to compare against.
        Long expectedIntervalMicros = (config.targetRps() != null && config.targetRps() > 0.0)
            ? (long) Math.max(1.0d, 1_000_000d / config.targetRps())
            : null;

        Instant start = Instant.now();
        long startNanos = System.nanoTime();
        long stopAtNanos = startNanos + TimeUnit.SECONDS.toNanos(config.durationSec());
        AtomicLong workflowCounter = new AtomicLong();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < config.virtualUsers(); index += 1) {
                final int virtualUser = index + 1;
                futures.add(pool.submit(() -> {
                    while (System.nanoTime() < stopAtNanos) {
                        long workflowId = workflowCounter.incrementAndGet();
                        paceWorkflowStart(config.targetRps(), startNanos, workflowId, stopAtNanos);
                        if (System.nanoTime() >= stopAtNanos) {
                            break;
                        }
                        long workflowStart = System.nanoTime();
                        ScenarioExecutor.WorkflowOutcome outcome =
                            executor.executeWorkflow(virtualUser, workflowId, stepLatencies);
                        long elapsedMicros = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - workflowStart);

                        bytes.add(outcome.responseBytes());
                        if (outcome.success()) {
                            // Success-only latency percentiles follow the standard SLO
                            // convention: a 5s timeout failure shouldn't masquerade as
                            // a 5s "successful" tail.
                            if (expectedIntervalMicros != null) {
                                latenciesMicros.recordWithExpectedInterval(elapsedMicros, expectedIntervalMicros);
                            } else {
                                latenciesMicros.record(elapsedMicros);
                            }
                            success.increment();
                        } else {
                            failures.increment();
                            incrementError(errors, outcome.errorKey());
                        }
                        sleep(config.thinkTimeMs());
                    }
                }));
            }
            awaitAll(futures);
            // Try-with-resources handles shutdown + awaitTermination; no manual call needed.
        }

        return LoadTestResult.from(
            start,
            Instant.now(),
            latenciesMicros.snapshot(),
            success.sum(),
            failures.sum(),
            bytes.sum(),
            stepLatencies.snapshot(),
            errors
        );
    }

    HttpClient client() {
        return client;
    }

    /**
     * Parks the current VU until its paced workflow start time, but never past
     * the run deadline. Without the deadline cap the last VU to grab a counter
     * slot could sit idle for seconds after the test window has closed.
     */
    private static void paceWorkflowStart(Double targetRps, long startNanos, long workflowNumber, long stopAtNanos) {
        if (targetRps == null || targetRps <= 0.0 || workflowNumber < 1) {
            return;
        }
        long nanosPerWorkflow = Math.max(1L, (long) (1_000_000_000d / targetRps));
        long targetStartNanos = startNanos + ((workflowNumber - 1) * nanosPerWorkflow);
        long deadlineNanos = Math.min(targetStartNanos, stopAtNanos);
        long delayNanos = deadlineNanos - System.nanoTime();
        if (delayNanos > 0L) {
            LockSupport.parkNanos(delayNanos);
        }
    }

    private static void awaitAll(List<Future<?>> futures) throws Exception {
        for (Future<?> future : futures) {
            future.get();
        }
    }

    private static void incrementError(ConcurrentHashMap<String, LongAdder> errors, String key) {
        errors.computeIfAbsent(key, ignored -> new LongAdder()).increment();
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
}

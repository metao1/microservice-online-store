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
 * Concurrency model: each virtual user is its own virtual thread. When a stage
 * has a {@code targetRps}, the driver paces workflow starts to match the
 * target rate (open model with HdrHistogram coordinated-omission correction).
 * Otherwise every VU self-paces with {@code thinkTimeMs} between iterations
 * (closed model).
 * <p>
 * Stop conditions use {@link System#nanoTime()} rather than wall-clock time so
 * NTP adjustments can't extend or truncate the measured window. A stage that
 * cannot sustain its target rate increments {@code paceMissCount} so the
 * report can distinguish "slow service" from "slow generator".
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
     * Runs {@code warmupSec} seconds of load using the peak virtual-user count
     * across all stages. Priming JIT / connection pools / DB caches / GC
     * generations against a smaller VU count than the real peak would hand the
     * first seconds of the measured window a warmup cost.
     */
    void runWarmup() throws Exception {
        if (config.warmupSec() <= 0) {
            return;
        }
        int warmupUsers = config.peakVirtualUsers();
        long stopAtNanos = System.nanoTime() + TimeUnit.SECONDS.toNanos(config.warmupSec());
        AtomicLong workflowCounter = new AtomicLong();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < warmupUsers; index += 1) {
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
        // Shared accumulators aggregate across all stages into one report.
        LatencyHistogram latenciesMicros = new LatencyHistogram();
        StepLatencyCollector stepLatencies = new StepLatencyCollector(config.steps());
        ConcurrentHashMap<String, LongAdder> errors = new ConcurrentHashMap<>();
        LongAdder success = new LongAdder();
        LongAdder failures = new LongAdder();
        LongAdder bytes = new LongAdder();
        LongAdder paceMisses = new LongAdder();

        Instant overallStart = Instant.now();
        // Run each stage sequentially. Stage-local pacing means each stage
        // gets its own fresh rate ramp; otherwise a fast "cruise" stage
        // following a slow "ramp" stage would inherit the ramp's workflow
        // counter and start late across all its VUs.
        for (LoadStage stage : config.stages()) {
            runStage(stage, latenciesMicros, stepLatencies, errors, success, failures, bytes, paceMisses);
        }

        return LoadTestResult.from(
            overallStart,
            Instant.now(),
            latenciesMicros.snapshot(),
            success.sum(),
            failures.sum(),
            bytes.sum(),
            paceMisses.sum(),
            stepLatencies.snapshot(),
            errors
        );
    }

    private void runStage(
        LoadStage stage,
        LatencyHistogram latenciesMicros,
        StepLatencyCollector stepLatencies,
        ConcurrentHashMap<String, LongAdder> errors,
        LongAdder success,
        LongAdder failures,
        LongAdder bytes,
        LongAdder paceMisses
    ) throws Exception {
        // Expected inter-arrival time used for HdrHistogram's coordinated-
        // omission correction. Only meaningful under open-model pacing; under
        // closed-model (no targetRps) each VU self-paces so there is no
        // "expected start time" to compare against.
        Long expectedIntervalMicros = (stage.targetRps() != null && stage.targetRps() > 0.0)
            ? (long) Math.max(1.0d, 1_000_000d / stage.targetRps())
            : null;

        long stageStartNanos = System.nanoTime();
        long stageStopAtNanos = stageStartNanos + TimeUnit.SECONDS.toNanos(stage.durationSec());
        // Stage-local counter so pacing math within a stage is relative to its
        // own start time, not the overall run's start time.
        AtomicLong stageWorkflowCounter = new AtomicLong();

        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<Future<?>> futures = new ArrayList<>();
            for (int index = 0; index < stage.users(); index += 1) {
                final int virtualUser = index + 1;
                futures.add(pool.submit(() -> {
                    while (System.nanoTime() < stageStopAtNanos) {
                        long workflowId = stageWorkflowCounter.incrementAndGet();
                        paceWorkflowStart(
                            stage.targetRps(),
                            stageStartNanos,
                            workflowId,
                            stageStopAtNanos,
                            paceMisses
                        );
                        if (System.nanoTime() >= stageStopAtNanos) {
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
        }
    }

    HttpClient client() {
        return client;
    }

    /**
     * Parks the current VU until its paced workflow start time, but never past
     * the stage deadline. When the target time has already elapsed we increment
     * {@code paceMisses} — a standing count of lateness is a primary signal
     * that either the generator or the target couldn't sustain the requested
     * rate.
     */
    private static void paceWorkflowStart(
        Double targetRps,
        long stageStartNanos,
        long workflowNumber,
        long stageStopAtNanos,
        LongAdder paceMisses
    ) {
        if (targetRps == null || targetRps <= 0.0 || workflowNumber < 1) {
            return;
        }
        long nanosPerWorkflow = Math.max(1L, (long) (1_000_000_000d / targetRps));
        long targetStartNanos = stageStartNanos + ((workflowNumber - 1) * nanosPerWorkflow);
        long deadlineNanos = Math.min(targetStartNanos, stageStopAtNanos);
        long delayNanos = deadlineNanos - System.nanoTime();
        if (delayNanos > 0L) {
            LockSupport.parkNanos(delayNanos);
        } else if (targetStartNanos < System.nanoTime()) {
            // We're already past the target start time — this VU missed its slot.
            paceMisses.increment();
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

package com.metao.book.performance;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAccumulator;
import java.util.concurrent.atomic.LongAdder;
import org.HdrHistogram.Histogram;
import org.HdrHistogram.Recorder;

final class LatencyHistogram {

    private static final long HIGHEST_TRACKABLE_LATENCY_MICROS = TimeUnit.MINUTES.toMicros(5);
    private static final int SIGNIFICANT_DIGITS = 3;

    private final Recorder recorder = new Recorder(1L, HIGHEST_TRACKABLE_LATENCY_MICROS + 1L, SIGNIFICANT_DIGITS);
    private final LongAdder sampleCount = new LongAdder();
    private final LongAccumulator minMicros = new LongAccumulator(Long::min, Long.MAX_VALUE);
    private final LongAccumulator maxMicros = new LongAccumulator(Long::max, Long.MIN_VALUE);

    void record(long latencyMicros) {
        long normalized = Math.max(0L, latencyMicros);
        long clamped = Math.min(normalized, HIGHEST_TRACKABLE_LATENCY_MICROS);
        recorder.recordValue(clamped + 1L);
        sampleCount.increment();
        minMicros.accumulate(clamped);
        maxMicros.accumulate(clamped);
    }

    /**
     * Records a latency sample with coordinated-omission correction.
     * <p>
     * When the scheduler attempts to start workflows at a fixed rate and the
     * system stalls, many workflows that "should have started" during the stall
     * are never sampled, making p95/p99 look artificially good. HdrHistogram
     * corrects this by back-filling synthetic samples in
     * {@code [expectedInterval, 2*expectedInterval, ...]} steps whenever an
     * observed latency exceeds the expected interval. The actual recorded
     * sample still drives {@link #minMicros}/{@link #maxMicros}; synthetic
     * samples only inflate the histogram distribution so tail percentiles
     * reflect the stall.
     *
     * @param latencyMicros         observed end-to-end latency in microseconds
     * @param expectedIntervalMicros target inter-arrival time in microseconds
     *                              (e.g. {@code 1_000_000 / targetRps})
     */
    void recordWithExpectedInterval(long latencyMicros, long expectedIntervalMicros) {
        long normalized = Math.max(0L, latencyMicros);
        long clamped = Math.min(normalized, HIGHEST_TRACKABLE_LATENCY_MICROS);
        long expected = Math.max(1L, expectedIntervalMicros);
        recorder.recordValueWithExpectedInterval(clamped + 1L, expected);
        sampleCount.increment();
        minMicros.accumulate(clamped);
        maxMicros.accumulate(clamped);
    }

    Snapshot snapshot() {
        long count = sampleCount.sum();
        return new Snapshot(
            recorder.getIntervalHistogram(),
            count,
            count == 0 ? 0L : minMicros.get(),
            count == 0 ? 0L : maxMicros.get()
        );
    }

    static final class Snapshot {

        private final Histogram histogram;
        private final long sampleCount;
        private final long minMicros;
        private final long maxMicros;

        private Snapshot(Histogram histogram, long sampleCount, long minMicros, long maxMicros) {
            this.histogram = histogram;
            this.sampleCount = sampleCount;
            this.minMicros = minMicros;
            this.maxMicros = maxMicros;
        }

        long sampleCount() {
            return sampleCount;
        }

        long minMicros() {
            return minMicros;
        }

        long maxMicros() {
            return maxMicros;
        }

        double percentileMs(double percentile) {
            if (sampleCount() == 0) {
                return 0.0;
            }
            if (percentile <= 0.0) {
                return microsToMs(minMicros);
            }
            if (percentile >= 100.0) {
                return microsToMs(maxMicros);
            }
            long value = histogram.getValueAtPercentile(percentile);
            return microsToMs(normalizeMicros(value));
        }

        private static long normalizeMicros(long hdrValue) {
            return Math.max(0L, hdrValue - 1L);
        }
    }

    private static double microsToMs(long micros) {
        return micros / 1000.0;
    }
}

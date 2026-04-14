package com.metao.book.performance;

record StepLatencyStats(
    long samples,
    double minMs,
    double p50Ms,
    double p95Ms,
    double p99Ms,
    double maxMs
) {
}


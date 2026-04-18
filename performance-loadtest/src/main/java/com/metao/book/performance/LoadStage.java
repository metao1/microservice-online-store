package com.metao.book.performance;

/**
 * One step of a ramped load profile. The load driver executes stages
 * sequentially, spinning up exactly {@link #users()} virtual users for
 * {@link #durationSec()} seconds with optional {@link #targetRps()} pacing.
 * <p>
 * This is the "step ramp" model (k6-style without smooth interpolation): fixed
 * user count per stage. It covers the common shapes — warm → cruise → spike →
 * cool-down — without the lifecycle complexity of continuous ramp interpolation
 * (dynamically adding/removing VUs mid-run).
 * <p>
 * If a scenario declares no stages, {@link LoadTestConfig} synthesizes a
 * single stage from the top-level {@code virtualUsers} / {@code durationSec}
 * / {@code targetRps} fields so the driver can always iterate stages
 * unconditionally.
 */
record LoadStage(int durationSec, int users, Double targetRps) {

    LoadStage {
        if (durationSec < 1) {
            throw new IllegalArgumentException("stage durationSec must be >= 1");
        }
        if (users < 1) {
            throw new IllegalArgumentException("stage users must be >= 1");
        }
        if (targetRps != null && targetRps <= 0.0) {
            throw new IllegalArgumentException("stage targetRps must be > 0 when provided");
        }
    }
}

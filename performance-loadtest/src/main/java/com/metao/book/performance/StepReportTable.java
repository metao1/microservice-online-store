package com.metao.book.performance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Renders the per-step endpoint breakdown as a single ASCII table for human
 * readers (CLI + text report). The same data is available structured in the
 * JSON report via {@code result.stepLatencyMs[stepName]}, so downstream tools
 * should prefer that; this view exists purely to make the CLI readable.
 * <p>
 * Columns:
 * <ul>
 *     <li>{@code #} — 1-based index preserving scenario declaration order</li>
 *     <li>{@code Step} / {@code Method} / {@code Endpoint} — request identity</li>
 *     <li>{@code Count} / {@code OK} / {@code Fail} / {@code Retry} — execution counters</li>
 *     <li>{@code p50(ms)}, {@code p95(ms)}, {@code p99(ms)} — success-only latency</li>
 *     <li>{@code Status} — compact "code:count" distribution of terminal status codes,
 *         sorted by count descending so the dominant code is visible first</li>
 * </ul>
 */
final class StepReportTable {

    // Truncation ceiling for the endpoint column. A long fully-qualified URL
    // would blow the table width out; we keep the last N characters because
    // the path suffix is what changes between steps.
    private static final int MAX_ENDPOINT_LENGTH = 60;

    private StepReportTable() {
    }

    static String render(LoadTestConfig config, LoadTestResult result) {
        Map<String, StepLatencyStats> stats = result.stepLatencyMs();
        if (stats.isEmpty()) {
            return "";
        }

        // Method + URL come from the scenario config (template form, before
        // per-request rendering). Look them up by step name so steps that
        // appear in results but not in config still render with blanks.
        Map<String, ScenarioStep> byName = new LinkedHashMap<>();
        for (ScenarioStep step : config.steps()) {
            byName.put(step.name(), step);
        }

        List<String> headers = List.of(
            "#", "Step", "Method", "Endpoint",
            "Count", "OK", "Fail", "Retry",
            "p50(ms)", "p95(ms)", "p99(ms)",
            "Status"
        );
        List<AsciiTable.Alignment> alignments = List.of(
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.LEFT,
            AsciiTable.Alignment.LEFT,
            AsciiTable.Alignment.LEFT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.RIGHT,
            AsciiTable.Alignment.LEFT
        );

        List<List<String>> rows = new ArrayList<>(stats.size());
        int index = 0;
        for (Map.Entry<String, StepLatencyStats> entry : stats.entrySet()) {
            index += 1;
            String stepName = entry.getKey();
            StepLatencyStats step = entry.getValue();
            ScenarioStep scenarioStep = byName.get(stepName);
            String method = scenarioStep == null ? "-" : scenarioStep.request().method();
            String endpoint = scenarioStep == null ? "-" : shortenEndpoint(scenarioStep.request().url());

            rows.add(List.of(
                Integer.toString(index),
                stepName,
                method,
                endpoint,
                Long.toString(step.samples()),
                Long.toString(step.successes()),
                Long.toString(step.failures()),
                Long.toString(step.retries()),
                String.format("%.1f", step.p50Ms()),
                String.format("%.1f", step.p95Ms()),
                String.format("%.1f", step.p99Ms()),
                formatStatusCodes(step.statusCodeCounts())
            ));
        }

        return AsciiTable.render(headers, alignments, rows);
    }

    /**
     * Strips protocol and host so long fully-qualified URLs collapse to the
     * endpoint path that actually differentiates steps. Falls back to the
     * original string when no scheme is present.
     */
    static String shortenEndpoint(String url) {
        if (url == null || url.isEmpty()) {
            return "-";
        }
        String trimmed = url;
        int schemeEnd = trimmed.indexOf("://");
        if (schemeEnd >= 0) {
            int pathStart = trimmed.indexOf('/', schemeEnd + 3);
            trimmed = pathStart < 0 ? "/" : trimmed.substring(pathStart);
        }
        if (trimmed.length() <= MAX_ENDPOINT_LENGTH) {
            return trimmed;
        }
        // Preserve both ends because the leading segment typically identifies
        // the service ("/payments/...") and the trailing segment the resource
        // ("…/status"). Middle ellipsis keeps both visible.
        int keep = (MAX_ENDPOINT_LENGTH - 3) / 2;
        return trimmed.substring(0, keep) + "..." + trimmed.substring(trimmed.length() - keep);
    }

    /**
     * Formats the status-code distribution as a compact "code:count" string
     * sorted by count descending. The sentinel {@code 0} code (no response)
     * renders as {@code ERR:N} so operators don't confuse it with a real code.
     */
    static String formatStatusCodes(Map<Integer, Long> counts) {
        if (counts == null || counts.isEmpty()) {
            return "-";
        }
        List<Map.Entry<Integer, Long>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort((left, right) -> {
            int byCount = Long.compare(right.getValue(), left.getValue());
            if (byCount != 0) {
                return byCount;
            }
            return Integer.compare(left.getKey(), right.getKey());
        });
        StringBuilder out = new StringBuilder();
        for (int index = 0; index < sorted.size(); index += 1) {
            if (index > 0) {
                out.append(' ');
            }
            Map.Entry<Integer, Long> entry = sorted.get(index);
            int code = entry.getKey();
            out.append(code == StepLatencyCollector.NO_RESPONSE_STATUS ? "ERR" : Integer.toString(code));
            out.append(':').append(entry.getValue());
        }
        return out.toString();
    }
}

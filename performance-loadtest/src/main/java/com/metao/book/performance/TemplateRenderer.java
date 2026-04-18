package com.metao.book.performance;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves {@code ${name}} placeholders in scenario strings against a runtime
 * context map, including recursive variable resolution where one scenario
 * variable references another.
 * <p>
 * Extracted from {@code HttpLoadTestRunner} so template behavior can be
 * unit-tested in isolation and the runner stays small.
 */
final class TemplateRenderer {

    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    private TemplateRenderer() {
    }

    /**
     * Substitutes every {@code ${key}} placeholder with the corresponding value
     * from {@code context}. Throws {@link MissingTemplateValueException} when a
     * referenced key is absent so callers like scenario-variable resolution can
     * distinguish "not ready yet, try the next pass" from genuine errors.
     */
    static String resolve(String template, Map<String, String> context) {
        if (template == null || template.isBlank()) {
            return template == null ? "" : template;
        }
        // Hot-path short-circuit: every header key/value, url, method, and body
        // passes through here on every attempt of every step of every workflow.
        // Most static scenario strings contain no placeholder at all; skipping
        // the Matcher allocation + StringBuilder copy on those strings is
        // measurable under 10k+ RPS load.
        if (template.indexOf('$') < 0) {
            return template;
        }
        Matcher matcher = TEMPLATE_PATTERN.matcher(template);
        StringBuilder resolved = new StringBuilder();
        int cursor = 0;
        while (matcher.find()) {
            resolved.append(template, cursor, matcher.start());
            String key = matcher.group(1);
            String value = context.get(key);
            if (value == null) {
                throw new MissingTemplateValueException(key);
            }
            resolved.append(value);
            cursor = matcher.end();
        }
        resolved.append(template.substring(cursor));
        return resolved.toString();
    }

    /**
     * Produces a new {@link HttpRequestSpec} with every method/url/body/header
     * field template-resolved against {@code context}. The original spec is not
     * mutated so a single scenario step can be safely shared across concurrent
     * virtual users.
     */
    static HttpRequestSpec renderRequest(HttpRequestSpec request, Map<String, String> context) {
        Map<String, String> headers = new LinkedHashMap<>();
        request.headers().forEach((key, value) -> headers.put(resolve(key, context), resolve(value, context)));
        return new HttpRequestSpec(
            request.method(),
            resolve(request.url(), context),
            resolve(request.body(), context),
            request.bodySource(),
            headers
        );
    }

    /**
     * Resolves scenario-level variables that may reference each other in any
     * order by iterating fixed-point style: any variable that still depends on
     * an unresolved one is deferred to the next pass. Terminates as soon as
     * every variable is resolved, or throws when a pass makes no progress
     * (cycle / genuinely missing reference).
     */
    static Map<String, String> resolveVariables(Map<String, String> variables, Map<String, String> baseContext) {
        if (variables.isEmpty()) {
            return Map.of();
        }
        Map<String, String> resolved = new LinkedHashMap<>();
        Map<String, String> workingContext = new LinkedHashMap<>(baseContext);
        List<String> pending = new ArrayList<>(variables.keySet());

        for (int pass = 0; pass < variables.size(); pass += 1) {
            boolean progressed = false;
            List<String> stillPending = new ArrayList<>();

            for (String key : pending) {
                try {
                    String value = resolve(variables.get(key), workingContext);
                    resolved.put(key, value);
                    workingContext.put(key, value);
                    progressed = true;
                } catch (MissingTemplateValueException exception) {
                    stillPending.add(key);
                }
            }

            if (stillPending.isEmpty()) {
                return resolved;
            }
            if (!progressed) {
                throw new IllegalArgumentException("Unable to resolve scenario variables: " + stillPending);
            }
            pending = stillPending;
        }

        throw new IllegalArgumentException("Unable to resolve scenario variables: " + pending);
    }

    /**
     * Signals that a {@code ${key}} placeholder could not be resolved. Package-
     * private because scenario-variable resolution treats this as a retryable
     * "not yet" signal, while assertion/request rendering treats it as fatal.
     */
    static final class MissingTemplateValueException extends RuntimeException {
        MissingTemplateValueException(String key) {
            super("Missing template value: " + key);
        }
    }
}

package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Evaluates the {@code assertions} block of a {@link ScenarioStep} against a
 * parsed response body. Operators are:
 * <ul>
 *     <li>{@code eq}, {@code ne}, {@code lt}, {@code lte}, {@code gt}, {@code gte}
 *         with numeric coercion when both sides are parseable numbers;</li>
 *     <li>{@code eq}, {@code ne}, {@code contains} otherwise (string semantics).</li>
 * </ul>
 * Assertion mismatches throw {@link AssertionFailedException} — a dedicated
 * type so the caller can fail-fast on logical assertion errors while still
 * retrying on network/HTTP failures. Scenarios can opt into retrying assertions
 * (useful for eventual-consistency checks) by setting
 * {@code retryOnAssertion: true} on the step.
 */
final class AssertionEvaluator {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private AssertionEvaluator() {
    }

    static void evaluate(ScenarioStep step, String body, Map<String, String> context) throws Exception {
        if (step.assertions().isEmpty()) {
            return;
        }
        JsonNode root = OBJECT_MAPPER.readTree(body);
        for (StepAssertion assertion : step.assertions()) {
            JsonNode actualNode = JsonPathResolver.resolve(root, assertion.path());
            if (!matches(actualNode, assertion, context)) {
                String actualValue = actualNode == null || actualNode.isNull() ? "null"
                    : actualNode.isTextual() ? actualNode.textValue() : actualNode.toString();
                throw new AssertionFailedException(
                    "Assertion failed for %s: expected %s %s but was %s"
                        .formatted(
                            assertion.path(),
                            assertion.operator(),
                            TemplateRenderer.resolve(assertion.expected(), context),
                            actualValue
                        )
                );
            }
        }
    }

    /**
     * Thrown when an {@code assertions} entry does not match the response. By
     * default callers fail-fast on this exception; scenarios that assert on
     * eventually-consistent state should set {@code retryOnAssertion: true} on
     * the step so the driver retries up to {@code maxAttempts}.
     */
    static final class AssertionFailedException extends RuntimeException {
        AssertionFailedException(String message) {
            super(message);
        }
    }

    private static boolean matches(JsonNode actualNode, StepAssertion assertion, Map<String, String> context) {
        String expectedValue = TemplateRenderer.resolve(assertion.expected(), context);
        String operator = assertion.operator().trim().toLowerCase();

        if (actualNode == null || actualNode.isMissingNode() || actualNode.isNull()) {
            return "eq".equals(operator) && "null".equalsIgnoreCase(expectedValue);
        }

        if (isNumeric(actualNode, expectedValue)) {
            double actual = actualNode.asDouble();
            double expected = Double.parseDouble(expectedValue);
            return switch (operator) {
                case "eq" -> Double.compare(actual, expected) == 0;
                case "ne" -> Double.compare(actual, expected) != 0;
                case "lt" -> actual < expected;
                case "lte" -> actual <= expected;
                case "gt" -> actual > expected;
                case "gte" -> actual >= expected;
                default -> throw new IllegalArgumentException(
                    "Unsupported numeric assertion operator: " + assertion.operator());
            };
        }

        String actual = actualNode.isTextual() ? actualNode.textValue() : actualNode.toString();
        return switch (operator) {
            case "eq" -> actual.equals(expectedValue);
            case "ne" -> !actual.equals(expectedValue);
            case "contains" -> actual.contains(expectedValue);
            default -> throw new IllegalArgumentException(
                "Unsupported assertion operator: " + assertion.operator());
        };
    }

    private static boolean isNumeric(JsonNode actualNode, String expectedValue) {
        if (!actualNode.isNumber()) {
            return false;
        }
        try {
            Double.parseDouble(expectedValue);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }
}

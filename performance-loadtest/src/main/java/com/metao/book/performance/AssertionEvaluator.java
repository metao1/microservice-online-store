package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

/**
 * Evaluates the {@code assertions} block of a {@link ScenarioStep} against a
 * parsed response body.
 *
 * <p>Operator semantics live on {@link AssertionOperator} (Strategy pattern
 * via enum-per-constant), so this class is deliberately small: walk the
 * assertions, delegate the match, raise a dedicated exception on mismatch.
 *
 * <p>Assertion mismatches throw {@link AssertionFailedException} — a dedicated
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
            String expected = TemplateRenderer.resolve(assertion.expected(), context);
            AssertionOperator operator = assertion.operatorType();
            if (!operator.matches(actualNode, expected)) {
                throw new AssertionFailedException(
                    "Assertion failed for %s: expected %s %s but was %s"
                        .formatted(
                            assertion.path(),
                            operator.symbol(),
                            expected,
                            describe(actualNode)
                        )
                );
            }
        }
    }

    private static String describe(JsonNode actualNode) {
        if (actualNode == null || actualNode.isNull()) {
            return "null";
        }
        return actualNode.isTextual() ? actualNode.textValue() : actualNode.toString();
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
}

package com.metao.book.performance;

/**
 * A single response-body check declared on a {@link ScenarioStep}.
 *
 * <p>The compact constructor validates {@code operator} against
 * {@link AssertionOperator#fromSymbol(String)} so a typo in a scenario JSON
 * file fails at configuration load time rather than at first request under
 * load — consistent with the fail-fast style of the rest of the parser.
 *
 * <p>{@link #operator} stays as a {@code String} for report/JSON fidelity with
 * the scenario file; callers that need to evaluate the operator use
 * {@link #operatorType()} which is cached per record.
 */
record StepAssertion(
    String path,
    String operator,
    String expected
) {

    StepAssertion {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("assertion path must be provided");
        }
        // Validates on construction; throws IllegalArgumentException on null/blank/unknown operators.
        AssertionOperator.fromSymbol(operator);
        if (expected == null) {
            expected = "";
        }
    }

    /**
     * Strategy instance for this assertion's operator. Safe to call from the
     * hot path; {@link AssertionOperator#fromSymbol(String)} is backed by a
     * small {@link java.util.Map} lookup on an already-validated key.
     */
    AssertionOperator operatorType() {
        return AssertionOperator.fromSymbol(operator);
    }
}

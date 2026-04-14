package com.metao.book.performance;

record StepAssertion(
    String path,
    String operator,
    String expected
) {

    StepAssertion {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("assertion path must be provided");
        }
        if (operator == null || operator.isBlank()) {
            throw new IllegalArgumentException("assertion operator must be provided");
        }
        if (expected == null) {
            expected = "";
        }
    }
}

package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Strategy pattern via enum-per-constant for scenario assertion operators.
 *
 * <p>Each operator owns its own {@link #matches(JsonNode, String)} implementation,
 * so adding a new operator is a single enum entry — no switches to edit and no
 * risk of forgetting a {@code default} branch. The enum is also the single
 * source of truth for which operator symbols are legal, so
 * {@link StepAssertion}'s compact constructor can fail-fast on typos at scenario
 * parse time rather than at first assertion evaluation under load.
 *
 * <p>Numeric coercion is centralized in {@link #numericCompare(JsonNode, String)}
 * so operators that only make sense for numbers ({@code lt/lte/gt/gte}) share
 * one implementation, while {@link #EQ}/{@link #NE} pick dynamically between
 * numeric and string semantics exactly like the original behaviour.
 */
enum AssertionOperator {

    EQ("eq") {
        @Override boolean matches(JsonNode actual, String expected) {
            if (isAbsent(actual)) {
                return "null".equalsIgnoreCase(expected);
            }
            if (bothNumeric(actual, expected)) {
                return Double.compare(actual.asDouble(), Double.parseDouble(expected)) == 0;
            }
            return stringValue(actual).equals(expected);
        }
    },
    NE("ne") {
        @Override boolean matches(JsonNode actual, String expected) {
            return !EQ.matches(actual, expected);
        }
    },
    LT("lt") {
        @Override boolean matches(JsonNode actual, String expected) {
            return numericCompare(actual, expected) < 0;
        }
    },
    LTE("lte") {
        @Override boolean matches(JsonNode actual, String expected) {
            return numericCompare(actual, expected) <= 0;
        }
    },
    GT("gt") {
        @Override boolean matches(JsonNode actual, String expected) {
            return numericCompare(actual, expected) > 0;
        }
    },
    GTE("gte") {
        @Override boolean matches(JsonNode actual, String expected) {
            return numericCompare(actual, expected) >= 0;
        }
    },
    CONTAINS("contains") {
        @Override boolean matches(JsonNode actual, String expected) {
            if (isAbsent(actual)) {
                return false;
            }
            return stringValue(actual).contains(expected);
        }
    };

    private static final Map<String, AssertionOperator> BY_SYMBOL = Arrays.stream(values())
        .collect(Collectors.toUnmodifiableMap(AssertionOperator::symbol, op -> op));

    private final String symbol;

    AssertionOperator(String symbol) {
        this.symbol = symbol;
    }

    String symbol() {
        return symbol;
    }

    /**
     * Resolves a scenario-file operator symbol (case-insensitive, whitespace-tolerant)
     * to its strategy instance, throwing with the complete list of supported symbols
     * so the error message is actionable for humans editing scenario JSON.
     */
    static AssertionOperator fromSymbol(String raw) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("assertion operator must be provided");
        }
        AssertionOperator op = BY_SYMBOL.get(raw.trim().toLowerCase(Locale.ROOT));
        if (op == null) {
            throw new IllegalArgumentException(
                "Unsupported assertion operator '" + raw + "'. Supported: "
                    + Arrays.stream(values()).map(AssertionOperator::symbol).toList()
            );
        }
        return op;
    }

    abstract boolean matches(JsonNode actual, String expected);

    private static int numericCompare(JsonNode actual, String expected) {
        if (actual == null || !actual.isNumber()) {
            throw new IllegalArgumentException(
                "Numeric operator requires a numeric actual value, got: "
                    + (actual == null ? "null" : actual));
        }
        double expectedValue;
        try {
            expectedValue = Double.parseDouble(expected);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(
                "Numeric operator requires a numeric expected value, got: " + expected);
        }
        return Double.compare(actual.asDouble(), expectedValue);
    }

    private static boolean bothNumeric(JsonNode actual, String expected) {
        if (!actual.isNumber()) {
            return false;
        }
        try {
            Double.parseDouble(expected);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static boolean isAbsent(JsonNode node) {
        return node == null || node.isMissingNode() || node.isNull();
    }

    private static String stringValue(JsonNode node) {
        return node.isTextual() ? node.textValue() : node.toString();
    }
}

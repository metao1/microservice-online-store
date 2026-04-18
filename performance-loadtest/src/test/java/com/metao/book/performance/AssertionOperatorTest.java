package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.IntNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.junit.jupiter.api.Test;

class AssertionOperatorTest {

    @Test
    void fromSymbolResolvesKnownOperatorsCaseInsensitive() {
        assertEquals(AssertionOperator.EQ, AssertionOperator.fromSymbol("eq"));
        assertEquals(AssertionOperator.EQ, AssertionOperator.fromSymbol("  EQ "));
        assertEquals(AssertionOperator.LT, AssertionOperator.fromSymbol("Lt"));
        assertEquals(AssertionOperator.CONTAINS, AssertionOperator.fromSymbol("contains"));
    }

    @Test
    void fromSymbolRejectsUnknownOperatorWithSupportedList() {
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> AssertionOperator.fromSymbol("less_than")
        );
        // Error message must list the supported operators so scenario-file
        // authors get an actionable hint rather than a generic failure.
        String message = ex.getMessage();
        assertTrue(message.contains("less_than"));
        for (AssertionOperator op : AssertionOperator.values()) {
            assertTrue(message.contains(op.symbol()),
                () -> "error message missing operator " + op.symbol() + ": " + message);
        }
    }

    @Test
    void fromSymbolRejectsBlankOrNull() {
        assertThrows(IllegalArgumentException.class, () -> AssertionOperator.fromSymbol(null));
        assertThrows(IllegalArgumentException.class, () -> AssertionOperator.fromSymbol(""));
        assertThrows(IllegalArgumentException.class, () -> AssertionOperator.fromSymbol("   "));
    }

    @Test
    void eqComparesNumericWhenBothSidesNumeric() {
        assertTrue(AssertionOperator.EQ.matches(new IntNode(5), "5"));
        assertTrue(AssertionOperator.EQ.matches(new IntNode(5), "5.0"));
        assertFalse(AssertionOperator.EQ.matches(new IntNode(5), "6"));
    }

    @Test
    void eqFallsBackToStringWhenExpectedIsNotNumeric() {
        assertTrue(AssertionOperator.EQ.matches(new TextNode("READY"), "READY"));
        assertFalse(AssertionOperator.EQ.matches(new TextNode("READY"), "PENDING"));
    }

    @Test
    void eqTreatsAbsentActualAsLiteralNull() {
        assertTrue(AssertionOperator.EQ.matches(NullNode.getInstance(), "null"));
        assertTrue(AssertionOperator.EQ.matches(MissingNode.getInstance(), "NULL"));
        assertFalse(AssertionOperator.EQ.matches(NullNode.getInstance(), "something"));
    }

    @Test
    void neIsTheNegationOfEq() {
        JsonNode five = new IntNode(5);
        assertFalse(AssertionOperator.NE.matches(five, "5"));
        assertTrue(AssertionOperator.NE.matches(five, "6"));
    }

    @Test
    void numericOperatorsCompareCorrectly() {
        JsonNode ten = new IntNode(10);
        assertTrue(AssertionOperator.LT.matches(ten, "11"));
        assertFalse(AssertionOperator.LT.matches(ten, "10"));
        assertTrue(AssertionOperator.LTE.matches(ten, "10"));
        assertTrue(AssertionOperator.GT.matches(ten, "9"));
        assertFalse(AssertionOperator.GT.matches(ten, "10"));
        assertTrue(AssertionOperator.GTE.matches(ten, "10"));
    }

    @Test
    void numericOperatorsRejectNonNumericActualOrExpected() {
        assertThrows(IllegalArgumentException.class,
            () -> AssertionOperator.LT.matches(new TextNode("five"), "10"));
        assertThrows(IllegalArgumentException.class,
            () -> AssertionOperator.GT.matches(new IntNode(5), "ten"));
        assertThrows(IllegalArgumentException.class,
            () -> AssertionOperator.LTE.matches(NullNode.getInstance(), "5"));
    }

    @Test
    void containsChecksSubstring() {
        assertTrue(AssertionOperator.CONTAINS.matches(new TextNode("Order created"), "created"));
        assertFalse(AssertionOperator.CONTAINS.matches(new TextNode("Order created"), "deleted"));
        assertFalse(AssertionOperator.CONTAINS.matches(NullNode.getInstance(), "anything"));
    }
}

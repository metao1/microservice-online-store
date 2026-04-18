package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class AsciiTableTest {

    @Test
    void rendersAutoSizedTableWithHeaderSeparatorAndAlignedCells() {
        String table = AsciiTable.render(
            List.of("Step", "Count", "p95(ms)"),
            List.of(AsciiTable.Alignment.LEFT, AsciiTable.Alignment.RIGHT, AsciiTable.Alignment.RIGHT),
            List.of(
                List.of("lookup-payment", "1000", "12.3"),
                List.of("read-product", "950", "8.1")
            )
        );

        List<String> lines = table.lines().toList();

        // Expected shape: separator, header, separator, row, row, separator.
        assertEquals(6, lines.size(), () -> "unexpected line count, got:\n" + table);
        assertTrue(lines.get(0).startsWith("+-") && lines.get(0).endsWith("+"));
        assertEquals(lines.get(0), lines.get(2), "header separators must match");
        assertEquals(lines.get(0), lines.get(5), "footer separator must match header separator");

        // All data rows render at identical width so columns align in terminal.
        assertEquals(lines.get(1).length(), lines.get(3).length());
        assertEquals(lines.get(1).length(), lines.get(4).length());
        assertEquals(lines.get(1).length(), lines.get(0).length());

        // Right-alignment guarantee: the numeric cell's last content character
        // sits immediately before the closing " |" delimiter.
        assertTrue(lines.get(3).endsWith("12.3 |"),
            () -> "p95 cell should be right-aligned to the column edge, got: " + lines.get(3));
        assertTrue(lines.get(4).endsWith("8.1 |"),
            () -> "p95 cell should be right-aligned to the column edge, got: " + lines.get(4));

        // Left-alignment guarantee: the Step cell starts right after "| ".
        assertTrue(lines.get(3).startsWith("| lookup-payment"));
        assertTrue(lines.get(4).startsWith("| read-product"));
    }

    @Test
    void rejectsRowWithWrongCellCount() {
        assertThrows(IllegalArgumentException.class, () -> AsciiTable.render(
            List.of("A", "B"),
            List.of(AsciiTable.Alignment.LEFT, AsciiTable.Alignment.LEFT),
            List.of(List.of("only-one-cell"))
        ));
    }

    @Test
    void rejectsMismatchedHeadersAndAlignments() {
        assertThrows(IllegalArgumentException.class, () -> AsciiTable.render(
            List.of("A", "B"),
            List.of(AsciiTable.Alignment.LEFT),
            List.of()
        ));
    }
}

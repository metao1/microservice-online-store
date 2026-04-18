package com.metao.book.performance;

import java.util.List;

/**
 * Minimal auto-sized ASCII table renderer for CLI output.
 * <p>
 * Columns are sized to the widest cell (header or row value) and padded with
 * spaces. Each column has an explicit alignment — numeric columns align right
 * so percentile / count numbers read vertically. No external dependency; the
 * load-test CLI stays single-jar.
 */
final class AsciiTable {

    enum Alignment {
        LEFT, RIGHT
    }

    private AsciiTable() {
    }

    /**
     * Renders a table with a header row and zero or more body rows. Every row
     * must have exactly {@code headers.size()} cells; otherwise an
     * {@link IllegalArgumentException} is thrown so bugs in the caller surface
     * loudly rather than producing a silently-misaligned table.
     */
    static String render(List<String> headers, List<Alignment> alignments, List<List<String>> rows) {
        if (headers.size() != alignments.size()) {
            throw new IllegalArgumentException("headers and alignments must have the same length");
        }
        int columnCount = headers.size();
        int[] widths = new int[columnCount];
        for (int index = 0; index < columnCount; index += 1) {
            widths[index] = headers.get(index).length();
        }
        for (List<String> row : rows) {
            if (row.size() != columnCount) {
                throw new IllegalArgumentException(
                    "row has " + row.size() + " cells but header has " + columnCount
                );
            }
            for (int index = 0; index < columnCount; index += 1) {
                widths[index] = Math.max(widths[index], row.get(index).length());
            }
        }

        String separator = buildSeparator(widths);
        StringBuilder out = new StringBuilder();
        out.append(separator).append(System.lineSeparator());
        out.append(buildRow(headers, widths, alignments, /* headerRow */ true));
        out.append(separator).append(System.lineSeparator());
        for (List<String> row : rows) {
            out.append(buildRow(row, widths, alignments, /* headerRow */ false));
        }
        out.append(separator).append(System.lineSeparator());
        return out.toString();
    }

    private static String buildSeparator(int[] widths) {
        StringBuilder out = new StringBuilder("+");
        for (int width : widths) {
            // Two-space padding inside each cell, so the total cell width is width + 2.
            for (int index = 0; index < width + 2; index += 1) {
                out.append('-');
            }
            out.append('+');
        }
        return out.toString();
    }

    private static String buildRow(List<String> cells, int[] widths, List<Alignment> alignments, boolean headerRow) {
        StringBuilder out = new StringBuilder("|");
        for (int index = 0; index < cells.size(); index += 1) {
            String cell = cells.get(index);
            int width = widths[index];
            Alignment alignment = headerRow ? Alignment.LEFT : alignments.get(index);
            out.append(' ').append(pad(cell, width, alignment)).append(" |");
        }
        out.append(System.lineSeparator());
        return out.toString();
    }

    private static String pad(String value, int width, Alignment alignment) {
        int padding = width - value.length();
        if (padding <= 0) {
            return value;
        }
        StringBuilder out = new StringBuilder(width);
        if (alignment == Alignment.RIGHT) {
            for (int index = 0; index < padding; index += 1) {
                out.append(' ');
            }
            out.append(value);
        } else {
            out.append(value);
            for (int index = 0; index < padding; index += 1) {
                out.append(' ');
            }
        }
        return out.toString();
    }
}

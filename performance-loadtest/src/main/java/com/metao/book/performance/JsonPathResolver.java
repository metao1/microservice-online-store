package com.metao.book.performance;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Minimal JSON-path-style resolver supporting the dotted subset used by
 * scenarios (e.g. {@code $.orderId}, {@code $.items.0.sku}).
 * <p>
 * Intentionally narrow: we do not need wildcards, filters, or recursive
 * descent. Keeping the grammar small means we avoid a 200 KB JsonPath
 * dependency and the confusing error messages that come with it.
 */
final class JsonPathResolver {

    private JsonPathResolver() {
    }

    /**
     * Walks {@code root} along the dotted path and returns the matching node.
     * Numeric segments address array indices; everything else is a field name.
     * Throws when the path is malformed or points at a missing value so the
     * caller can surface a descriptive error instead of producing a misleading
     * "extracted null" result.
     */
    static JsonNode resolve(JsonNode root, String path) {
        if (path == null || !path.startsWith("$.") || path.length() <= 2) {
            throw new IllegalArgumentException("Only simple JSON field paths are supported: " + path);
        }
        JsonNode current = root;
        for (String segment : path.substring(2).split("\\.")) {
            if (current == null) {
                break;
            }
            if (current.isArray()) {
                try {
                    int index = Integer.parseInt(segment);
                    current = current.get(index);
                } catch (NumberFormatException ignored) {
                    current = current.get(segment);
                }
            } else {
                current = current.get(segment);
            }
        }
        if (current == null || current.isMissingNode()) {
            throw new IllegalArgumentException("JSON path not found: " + path);
        }
        return current;
    }
}

package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

class JsonPathResolverTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Test
    void shouldResolveSimpleField() throws Exception {
        JsonNode root = MAPPER.readTree("{\"orderId\":\"ord-42\"}");

        JsonNode result = JsonPathResolver.resolve(root, "$.orderId");

        assertTrue(result.isTextual());
        assertEquals("ord-42", result.textValue());
    }

    @Test
    void shouldResolveNestedField() throws Exception {
        JsonNode root = MAPPER.readTree("{\"payment\":{\"status\":\"SUCCESSFUL\"}}");

        JsonNode result = JsonPathResolver.resolve(root, "$.payment.status");

        assertEquals("SUCCESSFUL", result.textValue());
    }

    @Test
    void shouldResolveArrayIndex() throws Exception {
        JsonNode root = MAPPER.readTree("{\"items\":[{\"sku\":\"A\"},{\"sku\":\"B\"}]}");

        JsonNode result = JsonPathResolver.resolve(root, "$.items.1.sku");

        assertEquals("B", result.textValue());
    }

    @Test
    void shouldRejectMalformedPath() throws Exception {
        JsonNode root = MAPPER.readTree("{}");
        assertThrows(IllegalArgumentException.class, () -> JsonPathResolver.resolve(root, "orderId"));
        assertThrows(IllegalArgumentException.class, () -> JsonPathResolver.resolve(root, "$."));
        assertThrows(IllegalArgumentException.class, () -> JsonPathResolver.resolve(root, null));
    }

    @Test
    void shouldThrowWhenPathMissing() throws Exception {
        JsonNode root = MAPPER.readTree("{\"a\":1}");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> JsonPathResolver.resolve(root, "$.b.c")
        );
        assertTrue(error.getMessage().contains("$.b.c"));
    }
}

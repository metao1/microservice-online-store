package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class TemplateRendererTest {

    @Test
    void shouldLeaveStringWithoutPlaceholdersUntouched() {
        assertEquals("plain-string", TemplateRenderer.resolve("plain-string", Map.of()));
    }

    @Test
    void shouldSubstituteAllPlaceholders() {
        Map<String, String> context = Map.of("vu", "3", "iteration", "42");
        assertEquals("loadtest-3-42", TemplateRenderer.resolve("loadtest-${vu}-${iteration}", context));
    }

    @Test
    void shouldThrowMissingValueExceptionWhenPlaceholderUnresolved() {
        TemplateRenderer.MissingTemplateValueException error = assertThrows(
            TemplateRenderer.MissingTemplateValueException.class,
            () -> TemplateRenderer.resolve("hello-${missing}", Map.of("vu", "1"))
        );
        assertTrue(error.getMessage().contains("missing"));
    }

    @Test
    void shouldResolveScenarioVariablesWithForwardReferences() {
        // "userId" depends on "vu" and "iteration" which are seeded in the base
        // context; "sessionKey" depends on the already-resolved "userId", so
        // resolution MUST handle declaration order independently.
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("sessionKey", "session-${userId}");
        variables.put("userId", "u-${vu}-${iteration}");

        Map<String, String> baseContext = Map.of("vu", "7", "iteration", "100");

        Map<String, String> resolved = TemplateRenderer.resolveVariables(variables, baseContext);

        assertEquals("u-7-100", resolved.get("userId"));
        assertEquals("session-u-7-100", resolved.get("sessionKey"));
    }

    @Test
    void shouldThrowOnCircularVariableDependency() {
        Map<String, String> variables = new LinkedHashMap<>();
        variables.put("a", "${b}");
        variables.put("b", "${a}");

        IllegalArgumentException error = assertThrows(
            IllegalArgumentException.class,
            () -> TemplateRenderer.resolveVariables(variables, Map.of())
        );
        assertTrue(error.getMessage().contains("Unable to resolve"));
    }

    @Test
    void shouldRenderRequestSpecWithoutMutatingOriginal() {
        HttpRequestSpec original = new HttpRequestSpec(
            "POST",
            "http://svc/${path}",
            "{\"id\":\"${id}\"}",
            "none",
            Map.of("X-Trace", "${traceId}")
        );
        Map<String, String> context = Map.of("path", "orders", "id", "42", "traceId", "abc");

        HttpRequestSpec rendered = TemplateRenderer.renderRequest(original, context);

        assertEquals("http://svc/orders", rendered.url());
        assertEquals("{\"id\":\"42\"}", rendered.body());
        assertEquals("abc", rendered.headers().get("X-Trace"));
        // Original must be untouched: rendered instance is a new record.
        assertEquals("http://svc/${path}", original.url());
        assertEquals("{\"id\":\"${id}\"}", original.body());
    }
}

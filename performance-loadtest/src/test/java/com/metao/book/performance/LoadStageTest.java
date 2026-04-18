package com.metao.book.performance;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class LoadStageTest {

    @Test
    void shouldRejectInvalidDuration() {
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(0, 1, null));
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(-5, 1, null));
    }

    @Test
    void shouldRejectInvalidUserCount() {
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(10, 0, null));
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(10, -1, null));
    }

    @Test
    void shouldRejectNonPositiveTargetRpsButAllowNull() {
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(10, 1, 0.0));
        assertThrows(IllegalArgumentException.class, () -> new LoadStage(10, 1, -1.5));
        assertDoesNotThrow(() -> new LoadStage(10, 1, null));
    }

    @Test
    void shouldExposeProvidedFields() {
        LoadStage stage = new LoadStage(30, 50, 100.0);
        assertEquals(30, stage.durationSec());
        assertEquals(50, stage.users());
        assertEquals(100.0, stage.targetRps());
    }
}

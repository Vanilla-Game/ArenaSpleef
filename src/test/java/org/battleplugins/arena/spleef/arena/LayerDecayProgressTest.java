package org.battleplugins.arena.spleef.arena;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LayerDecayProgressTest {
    @Test
    void spleefSphereFinishesInTwentySeconds() {
        LayerDecayProgress progress = new LayerDecayProgress(3249, 400);
        int removed = 0;
        for (int tick = 1; tick <= 400; tick++) {
            int batch = progress.nextBatchSize();
            assertTrue(batch == 8 || batch == 9);
            removed += batch;
            if (tick < 400) {
                assertTrue(removed < 3249);
            }
        }
        assertEquals(3249, removed);
        assertEquals(0, progress.nextBatchSize());
    }

    @Test
    void sparseLayerKeepsItsFullDuration() {
        LayerDecayProgress progress = new LayerDecayProgress(3, 400);
        int removed = 0;
        for (int tick = 1; tick <= 400; tick++) {
            removed += progress.nextBatchSize();
            assertEquals(tick * 3 / 400, removed);
        }
        assertEquals(3, removed);
    }

    @Test
    void nonDivisibleDurationDoesNotFinishEarly() {
        LayerDecayProgress progress = new LayerDecayProgress(7, 20);
        int removed = 0;
        for (int tick = 1; tick < 20; tick++) {
            removed += progress.nextBatchSize();
        }
        assertEquals(6, removed);
        assertEquals(1, progress.nextBatchSize());
    }

    @Test
    void oneTickDurationRemovesTheWholeLayer() {
        LayerDecayProgress progress = new LayerDecayProgress(3249, 1);
        assertEquals(3249, progress.nextBatchSize());
        assertEquals(0, progress.nextBatchSize());
    }

    @Test
    void emptyLayerHasNoWork() {
        assertEquals(0, new LayerDecayProgress(0, 400).nextBatchSize());
    }

    @Test
    void rejectsInvalidInputs() {
        assertThrows(IllegalArgumentException.class, () -> new LayerDecayProgress(-1, 400));
        assertThrows(IllegalArgumentException.class, () -> new LayerDecayProgress(1, 0));
    }
}

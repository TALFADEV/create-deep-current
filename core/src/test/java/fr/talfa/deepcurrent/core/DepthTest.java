package fr.talfa.deepcurrent.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class DepthTest {
    @Test
    void depthIsDistanceBelowSeaLevel() {
        assertEquals(16, Depth.belowSeaLevel(63, 47));
    }

    @Test
    void depthIsZeroAtOrAboveSeaLevel() {
        assertEquals(0, Depth.belowSeaLevel(63, 63));
        assertEquals(0, Depth.belowSeaLevel(63, 80));
    }
}

package fr.talfa.deepcurrent.core.turbine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TidalTurbineTest {
    private static final TurbineSpec SPEC = TurbineSpec.SMALL;

    @Test
    void referenceConditionsGiveTheNominalOutput() {
        int rpm = TidalTurbine.speed(SPEC, 1, 1, 1);
        assertEquals(32, rpm);
        assertEquals(16384, TidalTurbine.capacity(SPEC, rpm), 1e-9);
    }

    @Test
    void speedFollowsCurrentTideAndEfficiency() {
        assertEquals(16, TidalTurbine.speed(SPEC, 0.5, 1, 1));
        assertEquals(8, TidalTurbine.speed(SPEC, 1, 0.25, 1));
        assertEquals(24, TidalTurbine.speed(SPEC, 1, 1, 0.75));
        assertEquals(40, TidalTurbine.speed(SPEC, 1.25, 1, 1));
    }

    @Test
    void speedIsWholeAndCapped() {
        assertEquals(10, TidalTurbine.speed(SPEC, 0.31, 1, 1), "9.92 RPM rounds to 10");
        assertEquals(256, TidalTurbine.speed(SPEC, 100, 1, 1));
        assertEquals(0, TidalTurbine.speed(SPEC, 0, 1, 1), "no current, no rotation");
        assertEquals(0, TidalTurbine.speed(SPEC, -1, 1, 1));
    }

    @Test
    void immersionNeedsDepthWetFacesAndWaterAbove() {
        assertTrue(TidalTurbine.isImmersed(SPEC, 5, 4, 3));
        assertFalse(TidalTurbine.isImmersed(SPEC, 0, 6, 3), "at or above sea level");
        assertFalse(TidalTurbine.isImmersed(SPEC, 5, 3, 3), "too few wet faces");
        assertFalse(TidalTurbine.isImmersed(SPEC, 5, 4, 2), "not enough water above");
    }
}

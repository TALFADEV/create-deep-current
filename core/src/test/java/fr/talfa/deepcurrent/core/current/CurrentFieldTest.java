package fr.talfa.deepcurrent.core.current;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CurrentFieldTest {
    private static final CurrentSettings S = CurrentSettings.DEFAULT;

    @Test
    void sameSeedGivesSameCurrents() {
        CurrentField a = new CurrentField(42, S);
        CurrentField b = new CurrentField(42, S);
        for (int i = -2000; i < 2000; i += 137) {
            assertEquals(a.speedAt(i, -i * 3), b.speedAt(i, -i * 3));
        }
    }

    @Test
    void differentSeedsGiveDifferentCurrents() {
        CurrentField a = new CurrentField(1, S);
        CurrentField b = new CurrentField(2, S);
        assertNotEquals(a.speedAt(1000, 1000), b.speedAt(1000, 1000));
    }

    @Test
    void speedsStayWithinConfiguredRange() {
        CurrentField field = new CurrentField(123456789L, S);
        double min = Double.MAX_VALUE;
        double max = -Double.MAX_VALUE;
        for (int x = -20000; x < 20000; x += 97) {
            for (int z = -20000; z < 20000; z += 1013) {
                double speed = field.speedAt(x, z);
                assertTrue(speed >= S.minSpeed() && speed <= S.maxSpeed(), "speed out of range: " + speed);
                min = Math.min(min, speed);
                max = Math.max(max, speed);
            }
        }
        // The field must actually vary across the ocean, not sit near one value.
        assertTrue(max - min > (S.maxSpeed() - S.minSpeed()) * 0.5);
    }

    @Test
    void speedChangesSmoothlyBetweenNeighbours() {
        CurrentField field = new CurrentField(7, S);
        double maxStep = 0;
        for (int x = -3000; x < 3000; x++) {
            maxStep = Math.max(maxStep, Math.abs(field.speedAt(x + 1, 50) - field.speedAt(x, 50)));
        }
        // Smoothstep slope peaks at 1.5x the linear slope across one region.
        double bound = 1.5 * (S.maxSpeed() - S.minSpeed()) / S.regionSize() + 1e-9;
        assertTrue(maxStep <= bound, "jump of " + maxStep);
    }

    @Test
    void factorIsSpeedOverReference() {
        CurrentField field = new CurrentField(5, S);
        assertEquals(field.speedAt(10, 20) / S.referenceSpeed(), field.oceanFactorAt(10, 20), 1e-12);
    }

    @Test
    void flatFieldWhenMinEqualsMax() {
        CurrentField field = new CurrentField(9, new CurrentSettings(64, 1.5, 1.5, 2.0, 0.2));
        assertEquals(1.5, field.speedAt(-777, 31337), 1e-12);
    }

    @Test
    void rejectsInvalidSettings() {
        assertThrows(IllegalArgumentException.class, () -> new CurrentSettings(0, 0.5, 2, 2, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new CurrentSettings(64, 2, 1, 2, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new CurrentSettings(64, 0.5, 2, 0, 0.2));
    }
}

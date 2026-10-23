package fr.talfa.deepcurrent.core.tide;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class TideModelTest {
    private static final double EPS = 1e-9;
    private static final TideSettings S = TideSettings.DEFAULT;
    private static final int FULL_MOON = 0;
    private static final int FIRST_QUARTER = 6;
    private static final int NEW_MOON = 4;

    @Test
    void highTidesAtNoonAndMidnight() {
        assertEquals(1, TideModel.level(6000), EPS);
        assertEquals(1, TideModel.level(18000), EPS);
        assertEquals(-1, TideModel.level(0), EPS);
        assertEquals(-1, TideModel.level(12000), EPS);
    }

    @Test
    void twoHighTidesPerDay() {
        int highs = 0;
        for (int t = 0; t < TideModel.TICKS_PER_DAY; t++) {
            if (TideModel.level(t) > TideModel.level(t - 1) && TideModel.level(t) > TideModel.level(t + 1)) {
                highs++;
            }
        }
        assertEquals(2, highs);
    }

    @Test
    void streamIsWeakestAtSlackAndStrongestAtMidTide() {
        assertEquals(S.slackFloor(), TideModel.streamFactor(6000, S), EPS);
        assertEquals(S.slackFloor(), TideModel.streamFactor(0, S), EPS);
        assertEquals(1, TideModel.streamFactor(3000, S), EPS);
        assertEquals(1, TideModel.streamFactor(9000, S), EPS);
    }

    @Test
    void risesBeforeHighTideAndFallsAfter() {
        assertTrue(TideModel.isRising(3000));
        assertFalse(TideModel.isRising(9000));
    }

    @Test
    void springTidesAtFullAndNewMoonNeapAtQuarters() {
        assertEquals(1, TideModel.lunarFactor(FULL_MOON, S), EPS);
        assertEquals(1, TideModel.lunarFactor(NEW_MOON, S), EPS);
        assertEquals(1 - S.lunarInfluence(), TideModel.lunarFactor(FIRST_QUARTER, S), EPS);
        assertEquals(1 - S.lunarInfluence(), TideModel.lunarFactor(2, S), EPS);
        double gibbous = TideModel.lunarFactor(1, S);
        assertTrue(gibbous < 1 && gibbous > 1 - S.lunarInfluence());
    }

    @Test
    void coefficientCombinesAmplitudeMoonAndStream() {
        TideSettings settings = new TideSettings(1.2, 0.5, 0.25);
        assertEquals(1.2, TideModel.coefficient(3000, FULL_MOON, settings), EPS);
        assertEquals(1.2 * 0.5 * 0.25, TideModel.coefficient(6000, FIRST_QUARTER, settings), EPS);
    }

    @Test
    void handlesTimeBeyondFirstDayAndNegativeValues() {
        assertEquals(TideModel.level(3000), TideModel.level(3000 + 5L * TideModel.TICKS_PER_DAY), EPS);
        assertEquals(TideModel.level(23000), TideModel.level(-1000), EPS);
        assertEquals(TideModel.lunarFactor(1, S), TideModel.lunarFactor(9, S), EPS);
    }

    @Test
    void floorOfOneRemovesDailyVariation() {
        TideSettings flat = new TideSettings(1, 0, 1);
        for (int t = 0; t < TideModel.TICKS_PER_DAY; t += 500) {
            assertEquals(1, TideModel.coefficient(t, FIRST_QUARTER, flat), EPS);
        }
    }

    @Test
    void rejectsInvalidSettings() {
        assertThrows(IllegalArgumentException.class, () -> new TideSettings(-1, 0.5, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new TideSettings(1, 1.5, 0.2));
        assertThrows(IllegalArgumentException.class, () -> new TideSettings(1, 0.5, -0.1));
    }
}

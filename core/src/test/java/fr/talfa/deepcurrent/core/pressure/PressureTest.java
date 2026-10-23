package fr.talfa.deepcurrent.core.pressure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PressureTest {
    private static final PressureTiers TIERS = PressureTiers.DEFAULT;
    private static final IntegritySettings S = IntegritySettings.DEFAULT;
    private static final int ONE_MINUTE = 20 * 60;

    @Test
    void defaultTiersMatchTheDesign() {
        assertEquals(4, TIERS.count());
        assertEquals(16, TIERS.maxDepth(1));
        assertEquals(32, TIERS.maxDepth(2));
        assertEquals(64, TIERS.maxDepth(3));
        assertEquals(PressureTiers.UNLIMITED, TIERS.maxDepth(4));
    }

    @Test
    void requiredTierFollowsDepth() {
        assertEquals(1, TIERS.requiredTier(0));
        assertEquals(1, TIERS.requiredTier(16));
        assertEquals(2, TIERS.requiredTier(17));
        assertEquals(3, TIERS.requiredTier(64));
        assertEquals(4, TIERS.requiredTier(65));
        assertEquals(4, TIERS.requiredTier(500));
    }

    @Test
    void overshootIsDepthBeyondTheTier() {
        assertEquals(0, TIERS.overshoot(1, 16));
        assertEquals(4, TIERS.overshoot(1, 20));
        assertEquals(0, TIERS.overshoot(4, 10_000));
    }

    @Test
    void rejectsBadTiers() {
        assertThrows(IllegalArgumentException.class, () -> new PressureTiers(16, 16));
        assertThrows(IllegalArgumentException.class, () -> TIERS.maxDepth(0));
        assertThrows(IllegalArgumentException.class, () -> TIERS.maxDepth(5));
    }

    @Test
    void noDegradationWithinLimits() {
        assertEquals(100, Integrity.degrade(100, 0, 10 * ONE_MINUTE, S));
    }

    @Test
    void degradesFasterWhenFurtherBeyondTheLimit() {
        double small = Integrity.degrade(100, 1, ONE_MINUTE, S);
        double large = Integrity.degrade(100, 10, ONE_MINUTE, S);
        assertEquals(100 - (2.0 + 0.5), small, 1e-9);
        assertEquals(100 - (2.0 + 5.0), large, 1e-9);
        assertTrue(large < small);
    }

    @Test
    void degradationIsIndependentOfUpdateInterval() {
        double once = Integrity.degrade(100, 6, 2400, S);
        double stepped = 100;
        for (int i = 0; i < 120; i++) {
            stepped = Integrity.degrade(stepped, 6, 20, S);
        }
        assertEquals(once, stepped, 1e-9);
    }

    @Test
    void integrityStaysWithinBounds() {
        assertEquals(0, Integrity.degrade(1, 50, 100 * ONE_MINUTE, S));
        assertEquals(100, Integrity.repair(90, 25));
        assertEquals(50, Integrity.repair(25, 25));
    }

    @Test
    void statesFollowTheDesignThresholds() {
        assertEquals(IntegrityState.INTACT, IntegrityState.of(100));
        assertEquals(IntegrityState.INTACT, IntegrityState.of(75));
        assertEquals(IntegrityState.CREAKING, IntegrityState.of(74.9));
        assertEquals(IntegrityState.LEAKING, IntegrityState.of(49));
        assertEquals(IntegrityState.CRITICAL, IntegrityState.of(24));
        assertEquals(IntegrityState.BROKEN, IntegrityState.of(0));
    }

    @Test
    void efficiencyDropsWithDamage() {
        assertEquals(1, IntegrityState.CREAKING.efficiency(S));
        assertEquals(0.75, IntegrityState.LEAKING.efficiency(S));
        assertEquals(0.4, IntegrityState.CRITICAL.efficiency(S));
        assertEquals(0, IntegrityState.BROKEN.efficiency(S));
    }
}

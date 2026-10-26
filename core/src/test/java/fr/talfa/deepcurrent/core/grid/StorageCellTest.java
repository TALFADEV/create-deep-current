package fr.talfa.deepcurrent.core.grid;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StorageCellTest {
    private static final StorageSpec SPEC = StorageSpec.SMALL_BATTERY;
    private static final double EPS = 1e-9;

    @Test
    void chargingStoresTheEfficientFraction() {
        StorageCell cell = new StorageCell();
        cell.step(-16, 10, SPEC);
        assertEquals(16 * 0.9 * 10, cell.storedKj(), EPS);
    }

    @Test
    void dischargingRemovesEverythingSupplied() {
        StorageCell cell = new StorageCell();
        cell.setStoredKj(1000, SPEC);
        cell.step(16, 10, SPEC);
        assertEquals(840, cell.storedKj(), EPS);
    }

    @Test
    void fullCellStopsCharging() {
        StorageCell cell = new StorageCell();
        cell.setStoredKj(SPEC.capacityKj() - 1, SPEC);
        assertEquals(16, cell.chargeLimitKw(SPEC), EPS);
        assertTrue(cell.step(-16, 1, SPEC), "becoming full changes the limits");
        assertEquals(SPEC.capacityKj(), cell.storedKj(), EPS);
        assertEquals(0, cell.chargeLimitKw(SPEC), EPS);
        assertEquals(16, cell.dischargeLimitKw(SPEC), EPS);
    }

    @Test
    void emptyCellStopsDischarging() {
        StorageCell cell = new StorageCell();
        assertEquals(0, cell.dischargeLimitKw(SPEC), EPS);
        assertTrue(cell.step(-1, 1, SPEC), "leaving empty changes the limits");
        assertEquals(16, cell.dischargeLimitKw(SPEC), EPS);
        assertTrue(cell.step(16, 1, SPEC), "becoming empty changes the limits");
        assertEquals(0, cell.storedKj(), EPS);
    }

    @Test
    void stepsInsideTheRangeKeepTheLimits() {
        StorageCell cell = new StorageCell();
        cell.setStoredKj(3600, SPEC);
        assertFalse(cell.step(-16, 1, SPEC));
        assertFalse(cell.step(16, 1, SPEC));
        assertFalse(cell.step(0, 1, SPEC));
    }

    @Test
    void gaugeLightsOneCellPerQuarter() {
        StorageCell cell = new StorageCell();
        assertEquals(0, cell.level(SPEC, 4));
        cell.setStoredKj(1, SPEC);
        assertEquals(1, cell.level(SPEC, 4));
        cell.setStoredKj(SPEC.capacityKj() * 0.5, SPEC);
        assertEquals(2, cell.level(SPEC, 4));
        cell.setStoredKj(SPEC.capacityKj() * 0.99, SPEC);
        assertEquals(3, cell.level(SPEC, 4), "the last cell lights only when full");
        cell.setStoredKj(SPEC.capacityKj(), SPEC);
        assertEquals(4, cell.level(SPEC, 4));
    }

    @Test
    void comparatorSignalFollowsTheCharge() {
        StorageCell cell = new StorageCell();
        assertEquals(0, cell.comparatorSignal(SPEC));
        cell.setStoredKj(1, SPEC);
        assertEquals(1, cell.comparatorSignal(SPEC));
        cell.setStoredKj(SPEC.capacityKj() * 0.5, SPEC);
        assertEquals(8, cell.comparatorSignal(SPEC));
        cell.setStoredKj(SPEC.capacityKj() * 0.999, SPEC);
        assertEquals(14, cell.comparatorSignal(SPEC));
        cell.setStoredKj(SPEC.capacityKj(), SPEC);
        assertEquals(15, cell.comparatorSignal(SPEC));
    }

    @Test
    void storedEnergyStaysWithinCapacity() {
        StorageCell cell = new StorageCell();
        cell.setStoredKj(-5, SPEC);
        assertEquals(0, cell.storedKj(), EPS);
        cell.setStoredKj(1e9, SPEC);
        assertEquals(SPEC.capacityKj(), cell.storedKj(), EPS);
    }
}

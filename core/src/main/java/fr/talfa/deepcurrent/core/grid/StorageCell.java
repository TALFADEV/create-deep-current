package fr.talfa.deepcurrent.core.grid;

/**
 * Energy held by one storage block. The block steps it every tick with the power the grid solution gives it, and
 * pushes new limits to the grid only when {@link #step} reports that it became full or empty, or left either state:
 * the grid is not solved again as the charge moves.
 */
public final class StorageCell {
    /** Within this many kJ of a bound, the cell counts as full or empty. */
    private static final double EPSILON_KJ = 1e-6;

    private double storedKj;

    public double storedKj() {
        return storedKj;
    }

    /** Sets the stored energy, kept within the spec's capacity. */
    public void setStoredKj(double kj, StorageSpec spec) {
        storedKj = Math.max(0, Math.min(kj, spec.capacityKj()));
    }

    public boolean isFull(StorageSpec spec) {
        return storedKj >= spec.capacityKj() - EPSILON_KJ;
    }

    public boolean isEmpty() {
        return storedKj <= EPSILON_KJ;
    }

    /** Power the cell can absorb now: nothing once full. */
    public double chargeLimitKw(StorageSpec spec) {
        return isFull(spec) ? 0 : spec.maxChargeKw();
    }

    /** Power the cell can supply now: nothing once empty. */
    public double dischargeLimitKw(StorageSpec spec) {
        return isEmpty() ? 0 : spec.maxDischargeKw();
    }

    /**
     * Applies the power the grid gave the cell for {@code seconds}.
     *
     * @param flowKw  as in {@link GridSolution#flowKw(long)}: positive when the cell supplies the grid, negative
     *                when it draws from it (only the efficient fraction of what it draws is stored)
     * @return true when the cell became full or empty, or stopped being so: its limits changed
     */
    public boolean step(double flowKw, double seconds, StorageSpec spec) {
        boolean wasFull = isFull(spec);
        boolean wasEmpty = isEmpty();
        double delta = flowKw < 0 ? -flowKw * spec.chargeEfficiency() * seconds : -flowKw * seconds;
        setStoredKj(storedKj + delta, spec);
        return wasFull != isFull(spec) || wasEmpty != isEmpty();
    }

    /** Stored fraction of the capacity, from 0 to 1. */
    public double fraction(StorageSpec spec) {
        return storedKj / spec.capacityKj();
    }

    /** Lit cells of a gauge of {@code steps} cells: 0 only when empty, all only when full. */
    public int level(StorageSpec spec, int steps) {
        if (isEmpty()) {
            return 0;
        }
        if (isFull(spec)) {
            return steps;
        }
        return Math.max(1, Math.min(steps - 1, (int) Math.ceil(fraction(spec) * steps)));
    }

    /** Comparator signal: 0 when empty, 15 when full, 1 to 14 in between (as vanilla containers do). */
    public int comparatorSignal(StorageSpec spec) {
        if (isEmpty()) {
            return 0;
        }
        if (isFull(spec)) {
            return 15;
        }
        return 1 + Math.min(13, (int) (fraction(spec) * 14));
    }
}

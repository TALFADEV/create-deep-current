package fr.talfa.deepcurrent.core.grid;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Power balance of one network at one moment. All powers are in kW.
 *
 * @param flowKw        per node: positive when the node supplies the grid, negative when it draws from it
 * @param satisfaction  per consumer with a demand: fraction of its demand delivered, from 0 to 1
 * @param cableLoadKw   per cable carrying power: power injected into it (before its own losses)
 * @param overloaded    cables whose load exceeded their capacity; the power through them was capped
 * @param offeredKw     total power producers could inject
 * @param demandedKw    total power consumers asked for
 * @param producedKw    power producers actually injected
 * @param deliveredKw   power that reached consumers
 * @param chargedKw     power that reached storage nodes
 * @param dischargedKw  power storage nodes injected
 * @param lossesKw      power lost in cables
 */
public record GridSolution(Map<Long, Double> flowKw, Map<Long, Double> satisfaction,
                           Map<Cable, Double> cableLoadKw, Set<Cable> overloaded,
                           double offeredKw, double demandedKw, double producedKw, double deliveredKw,
                           double chargedKw, double dischargedKw, double lossesKw) {
    public GridSolution {
        flowKw = Collections.unmodifiableMap(flowKw);
        satisfaction = Collections.unmodifiableMap(satisfaction);
        cableLoadKw = Collections.unmodifiableMap(cableLoadKw);
        overloaded = Collections.unmodifiableSet(overloaded);
    }

    /** Power the node supplies (positive) or draws (negative). */
    public double flowKw(long node) {
        return flowKw.getOrDefault(node, 0.0);
    }

    /** Fraction of the node's demand delivered; 1 for a node with no demand. */
    public double satisfaction(long node) {
        return satisfaction.getOrDefault(node, 1.0);
    }

    public double cableLoadKw(Cable cable) {
        return cableLoadKw.getOrDefault(cable, 0.0);
    }

    public boolean isOverloaded() {
        return !overloaded.isEmpty();
    }

    /** Fraction of the injected power lost in cables, 0 when nothing flows. */
    public double lossRatio() {
        double injected = producedKw + dischargedKw;
        return injected > 0 ? lossesKw / injected : 0;
    }
}

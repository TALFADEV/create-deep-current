package fr.talfa.deepcurrent.core.grid;

/**
 * Conversion between Create's rotational power (stress units, SU) and electricity (kW), and the rules of the
 * machines that do it (GDD §4.1). Create counts a machine's stress as {@code impact per RPM x |speed|}.
 */
public final class PowerConversion {
    /** Below this fraction of its demand delivered, a motor's capacity follows the power it actually gets. */
    private static final double FULLY_SUPPLIED = 0.999;

    private PowerConversion() {
    }

    /** Electricity an alternator produces from the stress it absorbs, after its efficiency. */
    public static double alternatorKw(double impactPerRpm, double speed, double stressPerKw, double efficiency) {
        return impactPerRpm * Math.abs(speed) / stressPerKw * efficiency;
    }

    /**
     * Electricity a motor asks for: its share of the stress used on its kinetic network, in proportion to its
     * full capacity among all the network's sources, and never more than its full capacity.
     *
     * @param stressUsed      stress used on the kinetic network, in SU
     * @param otherCapacity   capacity of the network's other sources, in SU
     * @param motorCapacity   the motor's full capacity at its speed, in SU
     */
    public static double motorDemandKw(double stressUsed, double otherCapacity, double motorCapacity,
                                       double stressPerKw) {
        if (motorCapacity <= 0 || stressUsed <= 0) {
            return 0;
        }
        double share = motorCapacity / (Math.max(0, otherCapacity) + motorCapacity);
        return Math.min(stressUsed * share, motorCapacity) / stressPerKw;
    }

    /**
     * Capacity per RPM a motor offers: full when its demand is met (or when it asks for nothing, so that machines
     * can start), otherwise exactly the stress the delivered electricity pays for.
     */
    public static double motorCapacityPerRpm(double maxPerRpm, double speed, double demandKw, double satisfaction,
                                             double stressPerKw) {
        double rpm = Math.abs(speed);
        if (rpm == 0 || demandKw <= 0 || satisfaction >= FULLY_SUPPLIED) {
            return maxPerRpm;
        }
        double paidStress = demandKw * Math.max(0, satisfaction) * stressPerKw;
        return Math.min(maxPerRpm, paidStress / rpm);
    }
}

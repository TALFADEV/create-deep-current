package fr.talfa.deepcurrent.core.turbine;

/**
 * Rules of the tidal turbines (GDD §4.2, §4.3): when one is immersed, and how fast it turns.
 */
public final class TidalTurbine {
    private TidalTurbine() {
    }

    /**
     * A turbine runs below sea level, with enough of its faces in water and a column of water above it (the alpha
     * immersion rule validated by the author).
     *
     * @param depth       blocks below sea level
     * @param wetFaces    faces touching water, from 0 to 6
     * @param waterAbove  water blocks counted straight above it, up to {@code spec.minWaterAbove()}
     */
    public static boolean isImmersed(TurbineSpec spec, int depth, int wetFaces, int waterAbove) {
        return depth > 0 && wetFaces >= spec.minWetFaces() && waterAbove >= spec.minWaterAbove();
    }

    /**
     * Speed in whole RPM: reference speed x current factor x tide coefficient x efficiency, within the maximum.
     * Whole RPM keep the kinetic network from being updated for tiny changes.
     *
     * @param currentFactor  local current speed relative to the reference speed
     * @param tide           tide coefficient
     * @param efficiency     fraction of its output the turbine keeps (1 when intact)
     */
    public static int speed(TurbineSpec spec, double currentFactor, double tide, double efficiency) {
        double rpm = spec.referenceRpm() * Math.max(0, currentFactor) * Math.max(0, tide) * Math.max(0, efficiency);
        return (int) Math.min(spec.maxRpm(), Math.round(rpm));
    }

    /** Stress the turbine supplies at a speed, in SU. */
    public static double capacity(TurbineSpec spec, int rpm) {
        return spec.capacityPerRpm() * Math.abs(rpm);
    }
}

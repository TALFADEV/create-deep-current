package fr.talfa.deepcurrent.core.current;

/**
 * Base ocean current speed per area, from deterministic noise seeded by the world seed (GDD §4.3).
 *
 * <p>Each corner of a {@code regionSize} grid gets a pseudo-random value from the seed; speeds in between are a
 * smooth blend of the four surrounding corners, so currents change gradually across the ocean and are identical
 * for every player and every restart of a given world.
 */
public final class CurrentField {
    private final long seed;
    private final CurrentSettings settings;

    public CurrentField(long seed, CurrentSettings settings) {
        this.seed = seed;
        this.settings = settings;
    }

    public long seed() {
        return seed;
    }

    public CurrentSettings settings() {
        return settings;
    }

    /** Ocean current speed at a block column, in m/s. */
    public double speedAt(int x, int z) {
        return settings.minSpeed() + (settings.maxSpeed() - settings.minSpeed()) * noise(x, z);
    }

    /** Ocean current multiplier for turbine output: speed relative to the reference speed. */
    public double oceanFactorAt(int x, int z) {
        return speedAt(x, z) / settings.referenceSpeed();
    }

    /** Value noise in [0, 1]. */
    double noise(int x, int z) {
        double gx = (double) x / settings.regionSize();
        double gz = (double) z / settings.regionSize();
        long cx = (long) Math.floor(gx);
        long cz = (long) Math.floor(gz);
        double tx = smoothstep(gx - cx);
        double tz = smoothstep(gz - cz);

        double top = lerp(corner(cx, cz), corner(cx + 1, cz), tx);
        double bottom = lerp(corner(cx, cz + 1), corner(cx + 1, cz + 1), tx);
        return lerp(top, bottom, tz);
    }

    private double corner(long cx, long cz) {
        long h = mix(seed ^ mix(cx * 0x9E3779B97F4A7C15L) ^ mix(cz * 0xC2B2AE3D27D4EB4FL));
        return (h >>> 11) * 0x1.0p-53;
    }

    /** SplitMix64 finalizer. */
    private static long mix(long z) {
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }

    private static double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }
}

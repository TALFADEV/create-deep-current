package fr.talfa.deepcurrent.core;

/**
 * Depth measurement, GDD §3.1: {@code depth = seaLevel - y} for a submerged block.
 */
public final class Depth {
    private Depth() {
    }

    /** Blocks below sea level; zero at or above it. */
    public static int belowSeaLevel(int seaLevel, int y) {
        return Math.max(0, seaLevel - y);
    }
}

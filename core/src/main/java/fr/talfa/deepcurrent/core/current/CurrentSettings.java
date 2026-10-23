package fr.talfa.deepcurrent.core.current;

/**
 * Ocean current tuning (GDD §4.3).
 *
 * @param regionSize     width in blocks of a current region; speeds blend smoothly between regions
 * @param minSpeed       slowest base current, in m/s
 * @param maxSpeed       fastest base current, in m/s
 * @param referenceSpeed speed giving a factor of 1 for turbine output, in m/s
 * @param riverFactor    current factor in rivers, as a fraction of the reference speed
 */
public record CurrentSettings(int regionSize, double minSpeed, double maxSpeed, double referenceSpeed,
                              double riverFactor) {
    public static final CurrentSettings DEFAULT = new CurrentSettings(256, 0.5, 2.5, 2.0, 0.2);

    public CurrentSettings {
        if (regionSize < 1) {
            throw new IllegalArgumentException("regionSize must be >= 1");
        }
        if (minSpeed < 0 || maxSpeed < minSpeed) {
            throw new IllegalArgumentException("speeds must satisfy 0 <= minSpeed <= maxSpeed");
        }
        if (referenceSpeed <= 0) {
            throw new IllegalArgumentException("referenceSpeed must be > 0");
        }
        if (riverFactor < 0) {
            throw new IllegalArgumentException("riverFactor must be >= 0");
        }
    }
}

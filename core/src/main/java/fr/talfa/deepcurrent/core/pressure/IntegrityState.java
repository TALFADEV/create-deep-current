package fr.talfa.deepcurrent.core.pressure;

/**
 * Damage stages of a submerged machine (GDD §3.4), each with its feedback.
 */
public enum IntegrityState {
    /** 75 % and above: no effect. */
    INTACT,
    /** Below 75 %: creaks and bubbles. */
    CREAKING,
    /** Below 50 %: reduced output and visible leaks. */
    LEAKING,
    /** Below 25 %: heavily reduced output and cracking sounds. */
    CRITICAL,
    /** 0 %: the machine breaks. */
    BROKEN;

    public static final double CREAKING_BELOW = 75;
    public static final double LEAKING_BELOW = 50;
    public static final double CRITICAL_BELOW = 25;

    public static IntegrityState of(double integrity) {
        if (integrity <= 0) {
            return BROKEN;
        }
        if (integrity < CRITICAL_BELOW) {
            return CRITICAL;
        }
        if (integrity < LEAKING_BELOW) {
            return LEAKING;
        }
        if (integrity < CREAKING_BELOW) {
            return CREAKING;
        }
        return INTACT;
    }

    /** Output multiplier of a machine in this state. */
    public double efficiency(IntegritySettings settings) {
        return switch (this) {
            case INTACT, CREAKING -> 1;
            case LEAKING -> settings.leakingEfficiency();
            case CRITICAL -> settings.criticalEfficiency();
            case BROKEN -> 0;
        };
    }
}

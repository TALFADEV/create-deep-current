package fr.talfa.deepcurrent.core.pressure;

/**
 * Overpressure degradation tuning (GDD §3.4). Integrity is expressed in percent, from 0 to 100.
 *
 * @param baseLossPerMinute     integrity lost per minute as soon as the machine is too deep
 * @param lossPerBlockPerMinute extra integrity lost per minute for each block beyond the maximum depth
 * @param leakingEfficiency     output multiplier below 50 % integrity
 * @param criticalEfficiency    output multiplier below 25 % integrity
 */
public record IntegritySettings(double baseLossPerMinute, double lossPerBlockPerMinute,
                                double leakingEfficiency, double criticalEfficiency) {
    public static final IntegritySettings DEFAULT = new IntegritySettings(2.0, 0.5, 0.75, 0.4);

    public IntegritySettings {
        if (baseLossPerMinute < 0 || lossPerBlockPerMinute < 0) {
            throw new IllegalArgumentException("loss rates must be >= 0");
        }
        if (leakingEfficiency < 0 || leakingEfficiency > 1 || criticalEfficiency < 0 || criticalEfficiency > 1) {
            throw new IllegalArgumentException("efficiencies must be within [0, 1]");
        }
    }
}

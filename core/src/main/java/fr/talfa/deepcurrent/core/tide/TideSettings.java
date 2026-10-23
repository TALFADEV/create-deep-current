package fr.talfa.deepcurrent.core.tide;

/**
 * Tide tuning (GDD §4.3, §11).
 *
 * @param amplitude      coefficient at the strongest spring tide, at mid-tide
 * @param lunarInfluence how much weaker neap tides are than spring tides, from 0 (no lunar effect) to 1
 *                       (neap tides vanish)
 * @param slackFloor     fraction of the flow kept at slack water (high and low tide), from 0 to 1; 1 disables
 *                       the variation within the day
 */
public record TideSettings(double amplitude, double lunarInfluence, double slackFloor) {
    public static final TideSettings DEFAULT = new TideSettings(1.0, 0.5, 0.25);

    public TideSettings {
        if (amplitude < 0) {
            throw new IllegalArgumentException("amplitude must be >= 0");
        }
        if (lunarInfluence < 0 || lunarInfluence > 1) {
            throw new IllegalArgumentException("lunarInfluence must be within [0, 1]");
        }
        if (slackFloor < 0 || slackFloor > 1) {
            throw new IllegalArgumentException("slackFloor must be within [0, 1]");
        }
    }
}

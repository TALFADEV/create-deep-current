package fr.talfa.deepcurrent.core.turbine;

/**
 * What a tidal turbine needs and gives (GDD §4.2). Its speed follows the current and the tide; its stress capacity
 * per RPM is fixed, so the stress it supplies follows them too.
 *
 * @param referenceRpm    speed at the reference current with a tide coefficient of 1
 * @param maxRpm          speed it never exceeds
 * @param capacityPerRpm  stress capacity per RPM, in SU
 * @param minWetFaces     faces that must touch water for it to run
 * @param minWaterAbove   water blocks needed right above it
 */
public record TurbineSpec(double referenceRpm, int maxRpm, double capacityPerRpm, int minWetFaces,
                          int minWaterAbove) {
    /** Small turbine defaults, validated by the author: 16384 SU at 32 RPM in the reference conditions. */
    public static final TurbineSpec SMALL = new TurbineSpec(32, 256, 512, 4, 3);

    public TurbineSpec {
        if (referenceRpm < 0 || maxRpm < 0 || capacityPerRpm < 0 || minWetFaces < 0 || minWetFaces > 6
                || minWaterAbove < 0) {
            throw new IllegalArgumentException("invalid turbine spec");
        }
    }
}

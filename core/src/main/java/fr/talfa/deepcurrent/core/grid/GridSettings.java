package fr.talfa.deepcurrent.core.grid;

import java.util.EnumMap;
import java.util.Map;

/** Cable characteristics for every voltage class (GDD §4.5, §11). */
public final class GridSettings {
    /**
     * Defaults: LV 0.4 kV / 640 A (256 kW), HV 20 kV / 100 A (2 MW), EHV 400 kV / 40 A (16 MW).
     * Losses: LV keeps 82 % of its power over 100 blocks, HV over 500 blocks, EHV over 2500 blocks.
     */
    public static final GridSettings DEFAULT = new GridSettings(Map.of(
            VoltageClass.LV, new CableSpec(0.4, 640, 0.002),
            VoltageClass.HV, new CableSpec(20, 100, 0.0004),
            VoltageClass.EHV, new CableSpec(400, 40, 0.00008)));

    private final Map<VoltageClass, CableSpec> specs;

    public GridSettings(Map<VoltageClass, CableSpec> specs) {
        for (VoltageClass voltage : VoltageClass.values()) {
            if (!specs.containsKey(voltage)) {
                throw new IllegalArgumentException("missing cable spec for " + voltage);
            }
        }
        this.specs = new EnumMap<>(specs);
    }

    public CableSpec spec(VoltageClass voltage) {
        return specs.get(voltage);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GridSettings settings && specs.equals(settings.specs);
    }

    @Override
    public int hashCode() {
        return specs.hashCode();
    }
}

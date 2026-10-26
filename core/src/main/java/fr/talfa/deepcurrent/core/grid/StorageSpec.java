package fr.talfa.deepcurrent.core.grid;

/**
 * What a storage block can hold and move (GDD §4.7).
 *
 * @param capacityKwh       energy it holds when full, in kWh
 * @param maxChargeKw       power it can absorb, in kW
 * @param maxDischargeKw    power it can supply, in kW
 * @param chargeEfficiency  fraction of the absorbed power actually stored, from 0 to 1
 */
public record StorageSpec(double capacityKwh, double maxChargeKw, double maxDischargeKw, double chargeEfficiency) {
    /** Small battery defaults, validated by the author: 2 kWh, 16 kW each way, 90 % on charge. */
    public static final StorageSpec SMALL_BATTERY = new StorageSpec(2, 16, 16, 0.9);

    public StorageSpec {
        if (capacityKwh <= 0 || maxChargeKw < 0 || maxDischargeKw < 0
                || chargeEfficiency <= 0 || chargeEfficiency > 1) {
            throw new IllegalArgumentException("invalid storage spec");
        }
    }

    /** Capacity in kJ (kW x s), the unit stored energy is counted in. */
    public double capacityKj() {
        return capacityKwh * 3600;
    }
}

package fr.talfa.deepcurrent.core.grid;

/**
 * Electrical characteristics of one voltage class.
 *
 * @param nominalKv    voltage shown to players, in kV
 * @param maxCurrentA  current a cable carries before overloading, in A
 * @param lossPerBlock attenuation per block of cable: a cable of length L delivers {@code exp(-L * lossPerBlock)}
 *                     of the power injected into it
 */
public record CableSpec(double nominalKv, double maxCurrentA, double lossPerBlock) {
    public CableSpec {
        if (nominalKv <= 0 || maxCurrentA <= 0) {
            throw new IllegalArgumentException("voltage and current must be > 0");
        }
        if (lossPerBlock < 0) {
            throw new IllegalArgumentException("lossPerBlock must be >= 0");
        }
    }

    /** Power the cable carries before overloading, in kW (kV x A). */
    public double maxPowerKw() {
        return nominalKv * maxCurrentA;
    }

    /** Fraction of the injected power left after {@code length} blocks. */
    public double efficiency(double length) {
        return Math.exp(-length * lossPerBlock);
    }
}

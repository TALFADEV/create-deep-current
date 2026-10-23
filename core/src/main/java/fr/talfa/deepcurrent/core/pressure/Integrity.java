package fr.talfa.deepcurrent.core.pressure;

/**
 * Integrity arithmetic, in percent from 0 to 100 (GDD §3.4). Machines call {@link #degrade} at an interval
 * with the ticks elapsed since the last update, never every tick.
 */
public final class Integrity {
    public static final double MAX = 100;
    private static final double TICKS_PER_MINUTE = 20 * 60;

    private Integrity() {
    }

    /** Integrity lost per minute at this overshoot; zero when the hull holds. */
    public static double lossPerMinute(int overshoot, IntegritySettings settings) {
        if (overshoot <= 0) {
            return 0;
        }
        return settings.baseLossPerMinute() + settings.lossPerBlockPerMinute() * overshoot;
    }

    /** Integrity after {@code elapsedTicks} at a constant overshoot. */
    public static double degrade(double integrity, int overshoot, long elapsedTicks, IntegritySettings settings) {
        double loss = lossPerMinute(overshoot, settings) * elapsedTicks / TICKS_PER_MINUTE;
        return clamp(integrity - loss);
    }

    /** Integrity after a repair restoring {@code amount} percent. */
    public static double repair(double integrity, double amount) {
        return clamp(integrity + amount);
    }

    private static double clamp(double integrity) {
        return Math.max(0, Math.min(MAX, integrity));
    }
}

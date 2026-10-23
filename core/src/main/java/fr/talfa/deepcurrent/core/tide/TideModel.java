package fr.talfa.deepcurrent.core.tide;

/**
 * Tides tied to the time of day and the moon phase (GDD §4.3).
 *
 * <p>Two high and two low tides per Minecraft day. High tides happen when the moon is overhead (midnight, tick
 * 18000) and opposite (noon, tick 6000). The water level follows a cosine; the tidal stream, which drives the
 * turbines, follows the speed of that level: strongest at mid-tide, weakest at slack water.
 *
 * <p>Full and new moons give spring tides, quarters give neap tides. Phases use vanilla numbering:
 * 0 full moon, 2 last quarter, 4 new moon, 6 first quarter.
 */
public final class TideModel {
    public static final int TICKS_PER_DAY = 24000;
    public static final int MOON_PHASES = 8;
    /** Tick of the day with a high tide; the other one is half a day later. */
    public static final int HIGH_TIDE_TICK = 6000;

    private TideModel() {
    }

    /** Tide coefficient multiplying the turbines' output: amplitude x lunar factor x stream factor. */
    public static double coefficient(long dayTime, int moonPhase, TideSettings settings) {
        return settings.amplitude() * lunarFactor(moonPhase, settings) * streamFactor(dayTime, settings);
    }

    /** Snapshot of every tide value at a given time, for display. */
    public static TideState state(long dayTime, int moonPhase, TideSettings settings) {
        return new TideState(
                coefficient(dayTime, moonPhase, settings),
                lunarFactor(moonPhase, settings),
                streamFactor(dayTime, settings),
                level(dayTime),
                isRising(dayTime));
    }

    /** 1 at spring tides (full and new moon), {@code 1 - lunarInfluence} at neap tides (quarters). */
    public static double lunarFactor(int moonPhase, TideSettings settings) {
        double angle = Math.floorMod(moonPhase, MOON_PHASES) * Math.PI / 2;
        double neapness = (1 - Math.cos(angle)) / 2;
        return 1 - settings.lunarInfluence() * neapness;
    }

    /** Tidal stream strength within the day: {@code slackFloor} at high and low tide, 1 at mid-tide. */
    public static double streamFactor(long dayTime, TideSettings settings) {
        double stream = Math.abs(Math.sin(phase(dayTime)));
        return settings.slackFloor() + (1 - settings.slackFloor()) * stream;
    }

    /** Water level from -1 (low tide) to 1 (high tide). */
    public static double level(long dayTime) {
        return Math.cos(phase(dayTime));
    }

    /** Whether the water is rising (flood) rather than falling (ebb). */
    public static boolean isRising(long dayTime) {
        return Math.sin(phase(dayTime)) < 0;
    }

    /** Angle of the semi-diurnal cycle: 0 at high tide, two full turns per day. */
    private static double phase(long dayTime) {
        long tick = Math.floorMod(dayTime - HIGH_TIDE_TICK, TICKS_PER_DAY);
        return 2 * (2 * Math.PI) * tick / TICKS_PER_DAY;
    }
}

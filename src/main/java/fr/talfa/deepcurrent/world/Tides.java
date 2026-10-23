package fr.talfa.deepcurrent.world;

import fr.talfa.deepcurrent.DCTags;
import fr.talfa.deepcurrent.config.DCConfigs;
import fr.talfa.deepcurrent.config.DCServerConfig;
import fr.talfa.deepcurrent.core.current.CurrentField;
import fr.talfa.deepcurrent.core.current.CurrentSettings;
import fr.talfa.deepcurrent.core.tide.TideModel;
import fr.talfa.deepcurrent.core.tide.TideState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;

/**
 * Server-side access to tides and currents for a level: reads the world time, moon phase, seed and biomes, and
 * applies the debug override set by {@code /deepcurrent tide}.
 */
public final class Tides {
    /** Coefficient forced by an operator, or NaN when following the moon. */
    private static double forcedCoefficient = Double.NaN;
    private static CurrentField currentField;

    private Tides() {
    }

    public static TideState state(ServerLevel level) {
        TideState natural = TideModel.state(level.getDayTime(), level.getMoonPhase(), config().tideSettings());
        if (!isForced()) {
            return natural;
        }
        return new TideState(forcedCoefficient, natural.lunarFactor(), natural.streamFactor(), natural.level(),
                natural.rising());
    }

    /** Tide multiplier for turbine output. */
    public static double coefficient(ServerLevel level) {
        return state(level).coefficient();
    }

    public static boolean isForced() {
        return !Double.isNaN(forcedCoefficient);
    }

    public static void force(double coefficient) {
        forcedCoefficient = coefficient;
    }

    public static void release() {
        forcedCoefficient = Double.NaN;
    }

    /** What kind of current flows at a position, from the biome tags. */
    public static CurrentKind currentKind(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        if (biome.is(DCTags.HAS_OCEAN_CURRENT)) {
            return CurrentKind.OCEAN;
        }
        if (biome.is(DCTags.HAS_RIVER_CURRENT)) {
            return CurrentKind.RIVER;
        }
        return CurrentKind.NONE;
    }

    /** Current speed at a position in m/s: seeded per area at sea, constant in rivers, zero elsewhere. */
    public static double currentSpeed(ServerLevel level, BlockPos pos) {
        CurrentField field = currentField(level);
        return switch (currentKind(level, pos)) {
            case OCEAN -> field.speedAt(pos.getX(), pos.getZ());
            case RIVER -> field.settings().riverFactor() * field.settings().referenceSpeed();
            case NONE -> 0;
        };
    }

    /** Current multiplier for turbine output: speed relative to the reference speed. */
    public static double currentFactor(ServerLevel level, BlockPos pos) {
        return currentSpeed(level, pos) / currentField(level).settings().referenceSpeed();
    }

    /** Forgets the override and cached field when the server stops, so the next world starts clean. */
    public static void reset() {
        forcedCoefficient = Double.NaN;
        currentField = null;
    }

    private static CurrentField currentField(ServerLevel level) {
        CurrentSettings settings = config().currentSettings();
        long seed = level.getSeed();
        if (currentField == null || currentField.seed() != seed || !currentField.settings().equals(settings)) {
            currentField = new CurrentField(seed, settings);
        }
        return currentField;
    }

    private static DCServerConfig config() {
        return DCConfigs.server();
    }

    public enum CurrentKind {
        OCEAN,
        RIVER,
        NONE
    }
}

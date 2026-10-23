package fr.talfa.deepcurrent.config;

import java.util.Map;
import java.util.function.Supplier;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.core.current.CurrentSettings;
import fr.talfa.deepcurrent.core.grid.CableSpec;
import fr.talfa.deepcurrent.core.grid.GridSettings;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.core.pressure.IntegritySettings;
import fr.talfa.deepcurrent.core.pressure.PressureTiers;
import fr.talfa.deepcurrent.core.tide.TideSettings;
import net.createmod.catnip.config.ConfigBase;

/**
 * Balancing values (GDD §11). Stored per world and synced to clients.
 * The accessors at the bottom turn them into the settings objects of the core logic.
 */
public final class DCServerConfig extends ConfigBase {
    public final ConfigGroup grid = group(0, "grid", Comments.grid);
    public final ConfigInt stressPerKilowatt = i(256, 1, "stressPerKilowatt", Comments.stressPerKilowatt);
    public final ConfigFloat alternatorEfficiency =
            f(0.9f, 0.01f, 1f, "alternatorEfficiency", Comments.alternatorEfficiency);
    public final ConfigFloat lvVoltage = f(0.4f, 0.001f, 10000f, "lvVoltage", Comments.voltage);
    public final ConfigInt lvMaxCurrent = i(640, 1, "lvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat lvLossPerBlock = f(0.002f, 0f, 1f, "lvLossPerBlock", Comments.lossPerBlock);
    public final ConfigFloat hvVoltage = f(20f, 0.001f, 10000f, "hvVoltage", Comments.voltage);
    public final ConfigInt hvMaxCurrent = i(100, 1, "hvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat hvLossPerBlock = f(0.0004f, 0f, 1f, "hvLossPerBlock", Comments.lossPerBlock);
    public final ConfigFloat ehvVoltage = f(400f, 0.001f, 10000f, "ehvVoltage", Comments.voltage);
    public final ConfigInt ehvMaxCurrent = i(40, 1, "ehvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat ehvLossPerBlock = f(0.00008f, 0f, 1f, "ehvLossPerBlock", Comments.lossPerBlock);

    public final ConfigGroup tides = group(0, "tides", Comments.tides);
    public final ConfigFloat tideAmplitude = f(1f, 0f, 10f, "amplitude", Comments.tideAmplitude);
    public final ConfigFloat lunarInfluence = f(0.5f, 0f, 1f, "lunarInfluence", Comments.lunarInfluence);
    public final ConfigFloat slackFloor = f(0.25f, 0f, 1f, "slackFloor", Comments.slackFloor);

    public final ConfigGroup currents = group(0, "currents", Comments.currents);
    public final ConfigInt currentRegionSize = i(256, 16, 4096, "regionSize", Comments.regionSize);
    public final ConfigFloat minCurrentSpeed = f(0.5f, 0f, 20f, "minSpeed", Comments.minSpeed);
    public final ConfigFloat maxCurrentSpeed = f(2.5f, 0f, 20f, "maxSpeed", Comments.maxSpeed);
    public final ConfigFloat referenceCurrentSpeed = f(2f, 0.01f, 20f, "referenceSpeed", Comments.referenceSpeed);
    public final ConfigFloat riverFactor = f(0.2f, 0f, 10f, "riverFactor", Comments.riverFactor);

    public final ConfigGroup pressure = group(0, "pressure", Comments.pressure);
    public final ConfigInt tier1MaxDepth = i(16, 0, "tier1MaxDepth", Comments.tier1MaxDepth);
    public final ConfigInt tier2MaxDepth = i(32, 0, "tier2MaxDepth", Comments.tier2MaxDepth);
    public final ConfigInt tier3MaxDepth = i(64, 0, "tier3MaxDepth", Comments.tier3MaxDepth);
    public final ConfigFloat baseLossPerMinute = f(2f, 0f, 100f, "baseLossPerMinute", Comments.baseLossPerMinute);
    public final ConfigFloat lossPerBlockPerMinute =
            f(0.5f, 0f, 100f, "lossPerBlockPerMinute", Comments.lossPerBlockPerMinute);
    public final ConfigFloat leakingEfficiency = f(0.75f, 0f, 1f, "leakingEfficiency", Comments.leakingEfficiency);
    public final ConfigFloat criticalEfficiency = f(0.4f, 0f, 1f, "criticalEfficiency", Comments.criticalEfficiency);
    public final ConfigBool machinesBreak = b(true, "machinesBreak", Comments.machinesBreak);

    @Override
    public String getName() {
        return "server";
    }

    // --- Core settings -------------------------------------------------------------------

    public TideSettings tideSettings() {
        return orDefault("tides", TideSettings.DEFAULT, () -> new TideSettings(
                tideAmplitude.get(), lunarInfluence.get(), slackFloor.get()));
    }

    public CurrentSettings currentSettings() {
        return orDefault("currents", CurrentSettings.DEFAULT, () -> new CurrentSettings(
                currentRegionSize.get(), minCurrentSpeed.get(), maxCurrentSpeed.get(),
                referenceCurrentSpeed.get(), riverFactor.get()));
    }

    public PressureTiers pressureTiers() {
        return orDefault("pressure tiers", PressureTiers.DEFAULT, () -> new PressureTiers(
                tier1MaxDepth.get(), tier2MaxDepth.get(), tier3MaxDepth.get()));
    }

    public IntegritySettings integritySettings() {
        return orDefault("integrity", IntegritySettings.DEFAULT, () -> new IntegritySettings(
                baseLossPerMinute.get(), lossPerBlockPerMinute.get(),
                leakingEfficiency.get(), criticalEfficiency.get()));
    }

    public GridSettings gridSettings() {
        return orDefault("grid", GridSettings.DEFAULT, () -> new GridSettings(Map.of(
                VoltageClass.LV, new CableSpec(lvVoltage.get(), lvMaxCurrent.get(), lvLossPerBlock.get()),
                VoltageClass.HV, new CableSpec(hvVoltage.get(), hvMaxCurrent.get(), hvLossPerBlock.get()),
                VoltageClass.EHV, new CableSpec(ehvVoltage.get(), ehvMaxCurrent.get(), ehvLossPerBlock.get()))));
    }

    /** Values that pass the config's ranges can still be inconsistent together (e.g. min above max). */
    private static <T> T orDefault(String section, T fallback, Supplier<T> factory) {
        try {
            return factory.get();
        } catch (IllegalArgumentException e) {
            DeepCurrent.LOGGER.warn("Invalid {} config, using defaults: {}", section, e.getMessage());
            return fallback;
        }
    }

    private static final class Comments {
        static final String grid = "Electrical grid";
        static final String stressPerKilowatt = "Stress units (SU) converted into one kilowatt of electricity.";
        static final String alternatorEfficiency =
                "Fraction of the rotational power an alternator turns into electricity.";
        static final String voltage = "Nominal voltage of this cable class, in kV (shown to players).";
        static final String maxCurrent = "Current this cable class carries before overloading, in A.";
        static final String lossPerBlock = "Line loss per block: a cable of length L delivers exp(-L x loss) "
                + "of the power put into it.";

        static final String tides = "Tides (tied to the time of day and the moon phase)";
        static final String tideAmplitude = "Tide coefficient at mid-tide during spring tides.";
        static final String lunarInfluence = "How much weaker neap tides (moon quarters) are than spring tides "
                + "(full and new moon). 0 = no lunar effect.";
        static final String slackFloor = "Fraction of the tidal stream kept at slack water (high and low tide). "
                + "1 = no variation within the day.";

        static final String currents = "Ocean currents (per area, from the world seed)";
        static final String regionSize = "Width in blocks of a current area; speeds blend between areas.";
        static final String minSpeed = "Slowest ocean current, in m/s.";
        static final String maxSpeed = "Fastest ocean current, in m/s.";
        static final String referenceSpeed = "Current speed at which a turbine produces its nominal output, "
                + "in m/s.";
        static final String riverFactor = "Current factor in rivers, relative to the reference speed.";

        static final String pressure = "Pressure tiers and overpressure damage";
        static final String tier1MaxDepth = "Maximum depth of tier 1 hulls (brass), in blocks below sea level.";
        static final String tier2MaxDepth = "Maximum depth of tier 2 hulls (riveted steel).";
        static final String tier3MaxDepth = "Maximum depth of tier 3 hulls (abyssal alloy). Tier 4 has no limit.";
        static final String baseLossPerMinute = "Integrity (%) lost per minute by a machine below its depth limit.";
        static final String lossPerBlockPerMinute = "Extra integrity (%) lost per minute for each block beyond "
                + "the limit.";
        static final String leakingEfficiency = "Output multiplier below 50 % integrity.";
        static final String criticalEfficiency = "Output multiplier below 25 % integrity.";
        static final String machinesBreak = "Whether machines break at 0 % integrity.";
    }
}

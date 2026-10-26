package fr.talfa.deepcurrent.config;

import java.util.Map;
import java.util.function.Supplier;

import fr.talfa.deepcurrent.DeepCurrent;
import fr.talfa.deepcurrent.core.current.CurrentSettings;
import fr.talfa.deepcurrent.core.grid.CableSpec;
import fr.talfa.deepcurrent.core.grid.GridSettings;
import fr.talfa.deepcurrent.core.grid.StorageSpec;
import fr.talfa.deepcurrent.core.grid.VoltageClass;
import fr.talfa.deepcurrent.core.pressure.IntegritySettings;
import fr.talfa.deepcurrent.core.pressure.PressureTiers;
import fr.talfa.deepcurrent.core.tide.TideSettings;
import fr.talfa.deepcurrent.core.turbine.TurbineSpec;
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
    public final ConfigFloat alternatorStressImpact =
            f(64f, 0f, 4096f, "alternatorStressImpact", Comments.alternatorStressImpact);
    public final ConfigFloat motorStressCapacity =
            f(64f, 0f, 4096f, "motorStressCapacity", Comments.motorStressCapacity);
    public final ConfigInt manualRange = i(32, 1, 256, "manualRange", Comments.manualRange);
    public final ConfigInt blocksPerCoil = i(8, 1, 1024, "blocksPerCoil", Comments.blocksPerCoil);
    public final ConfigInt lossWarningPercent = i(15, 0, 100, "lossWarningPercent", Comments.lossWarningPercent);
    public final ConfigFloat lvVoltage = f(0.4f, 0.001f, 10000f, "lvVoltage", Comments.voltage);
    public final ConfigInt lvMaxCurrent = i(640, 1, "lvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat lvLossPerBlock = f(0.002f, 0f, 1f, "lvLossPerBlock", Comments.lossPerBlock);
    public final ConfigFloat hvVoltage = f(20f, 0.001f, 10000f, "hvVoltage", Comments.voltage);
    public final ConfigInt hvMaxCurrent = i(100, 1, "hvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat hvLossPerBlock = f(0.0004f, 0f, 1f, "hvLossPerBlock", Comments.lossPerBlock);
    public final ConfigFloat ehvVoltage = f(400f, 0.001f, 10000f, "ehvVoltage", Comments.voltage);
    public final ConfigInt ehvMaxCurrent = i(40, 1, "ehvMaxCurrent", Comments.maxCurrent);
    public final ConfigFloat ehvLossPerBlock = f(0.00008f, 0f, 1f, "ehvLossPerBlock", Comments.lossPerBlock);

    public final ConfigGroup storage = group(0, "storage", Comments.storage);
    public final ConfigFloat smallBatteryCapacity =
            f(2f, 0.01f, 100000f, "smallBatteryCapacity", Comments.batteryCapacity);
    public final ConfigFloat smallBatteryMaxCharge =
            f(16f, 0f, 100000f, "smallBatteryMaxCharge", Comments.batteryMaxCharge);
    public final ConfigFloat smallBatteryMaxDischarge =
            f(16f, 0f, 100000f, "smallBatteryMaxDischarge", Comments.batteryMaxDischarge);
    public final ConfigFloat chargeEfficiency = f(0.9f, 0.01f, 1f, "chargeEfficiency", Comments.chargeEfficiency);

    public final ConfigGroup turbines = group(0, "turbines", Comments.turbines);
    public final ConfigFloat smallTurbineReferenceRpm =
            f(32f, 0f, 256f, "smallTurbineReferenceRpm", Comments.turbineReferenceRpm);
    public final ConfigFloat smallTurbineCapacityPerRpm =
            f(512f, 0f, 65536f, "smallTurbineCapacityPerRpm", Comments.turbineCapacityPerRpm);
    public final ConfigInt turbineMinWetFaces = i(4, 0, 6, "minWetFaces", Comments.minWetFaces);
    public final ConfigInt turbineMinWaterAbove = i(3, 0, 64, "minWaterAbove", Comments.minWaterAbove);

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

    /**
     * A config value read where the server config may not be loaded yet (item tooltips in the main menu):
     * falls back to the default instead of throwing.
     */
    public static double orDefault(ConfigFloat value, double fallback) {
        try {
            return value.get();
        } catch (IllegalStateException | NullPointerException e) {
            return fallback;
        }
    }

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

    /** Small battery spec; falls back to the defaults before the config loads (item tooltips in the main menu). */
    public StorageSpec smallBatterySpec() {
        StorageSpec d = StorageSpec.SMALL_BATTERY;
        return new StorageSpec(orDefault(smallBatteryCapacity, d.capacityKwh()),
                orDefault(smallBatteryMaxCharge, d.maxChargeKw()),
                orDefault(smallBatteryMaxDischarge, d.maxDischargeKw()),
                orDefault(chargeEfficiency, d.chargeEfficiency()));
    }

    /** Small turbine spec; falls back to the defaults before the config loads (item tooltips in the main menu). */
    public TurbineSpec smallTurbineSpec() {
        TurbineSpec d = TurbineSpec.SMALL;
        try {
            return new TurbineSpec(smallTurbineReferenceRpm.get(), d.maxRpm(), smallTurbineCapacityPerRpm.get(),
                    turbineMinWetFaces.get(), turbineMinWaterAbove.get());
        } catch (IllegalStateException | NullPointerException e) {
            return d;
        }
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
        static final String alternatorStressImpact = "Stress an alternator puts on its shaft, per RPM (like "
                + "Create's machines). At 256 RPM, the default absorbs 16384 SU.";
        static final String motorStressCapacity = "Stress capacity of a fully supplied electric motor, per RPM. "
                + "It draws as much electricity as the stress its kinetic network actually uses.";
        static final String manualRange = "Longest cable a player can lay by hand, in blocks.";
        static final String blocksPerCoil = "Blocks of cable in one cable coil.";
        static final String lossWarningPercent = "Line losses (% of the injected power) above which laying a cable "
                + "warns the player to raise the voltage.";
        static final String storage = "Batteries";
        static final String batteryCapacity = "Energy a small battery holds when full, in kWh.";
        static final String batteryMaxCharge = "Power a small battery can absorb, in kW.";
        static final String batteryMaxDischarge = "Power a small battery can supply, in kW.";
        static final String chargeEfficiency = "Fraction of the power a battery absorbs that it actually stores.";
        static final String voltage = "Nominal voltage of this cable class, in kV (shown to players).";
        static final String maxCurrent = "Current this cable class carries before overloading, in A.";
        static final String lossPerBlock = "Line loss per block: a cable of length L delivers exp(-L x loss) "
                + "of the power put into it.";

        static final String turbines = "Tidal turbines";
        static final String turbineReferenceRpm = "Speed of a small tidal turbine at the reference current with a "
                + "tide coefficient of 1, in RPM. Its speed follows the current and the tide.";
        static final String turbineCapacityPerRpm = "Stress capacity of a small tidal turbine per RPM, in SU. "
                + "The default gives 16384 SU at 32 RPM, like a level 1 steam engine.";
        static final String minWetFaces = "Faces of a turbine that must touch water for it to run.";
        static final String minWaterAbove = "Water blocks needed straight above a turbine for it to run.";

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

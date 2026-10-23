package fr.talfa.deepcurrent.config;

import net.createmod.catnip.config.ConfigBase;

/**
 * Balancing values (GDD §11). Stored per world and synced to clients.
 * Sections (pressure, tides, maintenance...) are added with the milestones that use them.
 */
public final class DCServerConfig extends ConfigBase {
    public final ConfigGroup grid = group(0, "grid", Comments.grid);
    public final ConfigInt stressPerKilowatt = i(256, 1, "stressPerKilowatt", Comments.stressPerKilowatt);
    public final ConfigFloat alternatorEfficiency =
            f(0.9f, 0.01f, 1f, "alternatorEfficiency", Comments.alternatorEfficiency);

    @Override
    public String getName() {
        return "server";
    }

    private static final class Comments {
        static final String grid = "Electrical grid";
        static final String stressPerKilowatt = "Stress units (SU) converted into one kilowatt of electricity.";
        static final String alternatorEfficiency =
                "Fraction of the rotational power an alternator turns into electricity.";
    }
}

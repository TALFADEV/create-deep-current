package fr.talfa.deepcurrent.config;

import net.createmod.catnip.config.ConfigBase;

/**
 * Visual and audio settings (GDD §10 ter). Values are added with the milestones that use them.
 */
public final class DCClientConfig extends ConfigBase {
    public final ConfigGroup performance = group(0, "performance", Comments.performance);
    public final ConfigEnum<ParticleLevel> particles = e(ParticleLevel.FULL, "particles", Comments.particles);

    @Override
    public String getName() {
        return "client";
    }

    public enum ParticleLevel {
        FULL,
        REDUCED,
        OFF
    }

    private static final class Comments {
        static final String performance = "Performance";
        static final String particles = "Amount of ambient particles (bubbles, smoke, sediments).";
    }
}

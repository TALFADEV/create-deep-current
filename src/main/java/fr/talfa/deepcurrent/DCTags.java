package fr.talfa.deepcurrent;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/** Tags the mod reads; modpacks can edit them through datapacks (GDD §10 quater). */
public final class DCTags {
    /** Biomes with an ocean current (seeded per area): oceans and their coasts (beaches, stony shores). */
    public static final TagKey<Biome> HAS_OCEAN_CURRENT = biome("has_ocean_current");
    /** Biomes with a weak, constant river current. */
    public static final TagKey<Biome> HAS_RIVER_CURRENT = biome("has_river_current");

    private DCTags() {
    }

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, DeepCurrent.asResource(path));
    }
}
